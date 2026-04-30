# CRUD Produtos — Backend

## Sobre o Projeto

API REST desenvolvida com Spring Boot para gerenciamento de um estoque de produtos. A aplicação oferece um CRUD completo de produtos com autenticação baseada em sessão, autenticação de dois fatores (2FA via TOTP), recuperação de senha por e-mail e proteção contra ataques de força bruta.

O backend também **serve o frontend** diretamente, eliminando problemas de cross-origin. Toda a aplicação (API + interface web) é acessada pelo mesmo endereço: `https://localhost:8443`.

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

**5. Aceite o certificado autoassinado**

Na primeira vez, acesse `https://localhost:8443` no navegador. Aparecerá um aviso de segurança — clique em **"Avançar"** (ou **"Proceed to localhost"**) para aceitar o certificado de desenvolvimento.

**6. Acesse a aplicação**

```
https://localhost:8443/login.html
```

> O frontend é servido automaticamente pelo Spring Boot a partir da pasta `../crud-springboot-front/`. Não é necessário nenhum servidor adicional.

---

## Perfis de Execução

O perfil ativo é controlado pela variável `APP_PROFILE` no `.env`.

| Perfil | Banco de Dados | Porta | SSL |
|--------|---------------|-------|-----|
| `dev` (padrão) | PostgreSQL / Supabase | 8443 | HTTPS ativado |
| `local` | MySQL local | 8080 | HTTP (sem SSL) |

### Perfil `local` (MySQL)

Edite `src/main/resources/application-local.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crud
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
```

Crie o banco antes de subir:
```sql
CREATE DATABASE crud;
```

No perfil `local`, a aplicação sobe em `http://localhost:8080/login.html` (sem HTTPS).

---

## Variáveis de Ambiente (.env)

| Variável | Descrição | Obrigatório |
|---|---|---|
| `APP_PROFILE` | Perfil ativo (`dev` ou `local`). Padrão: `dev` | Não |
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
- Todo o tráfego é protegido por TLS na porta 8443. Requisições HTTP na porta 8080 são redirecionadas automaticamente para HTTPS (perfis dev/prod), impedindo comunicação em texto claro.
- O cabeçalho `Strict-Transport-Security` (HSTS, `max-age=31536000; includeSubDomains`) instrui o navegador a usar exclusivamente HTTPS nas requisições futuras.

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
