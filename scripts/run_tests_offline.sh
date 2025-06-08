#!/bin/bash
set -e

SPRING_BOOT_DIR="$HOME/.m2/repository/org/springframework/boot"
if [ ! -d "$SPRING_BOOT_DIR" ]; then
  echo "Local Maven repository missing Spring Boot artifacts. Downloading cache..."
  ARCHIVE_URL="https://example.com/m2_repo.tgz"
  TMP_ARCHIVE="$(mktemp)"
  curl -L "$ARCHIVE_URL" -o "$TMP_ARCHIVE"
  mkdir -p "$HOME/.m2"
  tar -xzf "$TMP_ARCHIVE" -C "$HOME/.m2"
  rm "$TMP_ARCHIVE"
fi

export JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false"

./mvnw -o -ntp test
