# Testes de seguranca

Documentacao dos testes automatizados e roteiro de testes manuais do projeto CRUD Produtos.

## 1. Visao geral

| Tipo | Framework | Banco em testes |
|------|-----------|-----------------|
| Unitario | JUnit 5 | N/A (mocks / sem BD) |
| Integracao | Spring Boot Test | H2 em memoria |

Executar todos os testes:

```bash
mvn test
```

Configuracao de teste: `src/test/resources/application.properties` (H2, `ddl-auto=create-drop`).

---

## 2. Testes automatizados

### 2.1 PasswordServiceTest

**Arquivo:** `src/test/java/umc/devapp/crud_produtos/service/PasswordServiceTest.java`

**Foco:** BCrypt — hash, validacao e unicidade do salt.

| Teste | Descricao | Resultado esperado |
|-------|-----------|-------------------|
| `shouldHashAndValidatePasswordWithBcrypt` | Hash de senha e `matches()` | PASS |
| `shouldGenerateDifferentHashesForSamePasswordBecauseOfSalt` | Dois hashes da mesma senha sao diferentes; ambos validam | PASS |

**Evidencia de seguranca:** confirma que senhas nao sao armazenadas em texto claro e que cada hash usa salt proprio (comportamento padrao BCrypt).

---

### 2.2 TotpSecretConverterTest

**Arquivo:** `src/test/java/umc/devapp/crud_produtos/config/TotpSecretConverterTest.java`

**Foco:** Criptografia AES-256-GCM do campo `totp_secret`.

Setup: `@BeforeAll` define `ENCRYPTION_KEY` (32 bytes fixos em Base64 para reproducibilidade).

| Teste | Descricao | Resultado esperado |
|-------|-----------|-------------------|
| `roundTripEncryptThenDecrypt` | Cifra e decifra segredo TOTP | PASS — plaintext igual |
| `legacyPlainSecretNotValidBase64ReturnedAsIs` | Segredo legado em texto plano | PASS — sem excecao |
| `legacyBase32DecodedTooShortReturnedAsIs` | Base32 curto (nao e blob GCM) | PASS |
| `base64DecodedUnderMinLengthReturnedAsIs` | Blob Base64 menor que IV+tag | PASS |

**Evidencia de seguranca:** cifragem em repouso funciona; migracao de dados legados nao quebra leitura.

---

### 2.3 AuthSecurityPropertiesTest

**Arquivo:** `src/test/java/umc/devapp/crud_produtos/config/AuthSecurityPropertiesTest.java`

**Foco:** Parametros de seguranca configurados.

| Parametro | Valor esperado |
|-----------|----------------|
| BCrypt strength | 12 |
| `maxAttempts` | 5 |
| `lockDurationMinutes` | 15 |
| TOTP issuer | `crud-produtos` |

---

### 2.4 CrudProdutosApplicationTests

**Arquivo:** `src/test/java/umc/devapp/crud_produtos/CrudProdutosApplicationTests.java`

| Teste | Descricao | Resultado esperado |
|-------|-----------|-------------------|
| `contextLoads` | Contexto Spring sobe com perfil de teste | PASS |

---

## 3. Matriz requisito × teste

| Requisito / controle | Teste que cobre |
|---------------------|-----------------|
| Hash seguro de senha (BCrypt) | `PasswordServiceTest` |
| Salt por hash | `shouldGenerateDifferentHashes...` |
| Custo BCrypt 12 | `AuthSecurityPropertiesTest` |
| Cifra TOTP em repouso | `TotpSecretConverterTest.roundTrip...` |
| Brute force (5 / 15 min) | `AuthSecurityPropertiesTest` (parametros) |
| App inicializa | `contextLoads` |

**Lacunas (sem teste automatizado hoje):**

- Fluxo HTTP completo de login + 2FA
- Bloqueio 423 apos 5 tentativas
- Isolamento de produtos entre usuarios
- Reset de senha (token expirado / invalido)
- Redirect HTTPS 8080 → 8443

---

## 4. Testes manuais recomendados

Use **HTTPS** em ambiente local: `https://localhost:8443`

### 4.1 Autenticacao e 2FA

| # | Cenario | Passos | Resultado esperado |
|---|---------|--------|-------------------|
| M1 | Login senha invalida | POST `/auth/login` com senha errada | 401 |
| M2 | Bloqueio por forca bruta | 5+ falhas de senha ou TOTP | 423 LOCKED |
| M3 | Login sem 2FA | Login OK, GET `/produto/listall` sem verify | 401 |
| M4 | Fluxo completo | login → 2fa/verify → listall | 200 + lista |
| M5 | Logout | POST `/auth/signout`, depois listall | 401 |

### 4.2 Autorizacao de dados

| # | Cenario | Resultado esperado |
|---|---------|-------------------|
| M6 | Usuario A cria produto | 201 |
| M7 | Usuario B tenta GET `/produto/list/{id}` do A | 404 ou erro |

### 4.3 Reset de senha

| # | Cenario | Resultado esperado |
|---|---------|-------------------|
| M8 | Request com e-mail inexistente | 404 |
| M9 | Confirm com token invalido | 400 |
| M10 | Confirm apos 30 min | 400 (expirado) |
| M11 | Confirm com senha valida | 200; login com nova senha OK |

### 4.4 Transporte

| # | Cenario | Resultado esperado |
|---|---------|-------------------|
| M12 | GET `http://localhost:8080/login.html` | Redirect para HTTPS 8443 |
| M13 | Inspecionar cookie apos login | `HttpOnly`, `Secure` (com SSL) |

---

## 5. Registro de execucao (template)

Preencha apos rodar `mvn test`:

| Campo | Valor |
|-------|-------|
| Data | _DD/MM/AAAA_ |
| Ambiente | OS / Java 21 / Maven _X_ |
| Comando | `mvn test` |
| Total de testes | _N_ |
| Falhas | _0_ |
| Erros | _0_ |
| Tempo | _Xs_ |
| Responsavel | _Nome_ |

**Saida esperada (resumo):**

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

*(Ajuste o total conforme a contagem real do Maven.)*

---

## 6. Evidencias de runtime (logs)

Classes que registram eventos de seguranca:

| Classe | Eventos logados |
|--------|-----------------|
| `AuthService` | Login, falha, 2FA, logout |
| `BruteForceProtectionService` | (via AuthService) tentativas e bloqueio |

Exemplos esperados nos logs:

- `Tentativa de login para username=...`
- `Falha de login por senha invalida`
- `Tentativa de login em conta bloqueada`
- `2FA validado com sucesso`
- `Logout realizado com sucesso`

---

## 7. Como adicionar novos testes

Sugestoes para cobrir lacunas:

1. **`@WebMvcTest(AuthController.class)`** — status HTTP sem subir BD completo.
2. **`@SpringBootTest` + MockMvc** — fluxo login com H2 e usuario fixture.
3. **Testcontainers MySQL** — validar `TotpSecretConverter` contra BD real.

---

## Arquivos relacionados

- [security-auth-flow.md](./security-auth-flow.md) — fluxo e comportamentos esperados
- [cryptography.md](./cryptography.md) — algoritmos testados
- [risk-analysis.md](./risk-analysis.md) — riscos e contramedidas
