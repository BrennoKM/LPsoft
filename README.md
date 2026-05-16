# LPsoft

Projeto-exemplo de **Linha de Produto de Software (LPS)** em Java + Next.js. Demonstra como uma única base de código pode entregar configurações diferentes por cliente — diferentes funcionalidades contratadas, diferentes pacotes de entrega, diferentes pipelines de release — usando apenas ferramentas nativas do stack (Maven multi-module, Spring auto-configuration, Flyway multi-location, webpack alias, GitHub Actions).

## Stack

- **Backend:** Spring Boot 3.3 + Java 21 + Maven multi-module
- **Frontend:** Next.js 16 + TypeScript 5 + Tailwind CSS
- **Banco:** PostgreSQL 16
- **Migrations:** Flyway (multi-location, versão por timestamp `YYYYMMDDHHMMSS`)
- **Container:** Docker + Docker Compose
- **CI/CD:** GitHub Actions + GHCR

## Estrutura

```
LPsoft/
├── backend/
│   ├── core/               # biblioteca: domínio, auth, contrato de eventos
│   ├── app/                # bootstrap executável (tem o main; agrega core + features)
│   └── features/           # módulos opcionais (ex.: lembretes)
├── frontend/               # Next.js (core + features espelhando o backend)
├── clients/                # Manifestos por cliente (YAML) — fonte única de features
├── scripts/                # build.sh (orquestra mvn + npm por cliente)
└── .github/workflows/      # CI/CD
```

> `core` é uma biblioteca, **não** é executável. Quem roda é o `app`, que agrega
> `core` + as features selecionadas. Por isso todo comando de execução aponta
> `-pl app -am` (ver abaixo).

## Como rodar (desenvolvimento)

Requisitos: Java 21, Node 20+, Docker + Compose.

```bash
# 1. Banco
docker compose --profile dev up -d db-dev          # Postgres na porta 5482

# 2. Backend (na pasta backend/)
./mvnw -pl app -am spring-boot:run                  # todas as features (default)

# 3. Frontend (na pasta frontend/)
npm run dev                                         # http://localhost:3050
```

Smoke check: `curl http://localhost:8130/api/v1/health`

### Dois controles independentes: `-P` e `-pl`

Ao rodar/buildar o backend, dois parâmetros do Maven fazem coisas **diferentes**:

| Parâmetro | Controla | Regra |
|---|---|---|
| `-P <perfil>` | **Quais features** entram no build | sem `-P` → perfil `dev` = **todas**; `-P lite` = **nenhuma**; `-P enterprise` = todas |
| `-pl app -am` | **O que executar/empacotar** | `app` é o único módulo com `main`; `-am` builda `core` (+features) antes |

`-P` escolhe *o que tem dentro*. `-pl app -am` escolhe *o que ligar*. Andam juntos no comando mas resolvem problemas distintos:

```bash
# Rodar como o produto completo (todas as features)
./mvnw -pl app -am spring-boot:run

# Rodar exatamente como o cliente 'lite' receberá (zero features)
./mvnw -P lite -pl app -am spring-boot:run

# Rodar como 'enterprise'
./mvnw -P enterprise -pl app -am spring-boot:run
```

Frontend, equivalente via variável `CLIENT`:

```bash
npm run dev                    # default (enterprise)
CLIENT=lite npm run dev        # simula o cliente lite
```

> Você **nunca** é obrigado a carregar todas as features: sem `-P` o default
> `dev` traz todas por conveniência de desenvolvimento; use `-P lite` para
> trabalhar/testar exatamente o que um cliente enxuto recebe.

### Build empacotado e produção

```bash
./mvnw -P enterprise -pl app -am clean package      # gera backend/app/target/lpsoft.jar
java -jar backend/app/target/lpsoft.jar             # roda o fat jar

# Stack completa containerizada
docker compose --profile prod up -d --build
```

> `mvn clean` é importante ao trocar de perfil: artefato de um build anterior
> pode contaminar o seguinte.

### Portas

| Serviço | Dev | Prod |
|---|---|---|
| Postgres | 5482 | 5483 |
| Backend | 8130 | 8131 |
| Frontend | 3050 | 3051 |

## Domínio

Mini-agenda compartilhada — substrato simples para demonstrar os mecanismos de LPS sem distrações de regra de negócio.

- Entidades atuais: `Usuario`, `Evento`
- Fluxos atuais: cadastro, login (JWT), criar evento, listar eventos

## Features

| Feature | Padrão demonstrado | Estado |
|---|---|---|
| `lembretes` | Listener puro de evento (sem migration, sem rota) | ✅ implementada |
| `categorias` | Feature autônoma com dados e rotas próprios | planejada |
| `recorrencia` | Estende entidade core via FK | planejada |
| `analytics` | Onividente — escuta tudo, agrega tudo | planejada |
| `push-notif` | Dependência estrita em `lembretes` | planejada |
| `relatorios-pdf` | Dependência opcional em `analytics` | planejada |

## Status

Em desenvolvimento. Funcionando: core (auth + eventos), frontend (login/eventos),
primeira feature opcional (`lembretes`) com seleção por perfil Maven.
