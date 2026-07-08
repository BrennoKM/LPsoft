# LPsoft

Projeto-exemplo de **Linha de Produto de Software (LPS)** em Java + Next.js.

Uma **única base de código** entrega produtos diferentes por cliente, cada cliente recebe só as funcionalidades que contratou, com um pacote de entrega próprio, usando apenas mecanismos **nativos do stack**: Maven multi-module, classpath scanning do Spring, Flyway multi-location, um registry de slots no frontend e um script de empacotamento. Sem framework de "plugins", sem feature flags espalhadas pelo código, sem rede entre módulos.

> A tese: dá para ter módulos isolados, dependências explícitas entre eles e corte por cliente, os ganhos que se busca em microserviços, resolvidos em **tempo de build**, dentro de um monólito modular. O `build.sh` *prova* isso: falha quando uma dependência obrigatória não é contratada e degrada graciosamente quando uma opcional falta.

## Stack

- **Backend:** Spring Boot 3.3 · Java 21 · Maven multi-module
- **Frontend:** Next.js 16 · TypeScript 5 · Tailwind CSS · TanStack Query
- **Banco:** PostgreSQL 16
- **Migrations:** Flyway multi-location, versão por timestamp `YYYYMMDDHHMMSS`
- **Container:** Docker + Docker Compose
- **CI:** GitHub Actions (`.github/workflows/ci.yml`) testes, montagem por cliente via `build.sh` e guarda LPS. **CD** (publicação GHCR/deploy) é *roadmap*.

## Conceito de LPS

| Conceito | No LPsoft |
|---|---|
| **Core** | A plataforma comum e **funcional por si só**: tem suas próprias funcionalidades, cadastro e login (auth JWT), CRUD de eventos, calendário, além dos contratos de evento e dos pontos de extensão (SPI). Não depende de, nem conhece, nenhuma feature opcional. |
| **Feature** | Módulo **opcional**, contratável por cliente. Depende só do core (e, quando declarado, de outra feature). |
| **Cliente** | Um manifesto `clients/<slug>.yml`, **fonte única de composição** (sem segredo): `delivery`, **lista** de `features` e `ports`. "Contratar" = adicionar o nome à lista `features` (ausência = não tem; nome fora do catálogo → build falha). Banco/JWT **não** ficam aqui, vêm do `.env`. |
| **Montagem** | O perfil Maven (backend) e o composition root (frontend) materializam o manifesto. O que não foi contratado **não existe** no artefato daquele cliente. |

Três clientes de exemplo:

- **`lite`:** só o core, zero features.
- **`plus`:** tudo, **menos** `analytics` (prova a dependência opcional).
- **`enterprise`:** todas as features.

## Estrutura do repositório

```
LPsoft/
├── backend/
│   ├── core/                 # biblioteca: domínio, auth, contratos, SPI, NÃO executável
│   ├── app/                  # bootstrap (tem o main; agrega core + features por perfil)
│   └── features/
│       ├── lembretes/        # política de antecedência (tela própria) + contrato do core
│       ├── categorias/       # feature autônoma (dados + rotas + UI + badge)
│       ├── resumo-por-categoria/  # depende ESTRITAMENTE de categorias (requires)
│       ├── recorrencia/      # estende o core gerando eventos
│       ├── analytics/        # onividente, escuta tudo, agrega
│       ├── notificacao/      # canal de aviso (emergente via contrato do core)
│       └── relatorios-pdf/   # PDF sem libs (dependência opcional via SPI)
├── frontend/
│   └── src/
│       ├── core/             # auth, eventos, calendário, registry de slots
│       ├── features/         # espelha o backend (cortado por cliente)
│       └── app/(protected)/  # rotas (guard hasFeature)
├── clients/                  # lite.yml · plus.yml · enterprise.yml (fonte única)
├── scripts/build.sh          # empacota dist/<cliente>/ (binary | source | image)
└── docker-compose.yml        # perfis dev / prod
```

> `core` é **biblioteca**, não roda sozinho (não tem `main`). Quem executa é o
> `app`, que agrega `core` + as features do perfil. Por isso todo comando de
> backend usa `-pl app -am` (build o `app` e seus módulos dependentes).

## Arquitetura - backend

Multi-module Maven. **Dois eixos independentes** no comando:

| Parâmetro | Controla | Regra |
|---|---|---|
| `-P <perfil>` | **Quais features** entram no build | sem `-P` → perfil `dev` = **todas** (conveniência de dev); `-P lite` = **nenhuma**; `-P plus` / `-P enterprise` = conforme o cliente |
| `-pl app -am` | **O que executar/empacotar** | `app` é o único módulo com `main`; `-am` builda `core` (+features do perfil) antes |

`-P` escolhe *o que tem dentro*; `-pl app -am` escolhe *o que ligar*.

> **Manifesto × perfil Maven, quem é a fonte da verdade:** o `clients/<slug>.yml` é a **fonte única**. O `build.sh` (a tooling de entrega) **gera** o POM enxuto a partir do manifesto, sem profiles, sem nome de cliente, tanto no modo `source` quanto no `binary`; ele **não** lê os perfis do `pom.xml` versionado. Esses perfis (`backend/pom.xml` + `backend/app/pom.xml`) existem **só como conveniência de desenvolvimento** (`./mvnw -P <slug>` ad-hoc) e são mantidos à mão por convenção, a entrega real nunca depende deles, então um cliente real jamais precisa aparecer no `pom.xml`. Num cenário de produção o manifesto de um cliente real fica fora do versionamento (como `.env`) e é injetado no CI a partir de uma **variável de GitHub Environment** (`vars.CLIENT_MANIFEST`, Environment de mesmo nome do slug). O workflow trata **todos os clientes igual**: se a variável existe, materializa `clients/<slug>.yml` a partir dela; senão usa o arquivo commitado (caso dos samples `lite`/`plus`/`enterprise`). A descoberta dos slugs no CI vem de `scripts/list-clients.sh` (mesma fonte da tooling, sem matriz hardcoded).

**Descoberta automática (sem registro manual):**

- `@SpringBootApplication(scanBasePackages = "io.lpsoft")` + `@EntityScan` +
  `@EnableJpaRepositories` em `io.lpsoft` → toda feature presente no classpath
  é descoberta. Feature fora do build = invisível, sem nenhuma linha de
  configuração condicional.
- **Flyway multi-location:** cada feature registra um
  `FlywayConfigurationCustomizer` que *anexa* sua location. O bean só existe se
  o módulo está no classpath → a migration da feature só roda se ela foi
  contratada. Sem feature, sem tabela.
- Trocar de perfil exige `mvn clean` (artefato de um build anterior contamina).

```mermaid
flowchart LR
  M["clients/&lt;slug&gt;.yml"] -->|perfil Maven| APP
  CORE["core (lib)"] --> APP["app (bootstrap, main)"]
  subgraph features["features (só as contratadas)"]
    F1[lembretes]
    F2[categorias]
    F7[resumo-por-categoria]
    F3[recorrencia]
    F4[analytics]
    F5[notificacao]
    F6[relatorios-pdf]
  end
  features --> APP
  CORE -.contratos / SPI.-> features
```

## Arquitetura - frontend

Espelha o backend:

- **Dev / build completo tem todas as features.** `hasFeature()` (lê
  `NEXT_PUBLIC_FEATURES`, exposto pelo manifesto) controla **menu e rota** em
  runtime, rota não contratada cai em `notFound()` (404).
- **O corte físico real é do `build.sh`**: antes do `next build` ele remove
  `src/features/<não-contratada>`, a rota correspondente e a linha dela no
  composition root. O bundle do cliente não carrega o código não contratado.
  *(Não se usa alias de bundler, Turbopack resolve os path-aliases do tsconfig
  com precedência; o corte é por remoção física, espelhando o Maven.)*

**Registry de slots = injeção de dependência análoga ao SPI do backend:**

- `src/core/shared/slots.ts`, o core define as "tomadas":
  `EventoRowSlot` (linha do evento), `EventoCreateSlot` (formulário de
  criação, read-write), `EventoBadgeSlot` (etiqueta read-only no calendário).
- `src/core/shared/features.ts`, **composition root**: importa o `register`
  das features contratadas. É o análogo ao *component scan* do Spring; o
  conjunto de imports é o "classpath", e o `build.sh` remove a linha de uma
  feature não contratada (espelha o perfil Maven).
- A feature se registra (`features/<x>/register.ts` →
  `registerEventoRowSlot(...)` etc.). O **core nunca importa uma feature**,
  só lê o registry; lista vazia ⇒ nada renderiza. Mesmo princípio do
  `List<SecaoRelatorio>` injetado no backend.

## Domínio

Mini-agenda compartilhada, substrato simples para demonstrar os mecanismos de
LPS sem ruído de regra de negócio.

- Entidades: `Usuario`, `Evento` (com `origem_id`, raiz da qual o evento
  deriva; soft-reference informal, sem FK física).
- Core: cadastro, login (JWT), CRUD de evento, **calendário mensal** (view
  padrão, navegação por mês, clicar no dia cria com data/horário sugerido,
  clicar no evento abre modal de edição/exclusão).

## Catálogo de features

Cada feature existe para provar um padrão de composição:

| Feature | Padrão | Backend | Frontend |
|---|---|---|---|
| `lembretes` | **Política visível**: reage a `EventoCriado` e decide *quando* avisar (antecedência configurável); publica o contrato do core `LembreteProgramado` | listener + política + REST | página (política + lista de programados) |
| `categorias` | **Autônoma**: dados, migration, rotas e UI próprios; referencia `evento` por FK informal | tabelas + REST | página + slots (linha, criação, badge no calendário) |
| `resumo-por-categoria` | **Dependência estrita**: importa os tipos de `categorias` e conta eventos por categoria, não compila sem ela | `requires: [categorias]` | painel injetado em `/categorias` via slot do core (não tem rota própria) |
| `recorrencia` | **Estende o core**: gera novos eventos a partir de um modelo; janela + "até"; job de reposição | regra + `EventoService.criar` | página + slot de criação |
| `analytics` | **Onividente**: escuta o contrato do core e agrega; implementa o SPI de relatório | agregação + endpoint | dashboard |
| `notificacao` | **Canal emergente**: reage ao contrato do core `LembreteProgramado` (zero dep entre features); dispatcher agendado marca como enviada na hora | listener + dispatcher (`@Scheduled`) | página (programada → enviada) |
| `relatorios-pdf` | Gera PDF (sem libs externas); **dependência opcional** de `analytics` via SPI | `integrates-with: [analytics]` | página com prévia do conteúdo + download |

No `lite` nenhuma aparece (rota 404, sem tabela, sem link). No `enterprise`
todas. No `plus`, tudo menos `analytics`, e o PDF sai sem a seção de
agregados, sem quebrar.

## Desacoplamento - os três tipos de relação

```mermaid
flowchart TD
  CORE["core: publica EventoCriado / LembreteProgramado / origemId · define SPI SecaoRelatorio"]
  CORE -->|emergente| LEM[lembretes]
  CORE -->|emergente| ANA[analytics]
  CORE -->|emergente| CAT["categorias (herança via origemId)"]
  LEM -->|publica LembreteProgramado| CORE
  CORE -->|emergente| NOTIF[notificacao]
  CAT ==>|estrita: requires| RES[resumo-por-categoria]
  ANA -.opcional: SPI do core.-> PDF[relatorios-pdf]
```

**1. Emergente - via contrato do core.** O core publica fatos
(`EventoCriado`, e `origemId` quando um evento deriva de outro). Features
reagem sem se conhecer: `lembretes` agenda, `analytics` conta, `categorias`
herda as categorias do modelo. Ninguém importa ninguém, só o contrato do core.
A própria `lembretes` publica outro contrato do core (`LembreteProgramado`);
`notificacao` reage a ele e dispara o aviso na hora, mesmo padrão, segundo salto.

**2. Estrita - `requires`.** A feature **não compila** sem a dependência:
dependência Maven no módulo da outra + import do contrato dela.
`feature-deps.yml` declara `requires: [...]`. O `build.sh` **falha (exit 1)**
se um cliente contratar a feature sem a dependência. Exemplo real:
`resumo-por-categoria` importa os tipos de `categorias` (`Categoria`,
`EventoCategoriaRepository`) e declara `requires: [categorias]`, um resumo
*por categoria* não existe sem o conceito de categoria.

**3. Opcional - `integrates-with`.** Zero acoplamento Maven entre as features.
A integração passa por um **ponto de extensão do core**
(`io.lpsoft.core.shared.spi.SecaoRelatorio`): `analytics` opcionalmente o
implementa; `relatorios-pdf` injeta `List<SecaoRelatorio>` (vazia se ausente).
Sem `analytics`, o PDF sai sem a seção, **builda e roda**, só degrada.

| | Emergente | Estrita (`requires`) | Opcional (`integrates-with`) |
|---|---|---|---|
| Acoplamento | nenhum (via core) | compile-time (dep Maven + import) | nenhum entre features (via SPI do core) |
| Conhece a outra? | não | sim (contrato dela) | não (só o SPI do core) |
| Sem a outra | não reage | **build falha** (exit 1) | builda, roda, **degrada** |
| Mediador | core (contrato) | a própria feature dependida | core (SPI) |

## Rodando local (desenvolvimento)

Requisitos: Java 21, Node 20+, Docker + Compose.

```bash
# 1. Banco (Postgres em :5482)
docker compose --profile dev up -d db-dev

# 2. Backend (pasta backend/), todas as features (perfil dev default)
#    SERVER_PORT explícito: sem ele o Spring sobe na porta PADRÃO 8080
#    (que não usamos) e o frontend procura a API em :8130 por padrão.
SERVER_PORT=8130 ./mvnw -pl app -am spring-boot:run   # API em http://localhost:8130/api/v1

# 3. Frontend (pasta frontend/)
npm run dev                                   # http://localhost:3050 → API em :8130
```

> **Portas padrão não são usadas.** O backend local **precisa** de
> `SERVER_PORT=8130` (o default 8080 do Spring é evitado e não casa com o
> frontend). Postgres dev em :5482 e frontend em :3050 já são não-padrão.

**Rodar exatamente como um cliente recebe:**

```bash
# Backend (sempre SERVER_PORT=8130, a porta padrão 8080 não é usada)
SERVER_PORT=8130 ./mvnw -pl app -am spring-boot:run            # tudo (default dev)
SERVER_PORT=8130 ./mvnw -P lite       -pl app -am spring-boot:run   # zero features
SERVER_PORT=8130 ./mvnw -P plus       -pl app -am spring-boot:run   # tudo menos analytics
SERVER_PORT=8130 ./mvnw -P enterprise -pl app -am spring-boot:run   # todas

# Frontend (espelha via CLIENT), :3050, fala com :8130
CLIENT=lite npm run dev
CLIENT=enterprise npm run dev
```

> Você **nunca** é obrigado a carregar tudo: sem `-P` o `dev` traz todas por conveniência; use `-P lite` para trabalhar/testar o que um cliente enxuto realmente recebe.

Build empacotado direto pelo Maven (sem o `build.sh`):

```bash
./mvnw -P enterprise -pl app -am clean package    # backend/app/target/lpsoft.jar
java -jar backend/app/target/lpsoft.jar
docker compose --profile prod up -d --build       # stack containerizada
```

### Portas

| | dev (compose) | prod (compose) | lite | plus | enterprise |
|---|---|---|---|---|---|
| Backend  | 8130 | 8131 | 8132 | 8134 | 8133 |
| Frontend | 3050 | 3051 | 3052 | 3054 | 3053 |
| Postgres | 5482 | 5483 | 5484 | 5486 | 5485 |

(Portas por cliente vêm do respectivo `clients/<slug>.yml`; rodam lado a lado
sem colisão.)

## Empacotamento por cliente - `scripts/build.sh`

```bash
scripts/build.sh <cliente> [--mode=binary|source|image] [--explain]
```

Lê `clients/<cliente>.yml` (`features` é **lista de nomes**; vazia/ausente = só o core), **valida**: nome de feature fora do catálogo `backend/features/` → erro/exit 1; **grafo de dependências** (`feature-deps.yml`: `requires` faltando → erro/exit 1; `integrates-with` é informativo). Corta o frontend fisicamente (restauração via `trap`, não suja a worktree) e produz `dist/<cliente>/`.

`--explain` = **dry-run**: imprime o grafo resolvido do cliente (features contratadas, `requires` satisfeitos, `integrates-with` presente/ausente) e **não gera nada**, útil para enxergar "o que anda junto com o quê" sem buildar.

> Modo de entrega: `--mode` (CLI) **>** campo `delivery` do manifesto **>** `binary`.

| Modo | O que entrega |
|---|---|
| `binary` (default) | POM enxuto **gerado do manifesto** (sem profiles) → `package` → JAR + Next standalone + `docker-compose.yml` + `.env` |
| `source` | Código-fonte **já filtrado** + **POM enxuto gerado** (só core+app+features contratadas, sem profiles) + compose; builda no `up --build` |
| `image` | Só o `docker-compose.yml` apontando para imagens publicadas (GHCR) |

Princípios:

- **O manifesto é input interno da tooling, nenhum modo o entrega.** As decisões são *projetadas* no artefato (perfil/POM, env, compose).
- Containers nomeados `lpsoft-<servico>-<cliente>` (rodam lado a lado).
- `mvn clean` por cliente (artefato stale contamina).

Provar a guarda de dependência estrita, um cliente que contrate
`resumo-por-categoria` sem `categorias` é recusado antes de qualquer build:

```text
$ scripts/build.sh <cliente-invalido>
>> Validando dependências de '<cliente-invalido>' (features: resumo-por-categoria)
  ✗ feature 'resumo-por-categoria' requer 'categorias', que não está contratada
ERRO: grafo de dependências inválido para '<cliente-invalido>'   → exit 1
```

Provar a degradação da opcional:

```bash
scripts/build.sh enterprise   # PDF inclui a seção de Analytics
scripts/build.sh plus         # mesmo PDF, sem a seção, sem erro
```

## Roadmap

- CD: publicação das imagens no GHCR + deploy. O CI já cobre testes,
  montagem por cliente com **matriz dinâmica** e injeção do manifesto via
  GitHub Environment (`vars.CLIENT_MANIFEST`); falta a entrega.
- Testes E2E (Playwright) cobrindo os fluxos por cliente.
