#!/usr/bin/env bash
# End-to-end check of the running Docker Compose stack.
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PORTFOLIO="SMOKE-$(date +%s)"
DELIVERY="$(date -u -d '+1 day' +%F)"

echo "Waiting for ${BASE_URL}/actuator/health ..."
for _ in $(seq 1 60); do
  if curl -fs "${BASE_URL}/actuator/health" | grep -q '"UP"'; then break; fi
  sleep 2
done
curl -fs "${BASE_URL}/actuator/health" | grep -q '"UP"'

echo "Booking a trade for ${PORTFOLIO}"
curl -fs -X POST "${BASE_URL}/api/trades" -H 'Content-Type: application/json' \
  -d "{\"portfolio\":\"${PORTFOLIO}\",\"commodity\":\"POWER\",\"direction\":\"BUY\",\"quantityMwh\":25,\"priceEurPerMwh\":95.5,\"deliveryDate\":\"${DELIVERY}\"}"
echo

echo "Waiting for the position to be projected from Kafka ..."
for _ in $(seq 1 30); do
  POSITIONS="$(curl -fs "${BASE_URL}/api/positions?portfolio=${PORTFOLIO}")"
  if echo "${POSITIONS}" | grep -q '"netQuantityMwh":25'; then
    echo "${POSITIONS}"
    echo "Smoke test passed"
    exit 0
  fi
  sleep 2
done

echo "Position not found: ${POSITIONS}" >&2
exit 1
