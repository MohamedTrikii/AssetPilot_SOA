#!/usr/bin/env bash
# =====================================================================
#  AssetPilot - lancement (Linux / macOS / Git Bash)
#    ./scripts/start.sh docker   -> docker compose up --build
#    ./scripts/start.sh local    -> 4 services Maven en arriere-plan (logs dans logs/)
#    ./scripts/start.sh stop     -> arret des services locaux lances par ce script
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MODE="${1:-docker}"
LOGS="$ROOT/logs"

wait_healthy() { # $1=nom $2=url
  printf '    attente de %s' "$1"
  for _ in $(seq 1 80); do
    if curl -fs -m 3 "$2" | grep -q '"status":"UP"'; then echo " [OK]"; return 0; fi
    printf '.'; sleep 3
  done
  echo " [ECHEC] voir les logs"; return 1
}

summary() {
  cat <<'EOF'

=====================================================================
  AssetPilot est demarre
  API Gateway   http://localhost:8083
  Swagger UI    http://localhost:8083/swagger-ui.html
  WSDL          http://localhost:8083/ws/warranties.wsdl
  gRPC          localhost:9090
  Postman       postman/AssetPilot.postman_collection.json
=====================================================================
EOF
}

case "$MODE" in
  docker)
    cd "$ROOT"
    docker compose up --build -d
    wait_healthy warranty-service    http://localhost:8082/actuator/health
    wait_healthy maintenance-service http://localhost:8084/actuator/health
    wait_healthy asset-service       http://localhost:8081/actuator/health
    wait_healthy gateway             http://localhost:8083/actuator/health
    summary
    ;;
  local)
    mkdir -p "$LOGS"
    start() { # $1=service
      (cd "$ROOT/$1" && nohup mvn -B spring-boot:run > "$LOGS/$1.log" 2>&1 & echo $! > "$LOGS/$1.pid")
      echo "    $1 lance (logs : logs/$1.log)"
    }
    start warranty-service; start maintenance-service
    wait_healthy warranty-service    http://localhost:8082/actuator/health
    wait_healthy maintenance-service http://localhost:8084/actuator/health
    start asset-service
    wait_healthy asset-service       http://localhost:8081/actuator/health
    start gateway
    wait_healthy gateway             http://localhost:8083/actuator/health
    summary
    ;;
  stop)
    (cd "$ROOT" && docker compose down 2>/dev/null) || true
    for f in "$LOGS"/*.pid; do
      [ -f "$f" ] || continue
      pkill -P "$(cat "$f")" 2>/dev/null || true
      kill "$(cat "$f")" 2>/dev/null || true
      rm -f "$f"
    done
    echo "AssetPilot est arrete."
    ;;
  *)
    echo "Usage : $0 [docker|local|stop]"; exit 1 ;;
esac
