# Controles de seguranca e gestao de credenciais

Documentacao dos controles implementados, gestao de segredos e identificacao de ativos do sistema CRUD Produtos.

## 1. Arquitetura logica

```mermaid
flowchart TB
    subgraph cliente [Cliente]
        Browser[Navegador]
    end

    subgraph app [Spring Boot - umc.devapp.crud_produtos]
        direction TB
        Static[Recursos estaticos frontend]
        AC[AuthController]
        PC[ProdutoController]
        SC[SecurityConfig]
        AS[AuthService]
        PS[ProdutoService]
        BF[BruteForceProtectionService]
        UR[(UsuarioRepository)]
        PR[(ProdutoRepository)]
    end

    subgraph externos [Servicos externos]
        DB[(MySQL / PostgreSQL)]
        Mail[SMTP Gmail]
    end

    Browser -->|HTTPS 8443| SC
    SC --> AC
    SC --> PC
    SC --> Static
    AC --> AS
    AC --> PasswordResetService
    PC --> PS
    AS --> BF
    AS --> UR
    PS --> PR
    UR --> DB
    PR --> DB
    PasswordResetService --> Mail
```

### Camadas

| Camada | Pacote | Responsabilidade |
|--------|--------|------------------|
| Apresentacao | `controller` | HTTP, validacao (`@Valid`), status codes |
| Negocio | `service` | Autenticacao, CRUD, e-mail, protecao |
| Persistencia | `repository`, `entity` | JPA, isolamento por usuario |
| Contratos | `dto.auth` | JSON request/response |
| Infraestrutura | `config` | Security, HTTPS, dotenv, criptografia |

---

## 2. Diagrama de implantacao

```mermaid
flowchart LR
    U[Usuario] -->|https://localhost:8443| T[Tomcat embarcado]
    U -->|http://localhost:8080| R[Redirect HTTPS]
    R --> T
    T --> K[certs/dev-keystore.p12]
    T --> F[../crud-springboot-front/]
    T -->|JDBC| DB[(Banco de dados)]
    T -->|STARTTLS:587| SMTP[smtp.gmail.com]
```

| Componente | Porta / protocolo |
|------------|-------------------|
| HTTPS API + frontend | 8443 (TLS) |
| HTTP redirect | 8080 → 8443 |
| JDBC | conforme URL (3306 MySQL, etc.) |
| SMTP | 587 (STARTTLS) |

---

## 3. Gestao de credenciais

### 3.1 Principios

1. **Segredos fora do codigo-fonte** — arquivo `.env` carregado por `DotenvConfig`.
2. **Nao versionar valores reais** — `.env`, `*.p12`, `/certs/` excluidos ou ignorados no Git.
3. **Senhas apenas como hash** — coluna `password_hash` (BCrypt).
4. **Segredo TOTP cifrado no BD** — `TotpSecretConverter` (AES-256-GCM).
5. **Autenticacao stateful** — sessao HTTP + cookie `JSESSIONID`.

### 3.2 Variaveis de ambiente

| Variavel | Finalidade | Obrigatorio |
|----------|------------|-------------|
| `APP_PROFILE` | Perfil Spring (`local`, `dev`) | Nao (padrao: `local`) |
| `PG_USER` | Usuario JDBC | Sim |
| `PG_PASSWORD` | Senha JDBC | Sim |
| `SPRING_DATASOURCE_URL` | URL JDBC completa | Nao |
| `MYSQL_SSL_MODE` | SSL do MySQL Connector/J | Nao |
| `MAIL_USERNAME` | Conta SMTP | Sim |
| `MAIL_PASSWORD` | Senha de app Gmail | Sim |
| `MAIL_FROM` | Remetente | Nao |
| `ENCRYPTION_KEY` | Chave AES-256 (Base64, 32 bytes) | Sim |
| `SSL_KEYSTORE_PASSWORD` | Senha do keystore PKCS12 | Nao |
| `SSL_KEYSTORE_FILE` | Caminho alternativo do `.p12` | Nao |
| `SSL_KEYSTORE_ALIAS` | Alias da chave no keystore | Nao |

Template: [.env.example](../.env.example)

### 3.3 Carregamento do `.env`

```java
// DotenvConfig — bloco static executado na inicializacao
Dotenv.configure().ignoreIfMissing().load()
    → System.setProperty(chave, valor)
```

Spring resolve `${VAR}` em `application.properties` a partir de propriedades do sistema / ambiente.

### 3.4 Politica de senhas

| Regra | Implementacao |
|-------|---------------|
| Tamanho minimo | 8 caracteres (`RegisterRequest` — `@Size(min = 8)`) |
| Armazenamento | BCrypt strength 12 |
| Reset | Nova senha hasheada; token UUID invalidado apos uso |

### 3.5 Sessao HTTP

| Parametro | Valor |
|-----------|-------|
| Timeout | 30 minutos |
| Cookie HttpOnly | `true` |
| Cookie Secure | `true` quando `server.ssl.enabled=true` |
| SameSite | `Lax` |
| Identificador | `JSESSIONID` |

### 3.6 Protecao contra forca bruta

| Parametro | Propriedade | Valor |
|-----------|-------------|-------|
| Max tentativas | `security.auth.max-attempts` | 5 |
| Bloqueio | `security.auth.lock-duration-minutes` | 15 min |
| Campos BD | `failed_attempts`, `lock_until` | `Usuario` |
| HTTP bloqueado | — | **423 LOCKED** |

Servico: `BruteForceProtectionService` — incrementa em falha de senha ou TOTP; zera em sucesso do 2FA.

---

## 4. Spring Security — regras de acesso

`SecurityConfig`:

| Recurso | Acesso |
|---------|--------|
| `/auth/**` | Publico |
| `/`, `/login.html`, `/scripts/**`, `/styles/**`, `/images/**`, `/**/*.html` (GET) | Publico |
| `/produto/**` | Autenticado |
| Demais rotas | Autenticado |

Outros controles:

- CSRF desabilitado (`csrf.disable()`)
- Form login / HTTP Basic desabilitados
- CORS: `allowedOriginPatterns("*")`, `allowCredentials(true)`
- Logout Spring: `/auth/logout` (invalida sessao e cookie)
- Logout aplicacao: `POST /auth/signout` (`AuthService.logout`)

---

## 5. Isolamento de dados (multi-usuario)

Cada usuario acessa apenas seus produtos:

```java
// ProdutoService
produtoRepository.findByUsuario(getCurrentUser())
produtoRepository.findByIdAndUsuario(id, getCurrentUser())
```

Usuario atual obtido via `SecurityContextHolder.getContext().getAuthentication().getName()`.

---

## 6. Identificacao de ativos

### 6.1 Software

| ID | Ativo | Criticidade |
|----|-------|-------------|
| A1 | Aplicacao Spring Boot (API + servidor estatico) | Alta |
| A2 | Frontend `crud-springboot-front` | Media |
| A3 | Codigo-fonte e dependencias Maven | Alta |
| A4 | Keystore TLS (`dev-keystore.p12`) | Alta |

### 6.2 Dados

| ID | Ativo | Local | Criticidade |
|----|-------|-------|-------------|
| D1 | Cadastro de usuarios (`usuarios`) | BD | Alta |
| D2 | `password_hash` | BD | Critica |
| D3 | `totp_secret` (cifrado) | BD | Critica |
| D4 | Tokens de reset | BD | Alta |
| D5 | Estoque (`produtos`) | BD | Media |
| D6 | Sessoes HTTP | Memoria Tomcat | Alta |

### 6.3 Credenciais e chaves

| ID | Ativo | Armazenamento | Criticidade |
|----|-------|---------------|-------------|
| K1 | `ENCRYPTION_KEY` | `.env` / SO | Critica |
| K2 | `PG_PASSWORD` | `.env` | Critica |
| K3 | `MAIL_PASSWORD` | `.env` | Alta |
| K4 | `SSL_KEYSTORE_PASSWORD` | `.env` | Alta |
| K5 | Cookie `JSESSIONID` | Cliente | Alta |

### 6.4 Infraestrutura

| ID | Ativo |
|----|-------|
| I1 | Servidor de banco (MySQL/PostgreSQL) |
| I2 | Servidor SMTP |
| I3 | Host de desenvolvimento / producao |

---

## 7. Endpoints e respostas (referencia)

### Autenticacao (`/auth`)

| Metodo | Path | Auth | Resposta 200 |
|--------|------|------|--------------|
| POST | `/register` | Nao | `TotpSetupResponse` |
| POST | `/login` | Nao | `AuthMessageResponse` |
| POST | `/2fa/setup` | Sessao parcial | `TotpSetupResponse` |
| POST | `/2fa/verify` | Sessao parcial | `AuthMessageResponse` |
| POST | `/signout` | Nao | `AuthMessageResponse` |
| POST | `/password-reset/request` | Nao | `PasswordResetResponse` |
| POST | `/password-reset/confirm` | Nao | `AuthMessageResponse` |

### Produtos (`/produto`)

| Metodo | Path | Auth | Resposta |
|--------|------|------|----------|
| GET | `/listall` | Sim | 200 — lista JSON |
| GET | `/list/{id}` | Sim | 200 / 404 |
| POST | `/add` | Sim | 201 — mensagem |
| PUT | `/update` | Sim | 200 — `Produto` |
| DELETE | `/delete/{id}` | Sim | 204 |

Detalhes de auth: [security-auth-flow.md](./security-auth-flow.md)  
Detalhes de reset: [password-reset.md](./password-reset.md)

---

## 8. Checklist operacional

- [ ] Copiar `.env.example` → `.env` e preencher valores
- [ ] Gerar `ENCRYPTION_KEY` com `openssl rand -base64 32`
- [ ] Executar `scripts/generate-dev-keystore.sh` antes de HTTPS local
- [ ] Nunca commitar `.env` nem keystores reais
- [ ] Em producao: certificado CA valido, CORS restrito, revisar CSRF

---

## Arquivos relacionados

- [cryptography.md](./cryptography.md)
- [risk-analysis.md](./risk-analysis.md)
- [security-tests.md](./security-tests.md)
