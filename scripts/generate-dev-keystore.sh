#!/usr/bin/env bash
# Gera PKCS12 para HTTPS no perfil dev (Tomcat / Spring Boot).
# Execute na raiz do projeto: bash scripts/generate-dev-keystore.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "${ROOT}/certs"
KEYSTORE="${ROOT}/certs/dev-keystore.p12"
STORE_PASS="${SSL_KEYSTORE_PASSWORD:-changeit}"
if [[ -f "${KEYSTORE}" ]]; then
  echo "Ja existe: ${KEYSTORE} (remova manualmente para regerar)"
  exit 0
fi
keytool -genkeypair \
  -alias tomcat \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "${KEYSTORE}" \
  -validity 3650 \
  -storepass "${STORE_PASS}" \
  -keypass "${STORE_PASS}" \
  -dname "CN=localhost, OU=Dev, O=crud-produtos, C=BR"
echo "Keystore criado: ${KEYSTORE}"
echo "Suba o backend com APP_PROFILE=local ou dev a partir da raiz do projeto (caminho relativo certs/)."
