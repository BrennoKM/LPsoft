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
[ $# -ge 1 ] || die "uso: scripts/build.sh <cliente> [--mode=binary|source|image]"
CLIENT="$1"; shift
MODE="binary"
for arg in "$@"; do
  case "$arg" in
    --mode=*) MODE="${arg#--mode=}" ;;
    *) die "argumento desconhecido: $arg" ;;
  esac
done
case "$MODE" in binary|source|image) ;; *) die "modo inválido: $MODE" ;; esac

MANIFEST="$ROOT/clients/$CLIENT.yml"
[ -f "$MANIFEST" ] || die "manifesto não encontrado: clients/$CLIENT.yml"

# ── Parsing do manifesto (formato fixo e simples) ─────────────────────────
manifest_scalar() {
  grep -E "^$1:" "$MANIFEST" | head -1 \
    | sed -E "s/^$1:[[:space:]]*//; s/^\"//; s/\"$//"
}
section_val() {
  awk -v sec="$1" -v key="$2" '
    $0 ~ "^"sec":" { ins=1; next }
    ins && /^[^[:space:]]/ { ins=0 }
    ins && $1 == key":" { print $2; exit }
  ' "$MANIFEST"
}
feature_enabled() { section_val "features" "$1"; }

DISPLAY_NAME="$(manifest_scalar displayName)"
VERSION="$(manifest_scalar version)"
PORT_BE="$(section_val ports backend)"
PORT_FE="$(section_val ports frontend)"
PORT_DB="$(section_val ports db)"
DB_NAME="$(section_val db database)"
DB_USER="$(section_val db user)"
DB_PASS="$(section_val db password)"

# Catálogo completo (diretórios de feature no backend)
ALL_FEATURES=()
for d in "$ROOT"/backend/features/*/; do ALL_FEATURES+=("$(basename "$d")"); done

ENABLED=()
for f in "${ALL_FEATURES[@]}"; do
  [ "$(feature_enabled "$f")" = "true" ] && ENABLED+=("$f")
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
      gsub(/^[[:space:]]*-[[:space:]]*/, ""); gsub(/[[:space:]]+$/, "")
      print
    }
  ' "$file"
}

info "Validando dependências de '$CLIENT' (features: ${ENABLED[*]:-nenhuma})"
GRAPH_OK=1
while IFS= read -r f; do
  [ -n "$f" ] || continue
  for req in $(deps_of "$f" requires); do
    if ! is_enabled "$req"; then
      echo "  ✗ feature '$f' requer '$req', que não está contratada" >&2
      GRAPH_OK=0
    fi
  done
  for opt in $(deps_of "$f" integrates-with); do
    if is_enabled "$opt"; then
      echo "  · '$f' integra '$opt' (presente)"
    else
      echo "  · '$f' integra '$opt' (ausente — degrada graciosamente)"
    fi
  done
done < <(enabled_each)
[ "$GRAPH_OK" = "1" ] || die "grafo de dependências inválido para '$CLIENT'"
info "Grafo OK"

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
gen_env() {
  cat > "$OUT/.env" <<EOF
# Gerado por build.sh — cliente $CLIENT
CLIENT=$CLIENT
DISPLAY_NAME=$DISPLAY_NAME
VERSION=$VERSION
PORT_BACKEND=$PORT_BE
PORT_FRONTEND=$PORT_FE
PORT_DB=$PORT_DB
DB_NAME=$DB_NAME
DB_USER=$DB_USER
DB_PASSWORD=$DB_PASS
JWT_SECRET=troque-este-segredo-em-producao-min-32-bytes-please
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
name: lpsoft-$CLIENT

services:
  db:
    image: postgres:16-alpine
    container_name: lpsoft-db-$CLIENT
    environment:
      POSTGRES_DB: \${DB_NAME}
      POSTGRES_USER: \${DB_USER}
      POSTGRES_PASSWORD: \${DB_PASSWORD}
    ports:
      - "\${PORT_DB}:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U \${DB_USER} -d \${DB_NAME}"]
      interval: 5s
      timeout: 5s
      retries: 5
    volumes:
      - db-data:/var/lib/postgresql/data

  backend:
$be_svc
    container_name: lpsoft-backend-$CLIENT
    environment:
      - DATABASE_URL=jdbc:postgresql://db:5432/\${DB_NAME}
      - DATABASE_USER=\${DB_USER}
      - DATABASE_PASSWORD=\${DB_PASSWORD}
      - SERVER_PORT=8080
      - JWT_SECRET=\${JWT_SECRET}
      - CORS_ALLOWED_ORIGINS=http://localhost:\${PORT_FRONTEND}
    depends_on:
      db:
        condition: service_healthy
    ports:
      - "\${PORT_BACKEND}:8080"

  frontend:
$fe_svc
    container_name: lpsoft-frontend-$CLIENT
    # NEXT_PUBLIC_API_URL é fixado no build (não em runtime): apontado para
    # http://localhost:\${PORT_BACKEND}/api/v1 ao gerar este pacote.
    environment:
      - PORT=3000
      - HOSTNAME=0.0.0.0
    depends_on:
      - backend
    ports:
      - "\${PORT_FRONTEND}:3000"

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

  gen_env
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

  gen_env
  gen_compose source
  gen_readme source
}

# ── Modo: image ───────────────────────────────────────────────────────────
build_image() {
  info "Modo image: gerando apenas compose (imagens em ghcr.io/$GHCR_OWNER)"
  gen_env
  gen_compose image
  gen_readme image
}

case "$MODE" in
  binary) build_binary ;;
  source) build_source ;;
  image)  build_image ;;
esac

info "Pronto: dist/$CLIENT/ (modo $MODE)"
