#!/usr/bin/env bash
# Start Spring Boot services with live reload: DevTools restarts an app
# whenever this script recompiles changed sources into target/classes.
#
# Usage:
#   ./dev.sh                         start every module in SERVICES
#   ./dev.sh services/user-service   start a single module
#
# Add new runnable modules to SERVICES as you build them.
set -uo pipefail

SERVICES=(
  cloud/service-registry
  cloud/api-gateway
  services/user-service
  services/location-service
  services/flight-service
)

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

csv() {
  local IFS=,
  echo "$*"
}

if [ "${1:-}" != "" ]; then
  MODULES=("$1")
else
  MODULES=("${SERVICES[@]}")
fi

for module in "${MODULES[@]}"; do
  if [ ! -d "$module" ]; then
    echo "No such module: $module" >&2
    exit 1
  fi
done

LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"

PIDS=()
LOG_FILES=()

cleanup() {
  echo
  echo "[dev.sh] stopping..."
  [ -n "${TAIL_PID:-}" ] && kill "$TAIL_PID" 2>/dev/null
  local pid
  for pid in "${PIDS[@]}"; do
    pkill -P "$pid" 2>/dev/null
    kill "$pid" 2>/dev/null
  done
  wait 2>/dev/null
}
trap cleanup EXIT INT TERM

echo "[dev.sh] compiling $(csv "${MODULES[@]}")..."
mvn -pl "$(csv "${MODULES[@]}")" -am compile || exit 1

start_module() {
  local module="$1"
  local name
  name="$(basename "$module")"
  local log="$LOG_DIR/${name}.log"
  : >"$log"
  echo "[dev.sh] starting $name  →  $log"
  mvn -pl "$module" -am spring-boot:run >>"$log" 2>&1 &
  PIDS+=($!)
  LOG_FILES+=("$log")
}

wait_for_url() {
  local url="$1"
  local name="$2"
  local i
  echo "[dev.sh] waiting for $name..."
  for i in $(seq 1 60); do
    if curl -s -o /dev/null --connect-timeout 1 "$url"; then
      echo "[dev.sh] $name is up"
      return 0
    fi
    sleep 1
  done
  echo "[dev.sh] timed out waiting for $name at $url" >&2
  return 1
}

REGISTRY="cloud/service-registry"
GATEWAY="cloud/api-gateway"
if printf '%s\n' "${MODULES[@]}" | grep -qx "$REGISTRY"; then
  start_module "$REGISTRY"
  wait_for_url "http://localhost:8761" "Eureka"
fi

for module in "${MODULES[@]}"; do
  [ "$module" = "$REGISTRY" ] && continue
  [ "$module" = "$GATEWAY" ] && continue
  start_module "$module"
done

if printf '%s\n' "${MODULES[@]}" | grep -qx "services/user-service"; then
  wait_for_url "http://localhost:8761/eureka/apps/USER-SERVICE" "USER-SERVICE"
fi
if printf '%s\n' "${MODULES[@]}" | grep -qx "services/location-service"; then
  wait_for_url "http://localhost:8761/eureka/apps/LOCATION-SERVICE" "LOCATION-SERVICE"
fi
if printf '%s\n' "${MODULES[@]}" | grep -qx "services/flight-service"; then
  wait_for_url "http://localhost:8761/eureka/apps/FLIGHT-SERVICE" "FLIGHT-SERVICE"
fi

if printf '%s\n' "${MODULES[@]}" | grep -qx "$GATEWAY"; then
  start_module "$GATEWAY"
  wait_for_url "http://localhost:8080" "API Gateway"
fi

tail -n +1 -F "${LOG_FILES[@]}" &
TAIL_PID=$!

WATCH_DIRS=()
for module in "${MODULES[@]}"; do
  [ -d "$module/src/main" ] && WATCH_DIRS+=("$module/src/main")
done
[ -d common-lib/src/main ] && WATCH_DIRS+=(common-lib/src/main)

STAMP="$(mktemp)"
any_running() {
  local pid
  for pid in "${PIDS[@]}"; do
    kill -0 "$pid" 2>/dev/null && return 0
  done
  return 1
}

while any_running; do
  sleep 1
  [ -z "$(find "${WATCH_DIRS[@]}" -type f -newer "$STAMP" -print -quit 2>/dev/null)" ] && continue

  common_changed=""
  if [ -d common-lib/src/main ]; then
    common_changed="$(find common-lib/src/main -type f -newer "$STAMP" -print -quit 2>/dev/null)"
  fi

  changed_modules=()
  for module in "${MODULES[@]}"; do
    [ ! -d "$module/src/main" ] && continue
    [ -z "$(find "$module/src/main" -type f -newer "$STAMP" -print -quit 2>/dev/null)" ] && continue
    changed_modules+=("$module")
  done

  touch "$STAMP"

  if [ -n "$common_changed" ]; then
    echo "[dev.sh] common-lib changed, recompiling all..."
    mvn -q -pl "$(csv "${MODULES[@]}")" -am compile
    continue
  fi

  if [ "${#changed_modules[@]}" -eq 0 ]; then
    continue
  fi
  for module in "${changed_modules[@]}"; do
    echo "[dev.sh] $(basename "$module") changed, recompiling..."
    mvn -q -pl "$module" -am compile
  done
done
