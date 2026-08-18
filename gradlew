#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")" && pwd)"

PROPS_FILE="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"
DIST_URL="$(grep '^distributionUrl=' "$PROPS_FILE" | cut -d= -f2-)"
DIST_URL="${DIST_URL//\\:/:}"
DIST_FILE="$(basename "$DIST_URL")"
GRADLE_VERSION="$(printf '%s' "$DIST_FILE" | sed -E 's/^gradle-(.+)-bin\.zip$/\1/')"

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
CACHE_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-$GRADLE_VERSION-bin"
ZIP_PATH="$CACHE_DIR/$DIST_FILE"
INSTALL_DIR="$CACHE_DIR/gradle-$GRADLE_VERSION"

mkdir -p "$CACHE_DIR"

if [ ! -x "$INSTALL_DIR/bin/gradle" ]; then
  if [ ! -f "$ZIP_PATH" ]; then
    curl -fsSL "$DIST_URL" -o "$ZIP_PATH"
  fi
  rm -rf "$INSTALL_DIR"
  unzip -q "$ZIP_PATH" -d "$CACHE_DIR"
fi

exec "$INSTALL_DIR/bin/gradle" -p "$APP_HOME" "$@"
