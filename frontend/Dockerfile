# syntax=docker/dockerfile:1.7
#
# A composição vem 100% por build-args (não carrega manifesto). next.config.mjs
# usa fallback por env quando não há clients/<slug>.yml.
# Build:
#   docker build -f frontend/Dockerfile \
#     --build-arg CLIENT=<cliente> --build-arg DISPLAY_NAME="..." \
#     --build-arg NEXT_PUBLIC_FEATURES='{"...":true}' \
#     --build-arg NEXT_PUBLIC_API_URL=http://localhost:<porta>/api/v1 -t lpsoft-frontend .

# ─── Stage 1: build ───────────────────────────────────────────────────────────
FROM node:20-alpine AS builder
WORKDIR /repo

COPY frontend/package.json frontend/package-lock.json* ./frontend/
RUN cd frontend && (test -f package-lock.json && npm ci || npm install)

COPY frontend ./frontend

ARG CLIENT=enterprise
ARG DISPLAY_NAME=${CLIENT}
ARG NEXT_PUBLIC_FEATURES={}
ARG NEXT_PUBLIC_API_URL=http://localhost:8131/api/v1
ENV CLIENT=${CLIENT}
ENV DISPLAY_NAME=${DISPLAY_NAME}
ENV NEXT_PUBLIC_FEATURES=${NEXT_PUBLIC_FEATURES}
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}

RUN cd frontend && npm run build

# ─── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production

RUN addgroup -S app && adduser -S app -G app

# Next standalone: server.js fica na RAIZ do standalone (projeto = frontend/,
# sem workspace acima). static/public ficam relativos a server.js.
COPY --from=builder --chown=app:app /repo/frontend/.next/standalone ./
COPY --from=builder --chown=app:app /repo/frontend/.next/static ./.next/static
COPY --from=builder --chown=app:app /repo/frontend/public ./public

USER app
EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

CMD ["node", "server.js"]
