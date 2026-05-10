# CRUD Produtos — Backend

## Sobre o Projeto

API REST desenvolvida com Spring Boot para gerenciamento de um estoque de produtos. A aplicação oferece um CRUD completo de produtos com autenticação baseada em sessão, autenticação de dois fatores (2FA via TOTP), recuperação de senha por e-mail e proteção contra ataques de força bruta.

O backend também **serve o frontend** diretamente, eliminando problemas de cross-origin. Nos perfis **`local`** e **`dev`** a API e o frontend usam **HTTPS em `https://localhost:8443`**; a porta **8080** apenas redireciona para HTTPS.

---

## Tecnologias Utilizadas

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.4 |
| Spring Web | — |
| Spring Data JPA | — |
| Spring Security | — |
| Spring Validation | — |
| Spring Boot Mail | — |
| Lombok | — |
| MySQL Connector/J | — |
| PostgreSQL Driver | — |
| Google Authenticator (TOTP) | 1.5.0 |
| Maven | — |

---

## Pré-requisitos

- [Java 21+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- Banco de dados conforme o perfil em uso (ver seção de perfis abaixo)
- Uma conta de e-mail com SMTP habilitado (ex: Gmail com senha de app)

---

## Como Rodar

**1. Clone os dois repositórios lado a lado**
```bash
git clone https://github.com/seu-usuario/crud-springboot.git
git clone https://github.com/seu-usuario/crud-springboot-front.git
```

A estrutura de pastas deve ficar assim:
```
Nova pasta (2)/
├── crud-springboot/        ← backend (este repositório)
└── crud-springboot-front/  ← frontend (servido pelo backend)
```

**2. Configure as variáveis de ambiente**

Copie o arquivo de exemplo e preencha com seus dados:
```bash
cp .env.example .env
```

Edite o `.env` com seus valores reais (veja a seção de variáveis abaixo).

**3. Gere a chave AES-256 para criptografia**
```bash
openssl rand -base64 32
```
Cole o resultado no campo `ENCRYPTION_KEY` do `.env`.

**4. Compile e inicie a aplicação**
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

**5. Certificado HTTPS (`local` e `dev`)**

Gere o keystore **uma vez** na raiz do projeto:

```bash
bash scripts/generate-dev-keystore.sh
```

Na primeira vez, acesse `https://localhost:8443` e aceite o certificado autoassinado (**Avançar** / **Proceed to localhost**). Suba o backend **a partir da raiz** do repositório para o caminho `certs/dev-keystore.p12` ser encontrado.

**6. Acesse a aplicação**

`https://localhost:8443/login.html` (HTTP na porta 8080 redireciona para HTTPS).

> O frontend é servido automaticamente pelo Spring Boot a partir da pasta `../crud-springboot-front/`. Não é necessário nenhum servidor adicional.

---

## Perfis de Execução

O perfil ativo é controlado pela variável `APP_PROFILE` no `.env` (em `application.properties` o **padrão** é `local` se `APP_PROFILE` não estiver definida).

| Perfil | Banco (padrão no arquivo) | App | Observação |
|--------|---------------------------|-----|------------|
| `local` (padrão) | MySQL com `sslMode` **DISABLED** por defeito | **HTTPS 8443**, redirect na **8080** | MySQL clássico em `localhost` sem TLS |
| `dev` | MySQL com `sslMode` **PREFERRED** por defeito | **HTTPS 8443**, redirect na **8080** | Mesmo HTTPS; JDBC mais favorável a SSL no banco |

Para PostgreSQL/Supabase ou outra URL JDBC, defina `SPRING_DATASOURCE_URL` no ambiente (sobrescreve o padrão do perfil).

### HTTPS em `local` e `dev`

Os dois perfis usam o mesmo keystore PKCS12:

1. `bash scripts/generate-dev-keystore.sh` → cria `certs/dev-keystore.p12` (ignorado pelo Git).
2. Senha padrão: `changeit` (`SSL_KEYSTORE_PASSWORD`). Opcional: `SSL_KEYSTORE_FILE` para outro `.p12`.
3. `APP_PROFILE=local` (padrão) ou `APP_PROFILE=dev` no `.env`.

### Perfil `local` — MySQL

Credenciais: `PG_USER` / `PG_PASSWORD` no `.env`. `MYSQL_SSL_MODE` por defeito **DISABLED**; use `REQUIRED`/`PREFERRED` se o MySQL local exigir SSL.

Crie o banco antes de subir:
```sql
CREATE DATABASE crud;
```

### Cookie `Secure`

Com `server.ssl.enabled=true` nos perfis acima, o `JSESSIONID` é **`Secure`** (compatível com HTTPS).

---

## Variáveis de Ambiente (.env)

| Variável | Descrição | Obrigatório |
|---|---|---|
| `APP_PROFILE` | Perfil ativo (`dev` ou `local`). Padrão no código: `local` | Não |
| `MYSQL_SSL_MODE` | Modo SSL do conector MySQL (`DISABLED`, `PREFERRED`, `REQUIRED`, …). Ver documentação do MySQL Connector/J | Não |
| `SPRING_DATASOURCE_URL` | URL JDBC completa (sobrescreve a URL padrão do perfil) | Não |
| `SSL_KEYSTORE_FILE` | Caminho absoluto ou relativo ao diretório de trabalho para o `.p12` (perfil `dev`) | Não |
| `PG_USER` | Usuário do banco PostgreSQL | Sim (perfil dev) |
| `PG_PASSWORD` | Senha do banco PostgreSQL | Sim (perfil dev) |
| `MAIL_USERNAME` | E-mail remetente (ex: `seu@gmail.com`) | Sim |
| `MAIL_PASSWORD` | Senha de app do Gmail | Sim |
| `MAIL_FROM` | Endereço exibido no e-mail. Padrão: `MAIL_USERNAME` | Não |
| `ENCRYPTION_KEY` | Chave AES-256 em Base64 para criptografia de dados sensíveis | Sim |
| `SSL_KEYSTORE_PASSWORD` | Senha do keystore PKCS12. Padrão: `changeit` | Não |

**Gerar `ENCRYPTION_KEY`:**
```bash
openssl rand -base64 32
```

---

## Endpoints da API

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|-------------|
| POST | `/auth/register` | Cadastrar usuário | Pública |
| POST | `/auth/login` | Login (1ª etapa) | Pública |
| POST | `/auth/2fa/setup` | Obter QR Code TOTP | Pós-login |
| POST | `/auth/2fa/verify` | Validar código TOTP | Pós-login |
| POST | `/auth/signout` | Encerrar sessão | Pública |
| POST | `/auth/password-reset/request` | Solicitar reset de senha | Pública |
| POST | `/auth/password-reset/confirm` | Confirmar reset de senha | Pública |
| GET | `/produto/listall` | Listar produtos | Autenticado |
| GET | `/produto/list/{id}` | Buscar produto por ID | Autenticado |
| POST | `/produto/add` | Adicionar produto | Autenticado |
| PUT | `/produto/update` | Atualizar produto | Autenticado |
| DELETE | `/produto/delete/{id}` | Remover produto | Autenticado |

---

## Segurança e Criptografia

### Estratégia de Criptografia (Req. 3.7)

| Camada | Mecanismo | Algoritmo |
|--------|-----------|-----------|
| Comunicação (em trânsito) | HTTPS/TLS via Spring Boot Embedded Tomcat | TLS 1.2/1.3 com RSA-2048 |
| Senhas (em repouso) | Hash adaptativo com salt | BCrypt (strength 12) |
| Segredo TOTP (em repouso) | Criptografia simétrica autenticada | AES-256-GCM |
| E-mail de reset (SMTP) | Canal criptografado | STARTTLS (porta 587) |

### Justificativa Técnica (Req. 3.8)

**TLS/HTTPS**
- Com o perfil **`dev`** (`server.ssl.enabled=true`), o tráfego da API e do frontend servido pelo Spring usa **HTTPS na porta 8443**. Requisições HTTP na porta **8080** são redirecionadas para HTTPS (`HttpsRedirectConfig` + Tomcat `CONFIDENTIAL`).
- O cabeçalho **HSTS** (`Strict-Transport-Security`, `max-age=31536000; includeSubDomains`) é aplicado quando SSL está ativo (`SecurityConfig`).
- O cookie `JSESSIONID` usa **`Secure`** quando SSL está ativo (`server.servlet.session.cookie.secure=${server.ssl.enabled}`), o que vale nos perfis **`local`** e **`dev`** (HTTPS).
- **TLS JDBC**: parâmetro `sslMode` na URL do MySQL, configurável via `MYSQL_SSL_MODE` / URL customizada (`SPRING_DATASOURCE_URL`), para cifrar tráfego app→banco quando o servidor MySQL exige ou oferece SSL.

**BCrypt (senhas)**
- Algoritmo de hash adaptativo com salt embutido. O fator de custo `strength=12` torna ataques de força bruta inviáveis mesmo com hardware moderno.
- Referência: NIST SP 800-63B recomenda funções de derivação de chave adaptativas para armazenamento de senhas.

**AES-256-GCM (segredo TOTP)**
- **AES-256**: cifra simétrica aprovada pelo NIST para dados classificados até SECRET. Chave de 256 bits.
- **GCM**: modo autenticado (AEAD) que garante confidencialidade e integridade em uma única operação, detectando adulteração silenciosa.
- **IV aleatório de 12 bytes**: gerado por `SecureRandom` a cada cifração, garantindo que textos iguais produzam cifras distintas (IND-CPA).
- A chave AES é lida exclusivamente da variável de ambiente `ENCRYPTION_KEY`, nunca embutida no código.

**Proteção das chaves (Req. 3.6)**
- `ENCRYPTION_KEY` e `SSL_KEYSTORE_PASSWORD` são carregadas via `.env` / variável de ambiente do SO.
- O arquivo `.env` está no `.gitignore` e nunca deve ser versionado com valores reais.
