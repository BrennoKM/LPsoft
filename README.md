# LPsoft

Projeto-exemplo de **Linha de Produto de Software (LPS)** em Java + Next.js. Demonstra como uma única base de código pode entregar configurações diferentes por cliente — diferentes funcionalidades contratadas, diferentes pacotes de entrega, diferentes pipelines de release — usando apenas ferramentas nativas do stack (Maven multi-module, Spring auto-configuration, Flyway multi-location, webpack alias, GitHub Actions).

## Stack

- **Backend:** Spring Boot 3.3 + Java 21 + Maven multi-module
- **Frontend:** Next.js 14 + TypeScript 5 + Tailwind CSS
- **Banco:** PostgreSQL 16
- **Migrations:** Flyway (multi-location)
- **Container:** Docker + Docker Compose
- **CI/CD:** GitHub Actions + GHCR

## Estrutura

```
LPsoft/
├── backend/                # Spring Boot multi-module (core + features)
├── frontend/               # Next.js (em construção)
├── clients/                # Manifestos por cliente (YAML)
├── infra/                  # Templates Dockerfile, compose
├── scripts/                # build.sh
└── .github/workflows/      # CI/CD
```

## Como rodar

Requisitos: Java 21, Docker + Compose.

```bash
cp .env.example .env.dev    # ajustar JWT_SECRET, etc

# Tudo dockerizado (dev)
docker compose --profile dev up -d --build

# Apenas DB dockerizado + backend local (workflow comum em desenvolvimento)
docker compose --profile dev up -d db-dev
cd backend && ./mvnw spring-boot:run -pl core -am

# Produção local (mesmo compose, profile prod)
cp .env.example .env.prod
docker compose --profile prod up -d --build
```

### Portas

| Serviço | Dev | Prod |
|---|---|---|
| Postgres | 5482 | 5483 |
| Backend | 8130 | 8131 |
| Frontend | 3050 | 3051 |

Smoke check após subir:

```bash
curl http://localhost:8130/api/v1/health     # dev
curl http://localhost:8131/api/v1/health     # prod
```

## Domínio

Mini-agenda compartilhada — substrato simples para demonstrar os mecanismos de LPS sem distrações de regra de negócio.

- Entidades: `Usuario`, `Evento`, `Convite`
- Fluxos: cadastro, criar evento, publicar, convidar, aceitar/recusar, cancelar

## Features

| Feature | Padrão demonstrado |
|---|---|
| `lembretes` | Listener puro de evento (sem migration, sem rota) |
| `categorias` | Feature autônoma com dados e rotas próprios |
| `recorrencia` | Estende entidade core via FK |
| `analytics` | Onividente — escuta tudo, agrega tudo |
| `push-notif` | Dependência estrita em `lembretes` |
| `relatorios-pdf` | Dependência opcional em `analytics` |

## Status

Em desenvolvimento — esqueleto inicial.
