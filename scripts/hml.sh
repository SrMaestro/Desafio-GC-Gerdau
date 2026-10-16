#!/usr/bin/env bash
# Ambiente de homologação: banco Postgres descartável + aplicação com o perfil "hml".
#   scripts/hml.sh run    sobe o banco (vazio) e roda a aplicação em http://localhost:8081
#   scripts/hml.sh up     só sobe o banco
#   scripts/hml.sh down   derruba o banco (os dados são descartados)
set -euo pipefail
cd "$(dirname "$0")/.."

case "${1:-run}" in
  up)
    docker compose --profile hml up -d --wait pgsql-db-hml ;;
  down)
    docker compose --profile hml rm -sf pgsql-db-hml ;;
  run)
    docker compose --profile hml rm -sf pgsql-db-hml >/dev/null 2>&1 || true
    docker compose --profile hml up -d --wait pgsql-db-hml
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=hml ;;
  *)
    echo "uso: $0 {run|up|down}" >&2; exit 1 ;;
esac
