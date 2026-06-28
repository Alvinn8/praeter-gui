#!/usr/bin/env bash
# Builds the fat JAR, copies it here, then serves this directory over HTTP.
# Run from the repo root: ./web-test/html/serve.sh
# Then open http://localhost:8080 in a browser.

set -e
cd "$(dirname "$0")/../.."   # repo root

echo "Building fat JAR..."
./gradlew :web-test:copyFatJar

echo ""
echo "Serving at http://localhost:8080"
echo "Open that URL in a browser."
echo ""
cd web-test/html
python3 -m http.server 8080
