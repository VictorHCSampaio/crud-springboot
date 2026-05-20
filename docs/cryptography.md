# Criptografia — CRUD Produtos

Documentacao do uso de criptografia em transito e em repouso no projeto Spring Boot.

## Visao geral

| Camada | Dado protegido | Mecanismo | Implementacao |
|--------|----------------|-----------|---------------|
| Em transito (cliente ↔ API) | HTTP, cookies, JSON | TLS/HTTPS | `application-local.properties`, `HttpsRedirectConfig` |
| Em transito (API ↔ banco) | Consultas JDBC | SSL/TLS do conector | `MYSQL_SSL_MODE`, `SPRING_DATASOURCE_URL` |
| Em transito (API ↔ SMTP) | E-mail de reset | STARTTLS | `spring.mail.*` (porta 587) |
| Em repouso | Senha do usuario | Hash adaptativo | BCrypt strength 12 — `PasswordService` |
| Em repouso | Segredo TOTP | Cifra simetrica autenticada | AES-256-GCM — `TotpSecretConverter` |

```mermaid
flowchart LR
    subgraph transito [Em transito]
        HTTPS[HTTPS TLS 8443]
        JDBC[JDBC sslMode]
        SMTP[SMTP STARTTLS]
    end
    subgraph repouso [Em repouso]
        BCrypt[BCrypt password_hash]
        AES[AES-256-GCM totp_secret]
    end
    Cliente --> HTTPS --> API[Spring Boot]
    API --> JDBC --> BD[(MySQL / PostgreSQL)]
    API --> SMTP --> Gmail[smtp.gmail.com]
    API --> BCrypt
    API --> AES
    AES --> BD
    BCrypt --> BD
```

---

## 1. HTTPS / TLS (dados em transito — aplicacao)

### Configuracao

Perfis `local` e `dev` (`application-local.properties`, `application-dev.properties`):

| Propriedade | Valor padrao |
|-------------|--------------|
| `server.port` | 8443 |
| `server.http.port` | 8080 |
| `server.ssl.enabled` | `true` |
| `server.ssl.key-store` | `file:certs/dev-keystore.p12` |
| `server.ssl.key-store-type` | PKCS12 |
| `server.ssl.key-alias` | `tomcat` |

### Redirect HTTP → HTTPS

`HttpsRedirectConfig` (ativo quando `server.ssl.enabled=true`):

- Conector adicional na porta **8080** redireciona para **8443**.
- Constraint Tomcat `CONFIDENTIAL` força canal seguro.

### HSTS e cookie de sessao

`SecurityConfig` (com SSL ativo):

- Cabecalho `Strict-Transport-Security` (max-age 31536000, includeSubDomains).
- `requiresChannel().anyRequest().requiresSecure()`.

`application.properties`:

```properties
server.servlet.session.cookie.secure=${server.ssl.enabled:false}
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax
```

### Geracao do keystore

```bash
bash scripts/generate-dev-keystore.sh
```

Variaveis: `SSL_KEYSTORE_FILE`, `SSL_KEYSTORE_PASSWORD`, `SSL_KEYSTORE_ALIAS`.

---

## 2. BCrypt — senhas em repouso

### Onde esta no codigo

| Componente | Arquivo |
|----------|---------|
| Encoder | `SecurityBeansConfig` → `BCryptPasswordEncoder` |
| Servico | `PasswordService` |
| Configuracao | `security.password.bcrypt.strength=12` |
| Coluna BD | `usuarios.password_hash` |

### Comportamento

- **Salt:** gerado automaticamente por hash; embutido no proprio valor BCrypt.
- **Custo:** strength **12** (2^12 iteracoes internas).
- **Operacoes:** `hashPassword()` no registro/reset; `matches()` no login.

### Justificativa tecnica

Alinhado a **NIST SP 800-63B**: usar funcoes de derivacao adaptativas para verificadores de senha, dificultando ataques offline por forca bruta.

---

## 3. AES-256-GCM — segredo TOTP em repouso

### Onde esta no codigo

| Componente | Arquivo |
|----------|---------|
| Conversor JPA | `TotpSecretConverter` |
| Entidade | `Usuario.totpSecret` com `@Convert(converter = TotpSecretConverter.class)` |
| Chave | `ENCRYPTION_KEY` (variavel de ambiente → `System.setProperty` via `DotenvConfig`) |

### Algoritmo

| Parametro | Valor |
|-----------|-------|
| Algoritmo JCA | `AES/GCM/NoPadding` |
| Tamanho da chave | 256 bits (32 bytes decodificados de Base64) |
| IV | 12 bytes aleatorios (`SecureRandom`) por cifragem |
| Tag GCM | 128 bits |
| Formato no BD | Base64( IV ‖ ciphertext+tag ) |

### Fluxo persistencia

```mermaid
sequenceDiagram
    participant App as Aplicacao
    participant Conv as TotpSecretConverter
    participant DB as Banco

    App->>Conv: convertToDatabaseColumn(plaintext)
    Conv->>Conv: AES-256-GCM encrypt
    Conv->>DB: string Base64 cifrada

    DB->>Conv: convertToEntityAttribute(ciphertext)
    Conv->>Conv: AES-256-GCM decrypt
    Conv->>App: plaintext (uso em TotpService)
```

### Compatibilidade legada

Se o valor no banco nao for um blob GCM valido (texto plano, Base32 curto, Base64 curto), o conversor **retorna o valor original** sem falhar — permite migracao gradual de registros antigos.

### Geracao da chave

```bash
openssl rand -base64 32
```

Configurar em `.env`:

```env
ENCRYPTION_KEY=<resultado-do-comando>
```

**Requisitos:** exatamente 32 bytes apos decodificacao Base64; nunca versionar o valor real.

### Servico auxiliar

`EncryptionService` implementa a mesma logica AES-GCM de forma generica; o campo TOTP usa exclusivamente `TotpSecretConverter` na persistencia JPA.

---

## 4. TOTP — segundo fator (RFC 6238)

| Item | Detalhe |
|------|---------|
| Biblioteca | `com.warrenstrange:googleauth` 1.5.0 |
| Servico | `TotpService` |
| Janela | `windowSize=1` |
| Issuer | `security.auth.totp.issuer=crud-produtos` |
| URI QR | `otpauth://totp/{issuer}:{username}?secret=...` |

O segredo em memoria e descriptografado pelo JPA ao carregar `Usuario`; em transito para o cliente, `TotpSetupResponse` expoe `secret` e `qrUri` apenas em HTTPS (registro ou setup 2FA).

---

## 5. TLS JDBC (aplicacao ↔ banco)

| Perfil | Parametro padrao na URL |
|--------|-------------------------|
| `local` | `sslMode=DISABLED` |
| `dev` | `sslMode=PREFERRED` |

Sobrescrever:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db?sslmode=require
MYSQL_SSL_MODE=REQUIRED
```

---

## 6. SMTP STARTTLS (e-mail de reset)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

Servico: `EmailService.sendPasswordResetEmail()` — token enviado no corpo do e-mail (nao retornado na API em producao).

---

## 7. Matriz de conformidade (requisitos 3.x do projeto)

| Requisito | Controle | Evidencia |
|-----------|----------|-----------|
| 3.1 TLS na aplicacao | HTTPS 8443 + keystore | `application-local.properties`, `HttpsRedirectConfig` |
| 3.2 Bloqueio HTTP claro | Redirect 8080 → 8443 | `HttpsRedirectConfig` |
| 3.4 Dado sensivel cifrado em repouso | `totp_secret` AES-GCM | `TotpSecretConverter` |
| 3.5 Algoritmo documentado | AES-256-GCM, BCrypt | Este documento + README |
| 3.6 Chave fora do codigo | `ENCRYPTION_KEY` no `.env` | `DotenvConfig`, `.env.example` |
| 3.7 Camadas de criptografia | TLS + BCrypt + AES-GCM + STARTTLS | Secoes acima |
| 3.8 Justificativa tecnica | NIST / RFC | README secao Seguranca |

---

## 8. Referencias tecnicas

- NIST FIPS 197 — AES
- NIST SP 800-38D — Galois/Counter Mode (GCM)
- NIST SP 800-63B — armazenamento de senhas
- RFC 6238 — TOTP
- RFC 8446 — TLS 1.3

---

## Arquivos relacionados

- [security-auth-flow.md](./security-auth-flow.md) — fluxo de login e 2FA
- [password-reset.md](./password-reset.md) — reset de senha e e-mail
- [security-controls.md](./security-controls.md) — gestao de credenciais e controles
- [security-tests.md](./security-tests.md) — testes de criptografia
