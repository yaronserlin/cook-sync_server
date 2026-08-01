#!/usr/bin/env bash
# Curl-based driver/smoke test for the CookSync Spring Boot API.
# Exercises: readiness, register, login, authenticated validate-token,
# public recipe listing (plain + paged).
#
# Usage: ./smoke.sh [base_url]   (default base_url: http://localhost:8080)
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
EMAIL="smoke+$(date +%s)@example.com"
PASSWORD="Passw0rd!"

pass() { echo "PASS: $1"; }
fail() { echo "FAIL: $1"; exit 1; }

echo "== waiting for $BASE_URL =="
code=""
for _ in $(seq 1 30); do
  code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/recipes/public" || true)
  [ "$code" = "200" ] && break
  sleep 2
done
[ "$code" = "200" ] || fail "server never became ready at $BASE_URL/api/recipes/public (last status: $code)"
pass "server is up (GET /api/recipes/public -> 200)"

echo "== register =="
REGISTER_RESP=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Smoke\",\"lastName\":\"Test\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo "$REGISTER_RESP" | grep -q '"success":true' || fail "register failed: $REGISTER_RESP"
TOKEN=$(echo "$REGISTER_RESP" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")
pass "registered $EMAIL"

echo "== login =="
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo "$LOGIN_RESP" | grep -q '"success":true' || fail "login failed: $LOGIN_RESP"
pass "login ok"

echo "== validate-token (authenticated request) =="
curl -s "$BASE_URL/api/auth/validate-token" -H "Authorization: Bearer $TOKEN" | grep -q '"success":true' \
  || fail "validate-token failed"
pass "token valid"

echo "== public recipes (plain) =="
curl -s "$BASE_URL/api/recipes/public" | grep -q '"success":true' || fail "public recipes failed"
pass "public recipes ok"

echo "== public recipes (paged) =="
curl -s "$BASE_URL/api/recipes/public/paged?page=0&size=2" | grep -q '"success":true' \
  || fail "paged recipes failed"
pass "paged recipes ok"

echo
echo "ALL CHECKS PASSED against $BASE_URL"
