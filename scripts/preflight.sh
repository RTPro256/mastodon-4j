#!/bin/bash
set -euo pipefail

fail=0

check_cmd() {
  local name="$1"
  local cmd="$2"
  local hint="$3"

  if command -v "$cmd" >/dev/null 2>&1; then
    echo "✅ $name: $($cmd --version 2>/dev/null | head -n 1)"
  else
    echo "❌ $name not found. $hint"
    fail=1
  fi
}

echo "Preflight checks"

if [ -n "${JAVA_HOME:-}" ]; then
  echo "JAVA_HOME: $JAVA_HOME"
else
  echo "JAVA_HOME: (not set)"
fi

check_cmd "Java" "java" "Install OpenJDK 25 and ensure JAVA_HOME/bin is on PATH."
check_cmd "Maven Wrapper" "./mvnw" "Run from repo root or ensure mvnw is executable."

if command -v java >/dev/null 2>&1; then
  java --version | head -n 1
fi

if command -v docker >/dev/null 2>&1; then
  if [ -n "${DOCKER_HOST:-}" ] && [[ "${DOCKER_HOST}" == *docker_cli* ]]; then
    echo "❌ DOCKER_HOST points at docker_cli (${DOCKER_HOST})."
    echo "   Unset DOCKER_HOST or set it to npipe:////./pipe/docker_engine before running tests."
    fail=1
  elif docker info >/dev/null 2>&1; then
    echo "✅ Docker: $(docker version --format '{{.Server.Version}}' 2>/dev/null || docker --version)"
  else
    echo "❌ Docker not running. Start Docker Desktop to run Testcontainers-based tests."
    fail=1
  fi
else
  echo "❌ Docker not found. Install Docker Desktop to run Testcontainers-based tests."
  fail=1
fi

if [ $fail -ne 0 ]; then
  echo ""
  echo "Preflight failed. Fix the issues above and rerun."
  exit 1
fi

echo ""
 echo "All checks passed."
