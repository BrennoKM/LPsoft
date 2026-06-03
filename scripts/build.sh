#!/usr/bin/env bash
#
# build.sh — monta o pacote de entrega de um cliente a partir de
# clients/<cliente>.yml, espelhando o corte do backend (Maven) no frontend
# (remoção física das features não contratadas).
#
# Uso:
#   scripts/build.sh <cliente> [--mode=binary|source|image]
#
# Modos:
#   binary  (default)  compila e empacota JAR + Next standalone + compose
#   source             código-fonte JÁ filtrado + POM enxuto + compose
#   image              só compose apontando para imagens publicadas (GHCR)
#
# O manifesto clients/<cliente>.yml é input INTERNO da tooling: NENHUM modo
# o entrega. As decisões são projetadas no artefato (perfil/POM, env, compose).
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

GHCR_OWNER="${GHCR_OWNER:-brennokm}"

die() { echo "ERRO: $*" >&2; exit 1; }
info() { echo ">> $*"; }

# ── Argumentos ────────────────────────────────────────────────────────────
[ $# -ge 1 ] || die "uso: scripts/build.sh <cliente> [--mode=binary|source|image] [--explain]"
CLIENT="$1"; shift
MODE_ARG=""   # --mode= explícito tem prioridade; vazio = decidir pelo manifesto
EXPLAIN=0     # --explain = dry-run: imprime o grafo resolvido e NÃO gera nada
for arg in "$@"; do
  case "$arg" in
    --mode=*) MODE_ARG="${arg#--mode=}" ;;
    --explain) EXPLAIN=1 ;;
    *) die "argumento desconhecido: $arg" ;;
  esac
done

MANIFEST="$ROOT/clients/$CLIENT.yml"
[ -f "$MANIFEST" ] || die "manifesto não encontrado: clients/$CLIENT.yml"

# ── Parsing do manifesto (formato fixo e simples) ─────────────────────────
manifest_scalar() {
  grep -E "^$1:" "$MANIFEST" | head -1 \
    | sed -E "s/^$1:[[:space:]]*//; s/^\"//; s/\"$//"
}

# Modo de entrega: --mode= (prioridade) > campo 'delivery' do manifesto > binary
MODE="${MODE_ARG:-$(manifest_scalar delivery 2>/dev/null || true)}"
MODE="${MODE:-binary}"
case "$MODE" in
  binary|source|image) ;;
  *) die "modo de entrega inválido: '$MODE' (use binary|source|image)" ;;
esac
section_val() {
  awk -v sec="$1" -v key="$2" '
    $0 ~ "^"sec":" { ins=1; next }
    ins && /^[^[:space:]]/ { ins=0 }
    ins && $1 == key":" { print $2; exit }
  ' "$MANIFEST"
}
# features: é LISTA de nomes (não mapa true/false). Ausência = não
# contratada. Mesmo parser de listas do deps_of (aceita também `features: []`).
manifest_list() {  # <secao> -> um item por linha
  awk -v sec="$1" '
    $0 ~ "^"sec":" { ins=1; if ($0 ~ /\[\]/) ins=0; next }
    ins && /^[^[:space:][]/ { ins=0 }
    ins && /^[[:space:]]*-[[:space:]]*/ {
      gsub(/^[[:space:]]*-[[:space:]]*/, "")
      sub(/[[:space:]]*#.*/, "")          # corta comentário inline (- x  # nota)
      gsub(/[[:space:]]+$/, "")
      if (length) print
    }
  ' "$MANIFEST"
}

DISPLAY_NAME="$(manifest_scalar displayName)"
VERSION="$(manifest_scalar version)"
PORT_BE="$(section_val ports backend)"
PORT_FE="$(section_val ports frontend)"
PORT_DB="$(section_val ports db)"
# Creds do banco INTERNO da stack docker são hardcoded no compose pelo build.sh
# (não são segredo: o postgres só é acessível pela rede docker da stack). Vêm
# do .env da raiz (gitignored) se existir, senão .env.example (contrato
# commitado). JWT_SECRET, em contraste, é genuinamente sensível: o build.sh
# NÃO o coloca no compose — ele é carregado em runtime via env_file: .env,
# vindo de secrets.ENV no CD ou ausente (fallback do app) em dev local.
ENV_SRC="$ROOT/.env"; [ -f "$ENV_SRC" ] || ENV_SRC="$ROOT/.env.example"
env_value() { grep -E "^$1=" "$ENV_SRC" 2>/dev/null | tail -1 | sed -E "s/^$1=//" || true; }
DATABASE_NAME="$(env_value DATABASE_NAME)";         DATABASE_NAME="${DATABASE_NAME:-lpsoft}"
DATABASE_USER="$(env_value DATABASE_USER)";         DATABASE_USER="${DATABASE_USER:-lpsoft}"
DATABASE_PASSWORD="$(env_value DATABASE_PASSWORD)"; DATABASE_PASSWORD="${DATABASE_PASSWORD:-lpsoft}"

# Catálogo completo (diretórios de feature no backend) = fonte da verdade
ALL_FEATURES=()
for d in "$ROOT"/backend/features/*/; do ALL_FEATURES+=("$(basename "$d")"); done

# O que o cliente contratou (lista de nomes crua, do manifesto)
CONTRACTED=()
while IFS= read -r f; do [ -n "$f" ] && CONTRACTED+=("$f"); done < <(manifest_list features)

# Nome contratado que não existe no catálogo → ERRO (fail-loud; pega typo,
# ex.: 'lembrete' em vez de 'lembretes' — antes sumia calado).
for f in ${CONTRACTED[@]+"${CONTRACTED[@]}"}; do
  _ok=0
  for a in "${ALL_FEATURES[@]}"; do [ "$a" = "$f" ] && _ok=1 && break; done
  [ "$_ok" = 1 ] || die "feature '$f' contratada não existe no catálogo (backend/features/)"
done

# ENABLED na ordem do catálogo (determinístico p/ POM/JSON), só contratadas
ENABLED=()
for a in "${ALL_FEATURES[@]}"; do
  for f in ${CONTRACTED[@]+"${CONTRACTED[@]}"}; do
    [ "$a" = "$f" ] && ENABLED+=("$a") && break
  done
done

# Iteração segura: array vazia → ZERO iterações (sob `set -u`,
# "${ENABLED[@]:-}" produziria UMA string vazia — bug que vazava tudo).
enabled_each() { printf '%s\n' ${ENABLED[@]+"${ENABLED[@]}"}; }

is_enabled() {
  local x
  for x in ${ENABLED[@]+"${ENABLED[@]}"}; do [ "$x" = "$1" ] && return 0; done
  return 1
}

# JSON de features (name→bool) projetado no artefato (substitui o manifesto)
FEATURES_JSON="{"
_fj_first=1
for f in "${ALL_FEATURES[@]}"; do
  _v=false; is_enabled "$f" && _v=true
  [ $_fj_first -eq 1 ] || FEATURES_JSON+=","
  FEATURES_JSON+="\"$f\":$_v"; _fj_first=0
done
FEATURES_JSON+="}"

# ── Validação do grafo de dependências (feature-deps.yml) ─────────────────
deps_of() { # <feature> <requires|integrates-with>
  local file="$ROOT/backend/features/$1/feature-deps.yml" kind="$2"
  [ -f "$file" ] || return 0
  awk -v kind="$kind" '
    $0 ~ "^"kind":" {
      ins=1
      if ($0 ~ /\[\]/) ins=0
      next
    }
    ins && /^[^[:space:][]/ { ins=0 }
    ins && /^[[:space:]]*-[[:space:]]*/ {
      gsub(/^[[:space:]]*-[[:space:]]*/, "")
      sub(/[[:space:]]*#.*/, "")          # corta comentário inline
      gsub(/[[:space:]]+$/, "")
      if (length) print
    }
  ' "$file"
}

# Valida o grafo UMA vez e guarda as linhas já formatadas/alinhadas, pra
# render ser idêntico no build normal e no --explain (sem print duplicado).
#   requer  → obrigatória: faltar = build falha (exit 1)
#   integra → opcional: faltar só degrada (não falha)
# Cada relação vira um BLOCO de 2 linhas: o vínculo + a consequência em
# português claro (não um adjetivo solto). Mesmo render no build e no --explain.
GRAPH_OK=1
DEP_ROWS=()
while IFS= read -r f; do
  [ -n "$f" ] || continue
  for req in $(deps_of "$f" requires); do
    if is_enabled "$req"; then
      DEP_ROWS+=("$(printf '  [%s]  PRECISA DE  [%s]\n      [%s] contratada? SIM  →  [%s] compila; build ok' "$f" "$req" "$req" "$f")")
    else
      DEP_ROWS+=("$(printf '  [%s]  PRECISA DE  [%s]\n      [%s] contratada? NÃO  →  ✗ [%s] não compila — BUILD FALHA (exit 1)' "$f" "$req" "$req" "$f")")
      GRAPH_OK=0
    fi
  done
  for opt in $(deps_of "$f" integrates-with); do
    if is_enabled "$opt"; then
      DEP_ROWS+=("$(printf '  [%s]  INTEGRA (opcional)  [%s]\n      [%s] contratada? SIM  →  [%s] ativa a parte que usa [%s]' "$f" "$opt" "$opt" "$f" "$opt")")
    else
      DEP_ROWS+=("$(printf '  [%s]  INTEGRA (opcional)  [%s]\n      [%s] contratada? NÃO  →  [%s] builda e roda SEM a parte que usa [%s]' "$f" "$opt" "$opt" "$f" "$opt")")
    fi
  done
done < <(enabled_each)

print_deps() {
  if [ ${#DEP_ROWS[@]} -eq 0 ]; then
    echo "  — nenhuma relação entre as features contratadas —"
  else
    local r
    for r in "${DEP_ROWS[@]}"; do printf '%s\n\n' "$r"; done
  fi
}

if [ "$GRAPH_OK" != 1 ]; then
  {
    echo "Grafo de dependências INVÁLIDO — cliente '$CLIENT':"
    print_deps
  } >&2
  die "feature contratada sem a dependência obrigatória (linha ✗ acima)"
fi

if [ "$EXPLAIN" = 1 ]; then
  echo
  echo "═══ Plano: $CLIENT   (--explain · dry-run, nada foi gerado) ═══"
  echo "Entrega : $MODE"
  if [ ${#ENABLED[@]} -eq 0 ]; then
    echo "Features: nenhuma (só o core)"
  else
    echo "Features: ${#ENABLED[@]}"
    for f in "${ENABLED[@]}"; do echo "  ✓ $f"; done
  fi
  echo "Dependências entre features:"
  print_deps
  exit 0
fi

info "Grafo OK — ${#ENABLED[@]} feature(s)"
print_deps

# ── Saída ─────────────────────────────────────────────────────────────────
OUT="$ROOT/dist/$CLIENT"
rm -rf "$OUT"
mkdir -p "$OUT"
GIT_SHA="$(git -C "$ROOT" rev-parse --short HEAD 2>/dev/null || echo nogit)"

FE_BACKUP=""
restore_frontend() {
  [ -n "$FE_BACKUP" ] && [ -d "$FE_BACKUP" ] || return 0
  rm -rf "$ROOT/frontend/src/features" "$ROOT/frontend/src/app/(protected)"
  cp -a "$FE_BACKUP/features" "$ROOT/frontend/src/features"
  cp -a "$FE_BACKUP/(protected)" "$ROOT/frontend/src/app/(protected)"
  [ -f "$FE_BACKUP/features.ts" ] && cp -a "$FE_BACKUP/features.ts" "$ROOT/frontend/src/core/shared/features.ts"
  [ -f "$FE_BACKUP/next-env.d.ts" ] && cp -a "$FE_BACKUP/next-env.d.ts" "$ROOT/frontend/next-env.d.ts"
  rm -rf "$FE_BACKUP"
  FE_BACKUP=""
}
cut_frontend() {
  FE_BACKUP="$(mktemp -d)"
  cp -a "$ROOT/frontend/src/features" "$FE_BACKUP/features"
  cp -a "$ROOT/frontend/src/app/(protected)" "$FE_BACKUP/(protected)"
  local croot="$ROOT/frontend/src/core/shared/features.ts"
  [ -f "$croot" ] && cp -a "$croot" "$FE_BACKUP/features.ts"
  [ -f "$ROOT/frontend/next-env.d.ts" ] && cp -a "$ROOT/frontend/next-env.d.ts" "$FE_BACKUP/next-env.d.ts"
  trap restore_frontend EXIT
  local d name
  for d in "$ROOT"/frontend/src/features/*/; do
    name="$(basename "$d")"
    if ! is_enabled "$name"; then
      info "corte frontend: removendo feature '$name'"
      rm -rf "$ROOT/frontend/src/features/$name"
      rm -rf "$ROOT/frontend/src/app/(protected)/$name"
      [ -f "$croot" ] && sed -i "\|features/$name/register|d" "$croot"
    fi
  done
}

# ── compose / env / readme ────────────────────────────────────────────────
# .env.example é template: lista apenas as variáveis que o compose espera
# carregar via env_file no runtime (hoje só JWT_SECRET). Portas, creds do
# banco interno, CLIENT/DISPLAY_NAME/VERSION estão hardcoded no compose
# (vindos do manifesto via build.sh).
# Cliente real preenche os valores; no CD, secrets.ENV do GitHub Environment
# vira o .env final na VPS (apenas com os campos sensíveis).
gen_env_example() {
  cat > "$OUT/.env.example" <<EOF
# Variáveis carregadas pelo backend via env_file: .env (compose).
# Em produção, defina no GitHub Environment como secrets.ENV.
JWT_SECRET=
EOF
}

gen_compose() { # <mode>
  local mode="$1" be_svc fe_svc
  case "$mode" in
    binary)
      be_svc=$'    image: eclipse-temurin:21-jre\n    working_dir: /app\n    command: ["java","-jar","/app/app.jar"]\n    volumes:\n      - ./app.jar:/app/app.jar:ro'
      fe_svc=$'    image: node:22-alpine\n    working_dir: /app\n    command: ["node","server.js"]\n    volumes:\n      - ./frontend:/app:ro'
      ;;
    source)
      be_svc=$'    build:\n      context: ./backend\n      dockerfile: Dockerfile'
      fe_svc=$'    build:\n      context: .\n      dockerfile: frontend/Dockerfile\n      args:\n'
      fe_svc+=$'        CLIENT: '"$CLIENT"$'\n'
      fe_svc+=$'        DISPLAY_NAME: "'"$DISPLAY_NAME"$'"\n'
      fe_svc+=$'        NEXT_PUBLIC_FEATURES: \''"$FEATURES_JSON"$'\'\n'
      fe_svc+=$'        NEXT_PUBLIC_API_URL: http://localhost:'"$PORT_BE"'/api/v1'
      ;;
    image)
      be_svc="    image: ghcr.io/$GHCR_OWNER/lpsoft-backend:$VERSION-$CLIENT"
      fe_svc="    image: ghcr.io/$GHCR_OWNER/lpsoft-frontend:$VERSION-$CLIENT"
      ;;
  esac

  cat > "$OUT/docker-compose.yml" <<EOF
# Gerado por build.sh ($mode) — cliente $CLIENT ($DISPLAY_NAME)
# Portas, nomes e creds do banco interno são interpolados aqui pelo build.sh
# (literais no yml). O único \${VAR} que sobra é JWT_SECRET, carregado via
# env_file: .env, que vem do secrets.ENV do GitHub Environment no CD (no
# dev local, .env não existe e o backend cai no fallback do application.properties).
name: lpsoft-$CLIENT

services:
  db:
    image: postgres:16-alpine
    container_name: lpsoft-db-$CLIENT
    environment:
      POSTGRES_DB: $DATABASE_NAME
      POSTGRES_USER: $DATABASE_USER
      POSTGRES_PASSWORD: $DATABASE_PASSWORD
    ports:
      - "$PORT_DB:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $DATABASE_USER -d $DATABASE_NAME"]
      interval: 5s
      timeout: 5s
      retries: 5
    volumes:
      - db-data:/var/lib/postgresql/data

  backend:
$be_svc
    container_name: lpsoft-backend-$CLIENT
    env_file:
      - path: .env
        required: false
    environment:
      - DATABASE_URL=jdbc:postgresql://db:5432/$DATABASE_NAME
      - DATABASE_USER=$DATABASE_USER
      - DATABASE_PASSWORD=$DATABASE_PASSWORD
      - SERVER_PORT=8080
      - CORS_ALLOWED_ORIGINS=http://localhost:$PORT_FE
    depends_on:
      db:
        condition: service_healthy
    ports:
      - "$PORT_BE:8080"

  frontend:
$fe_svc
    container_name: lpsoft-frontend-$CLIENT
    # NEXT_PUBLIC_API_URL foi fixado no build (build-arg do Dockerfile,
    # apontando para http://localhost:$PORT_BE/api/v1).
    environment:
      - PORT=3000
      - HOSTNAME=0.0.0.0
    depends_on:
      - backend
    ports:
      - "$PORT_FE:3000"

volumes:
  db-data:
EOF
}

gen_readme() { # <mode>
  cat > "$OUT/README.md" <<EOF
# LPsoft — $DISPLAY_NAME

Pacote gerado por \`build.sh\` (modo **$1**).

- Cliente: \`$CLIENT\`
- Versão: \`$VERSION\` (\`$GIT_SHA\`)
- Features: ${ENABLED[*]:-nenhuma (só core)}

## Subir

\`\`\`bash
docker compose up -d        # use --build no modo source
\`\`\`

- Frontend: http://localhost:$PORT_FE
- Backend:  http://localhost:$PORT_BE/api/v1
EOF
  echo "$VERSION+$GIT_SHA ($CLIENT, $1)" > "$OUT/VERSION"
}

# POM enxuto: só core + app + features contratadas, SEM profiles e SEM
# mencionar nenhuma outra feature/cliente — o artefato é self-contained.
gen_parent_pom_slim() {
  local dest="$1"
  local mods
  mods=$'        <module>core</module>\n        <module>app</module>\n'
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    mods+="        <module>features/$f</module>"$'\n'
  done < <(enabled_each)
  cat > "$dest/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.2</version>
        <relativePath/>
    </parent>

    <groupId>io.lpsoft</groupId>
    <artifactId>lpsoft-parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>lpsoft-parent</name>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java-jwt.version>4.4.0</java-jwt.version>
        <testcontainers.version>1.21.3</testcontainers.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.auth0</groupId>
                <artifactId>java-jwt</artifactId>
                <version>\${java-jwt.version}</version>
            </dependency>
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>\${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- Composição já resolvida para o cliente '$CLIENT'. Sem profiles. -->
    <modules>
$mods    </modules>
</project>
EOF
}

gen_app_pom_slim() {
  local dest="$1"
  local deps=""
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    deps+=$'        <dependency>\n'
    deps+=$'            <groupId>io.lpsoft</groupId>\n'
    deps+="            <artifactId>feature-$f</artifactId>"$'\n'
    deps+=$'            <version>${project.version}</version>\n'
    deps+=$'        </dependency>\n'
  done < <(enabled_each)
  cat > "$dest/app/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.lpsoft</groupId>
        <artifactId>lpsoft-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>app</artifactId>
    <name>lpsoft-app</name>

    <dependencies>
        <dependency>
            <groupId>io.lpsoft</groupId>
            <artifactId>core</artifactId>
            <version>\${project.version}</version>
        </dependency>
$deps    </dependencies>

    <build>
        <finalName>lpsoft</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>io.lpsoft.app.Application</mainClass>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF
}

# Monta uma árvore de backend ENXUTA (core+app+features contratadas) com o
# POM gerado do manifesto, SEM profiles. É o que o cliente real teria — a
# tooling nunca lê o perfil do pom.xml versionado (esse é só conveniência
# de dev). Usada por binary (compila e descarta) e source (entrega o código).
stage_backend_slim() {
  local dest="$1"
  mkdir -p "$dest"
  rsync -a --exclude target "$ROOT/backend/core" "$ROOT/backend/app" "$dest/"
  cp "$ROOT/backend/mvnw" "$dest/" 2>/dev/null || true
  rsync -a "$ROOT/backend/.mvn" "$dest/" 2>/dev/null || true
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    mkdir -p "$dest/features"
    rsync -a --exclude target "$ROOT/backend/features/$f" "$dest/features/"
  done < <(enabled_each)
  gen_parent_pom_slim "$dest"
  gen_app_pom_slim "$dest"
}

# ── Modo: binary ──────────────────────────────────────────────────────────
build_binary() {
  info "Backend: POM enxuto do manifesto + package (sem -P; igual a um cliente real)"
  local bdir
  bdir="$(mktemp -d)"
  stage_backend_slim "$bdir"
  ( cd "$bdir" && ./mvnw -q -pl app -am -DskipTests clean package )
  cp "$bdir/app/target/lpsoft.jar" "$OUT/app.jar"
  rm -rf "$bdir"

  cut_frontend
  local api_url="http://localhost:${PORT_BE}/api/v1"
  info "Frontend: CLIENT=$CLIENT NEXT_PUBLIC_API_URL=$api_url npm run build"
  ( cd "$ROOT/frontend" && CLIENT="$CLIENT" NEXT_PUBLIC_API_URL="$api_url" npm run build >/dev/null )
  mkdir -p "$OUT/frontend"
  cp -a "$ROOT/frontend/.next/standalone/." "$OUT/frontend/"
  mkdir -p "$OUT/frontend/.next"
  cp -a "$ROOT/frontend/.next/static" "$OUT/frontend/.next/static"
  [ -d "$ROOT/frontend/public" ] && cp -a "$ROOT/frontend/public" "$OUT/frontend/public"
  restore_frontend
  trap - EXIT

  gen_env_example
  gen_compose binary
  gen_readme binary
}

# ── Modo: source ──────────────────────────────────────────────────────────
build_source() {
  info "Copiando código-fonte filtrado (POM enxuto, sem manifesto)"
  mkdir -p "$OUT/backend" "$OUT/frontend"
  stage_backend_slim "$OUT/backend"
  [ -f "$ROOT/backend/Dockerfile" ] && cp "$ROOT/backend/Dockerfile" "$OUT/backend/"
  cut_frontend
  rsync -a --exclude node_modules --exclude .next "$ROOT/frontend/" "$OUT/frontend/"
  restore_frontend
  trap - EXIT

  gen_env_example
  gen_compose source
  gen_readme source
}

# ── Modo: image ───────────────────────────────────────────────────────────
build_image() {
  info "Modo image: gerando apenas compose (imagens em ghcr.io/$GHCR_OWNER)"
  gen_env_example
  gen_compose image
  gen_readme image
}

case "$MODE" in
  binary) build_binary ;;
  source) build_source ;;
  image)  build_image ;;
esac

info "Pronto: dist/$CLIENT/ (modo $MODE)"
