#!/usr/bin/env bash
# 로컬 엔드투엔드: mavenLocal 발행 → 샘플 소비자 컴파일 → init.gradle로 archRulesCheck.
# report-only(exit 0)와 --threshold HIGH(exit 1) 둘 다 검증.
set -uo pipefail
ROOT="/Users/ryu-qqq/Documents/ryu-qqq/archrules-platform"
INIT="$ROOT/ci/archrules.init.gradle"
CONSUMER="$ROOT/ci/sample-consumer"

cd "$ROOT"
# sample-consumer는 자체 wrapper가 없으므로 root gradlew를 -p 로 사용한다.
./gradlew -q publishToMavenLocal || { echo "publish 실패"; exit 9; }
./gradlew -q -p "$CONSUMER" compileJava || { echo "consumer 컴파일 실패"; exit 9; }

echo "=== report-only (threshold none) ==="
./gradlew -p "$CONSUMER" --init-script "$INIT" \
  -ParchRulesVersion=0.1.0 \
  -ParchRulesClasses="$CONSUMER/build/classes/java/main" \
  -ParchRulesReport="$CONSUMER/build/reports/archrules/report.md" \
  -ParchRulesThreshold=none \
  archRulesCheck
RO=$?
echo "report-only exit=$RO"
echo "--- report ---"; cat "$CONSUMER/build/reports/archrules/report.md"

echo "=== gate (threshold HIGH) ==="
./gradlew -p "$CONSUMER" --init-script "$INIT" \
  -ParchRulesVersion=0.1.0 \
  -ParchRulesClasses="$CONSUMER/build/classes/java/main" \
  -ParchRulesReport="$CONSUMER/build/reports/archrules/report.md" \
  -ParchRulesThreshold=HIGH \
  archRulesCheck
GATE=$?
echo "gate exit=$GATE"

# 기대: report-only RO=0, gate GATE!=0 (JavaExec가 CLI exit 1을 task 실패로 전파)
[ "$RO" -eq 0 ] && [ "$GATE" -ne 0 ] && echo "PASS: 메커니즘 증명됨" || { echo "FAIL: RO=$RO GATE=$GATE"; exit 1; }
