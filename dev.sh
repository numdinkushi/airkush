#!/usr/bin/env bash
# Start a service with live reload: Spring Boot DevTools restarts the app
# whenever this script recompiles changed sources into target/classes.
#
# Usage: ./dev.sh [module]        e.g. ./dev.sh services/location-service
set -uo pipefail

MODULE="${1:-services/location-service}"
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

if [ ! -d "$MODULE" ]; then
  echo "No such module: $MODULE" >&2
  exit 1
fi

mvn -pl "$MODULE" -am spring-boot:run &
APP_PID=$!
trap 'kill "$APP_PID" 2>/dev/null' EXIT INT TERM

WATCH_DIRS=("$MODULE/src/main")
[ -d common-lib/src/main ] && WATCH_DIRS+=(common-lib/src/main)

STAMP="$(mktemp)"
while kill -0 "$APP_PID" 2>/dev/null; do
  sleep 1
  changed="$(find "${WATCH_DIRS[@]}" -type f -newer "$STAMP" -print -quit 2>/dev/null)"
  [ -z "$changed" ] && continue

  touch "$STAMP"
  echo "[dev.sh] change detected, recompiling..."
  mvn -q -pl "$MODULE" -am compile
done
