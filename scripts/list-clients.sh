#!/usr/bin/env sh
#
# list-clients.sh — slugs de cliente descobertos em clients/*.yml.
#
# Fonte da verdade = os manifestos versionados. Espelha a filosofia do
# build.sh (derivar a composição do filesystem, não de um índice manual).
#
# Convenção: manifestos cujo nome começa com '_' são internos/temporários
# (ex.: clients/_ci_invalid.yml usado pela guarda LPS no CI) e NÃO são
# clientes — ficam de fora da listagem.
#
# Uso:
#   scripts/list-clients.sh            # um slug por linha (ordenado)
#   scripts/list-clients.sh --json     # array JSON: ["enterprise","lite",...]
#
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CLIENTS_DIR="${LPSOFT_CLIENTS_DIR:-$ROOT/clients}"

FORMAT="lines"
if [ "${1:-}" = "--json" ]; then
  FORMAT="json"
elif [ $# -gt 0 ]; then
  echo "uso: list-clients.sh [--json]" >&2
  exit 2
fi

slugs=""
if [ -d "$CLIENTS_DIR" ]; then
  for f in "$CLIENTS_DIR"/*.yml; do
    [ -e "$f" ] || continue                 # glob sem match → pula
    name="$(basename "$f" .yml)"
    case "$name" in
      _*) continue ;;                       # interno/temporário → fora
    esac
    slugs="$slugs$name
"
  done
fi

# ordena, remove vazios/duplicados
slugs="$(printf '%s' "$slugs" | sed '/^$/d' | sort -u)"

if [ "$FORMAT" = "json" ]; then
  if [ -z "$slugs" ]; then
    printf '[]\n'
  else
    printf '%s' "$slugs" \
      | awk 'BEGIN{printf "["} {printf "%s\"%s\"", (NR>1?",":""), $0} END{print "]"}'
  fi
else
  [ -z "$slugs" ] || printf '%s\n' "$slugs"
fi
