#!/usr/bin/env bash
set -e
JAR_FILE=$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)
if [ -z "$JAR_FILE" ]; then
  echo "Executable Spring Boot JAR not found"
  exit 1
fi
exec java -Dserver.port="${PORT:-8080}" -jar "$JAR_FILE"
