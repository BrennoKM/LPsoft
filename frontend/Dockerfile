# syntax=docker/dockerfile:1.7
#
# Context esperado: raiz do repositório (precisa acessar ./clients/<cliente>.yml).
# Build:
#   docker build -f frontend/Dockerfile --build-arg CLIENT=<cliente> -t lpsoft-frontend .

# ─── Stage 1: build ───────────────────────────────────────────────────────────
FROM node:20-alpine AS builder
WORKDIR /repo

COPY frontend/package.json frontend/package-lock.json* ./frontend/
RUN cd frontend && (test -f package-lock.json && npm ci || npm install)

COPY clients ./clients
COPY frontend ./frontend

ARG CLIENT=enterprise
ARG NEXT_PUBLIC_API_URL=http://localhost:8131/api/v1
ENV CLIENT=${CLIENT}
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}

RUN cd frontend && npm run build

# ─── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production

RUN addgroup -S app && adduser -S app -G app

COPY --from=builder --chown=app:app /repo/frontend/.next/standalone ./
COPY --from=builder --chown=app:app /repo/frontend/.next/static ./frontend/.next/static
COPY --from=builder --chown=app:app /repo/frontend/public ./frontend/public

USER app
EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

CMD ["node", "frontend/server.js"]
