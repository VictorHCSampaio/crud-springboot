# Sistema de Recuperação de Senha

Este documento descreve como usar o sistema de recuperação de senha implementado no projeto.

## Endpoints

### 1. Solicitar Recuperação de Senha

**Endpoint:** `POST /auth/password-reset/request`

**Descrição:** Inicia o processo de recuperação de senha gerando um token único.

**Request:**
```json
{
  "email": "usuario@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "Email de recuperação enviado. Use o token para resetar a senha.",
  "resetToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Erros:**
- `404 Not Found`: Se o email não estiver cadastrado no sistema.

---

### 2. Confirmar Recuperação de Senha

**Endpoint:** `POST /auth/password-reset/confirm`

**Descrição:** Valida o token e redefine a senha do usuário.

**Request:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "novaSenha123"
}
```

**Response (200 OK):**
```json
{
  "message": "Senha resetada com sucesso. Faça login com sua nova senha."
}
```

**Erros:**
- `400 Bad Request`: Se o token for inválido ou estiver expirado.
- `400 Bad Request`: Se a senha tiver menos de 6 caracteres.

---

## Fluxo de Funcionamento

### Etapa 1: Usuário solicita recuperação
1. Usuário acessa a página de "Esqueci a Senha"
2. Insere seu email cadastrado
3. Clica em "Recuperar Senha"
4. Frontend envia POST para `/auth/password-reset/request` com o email
5. Backend retorna um token (válido por 30 minutos)

### Etapa 2: Usuário recebe o token
- **Em produção com email:** Um email seria enviado ao usuário com um link contendo o token
- **Atual (desenvolvimento):** O token é retornado no response, para testes

### Etapa 3: Usuário confirma nova senha
1. Usuário acessa o link ou cola o token
2. Insere a nova senha
3. Clica em "Resetar Senha"
4. Frontend envia POST para `/auth/password-reset/confirm` com token e nova senha
5. Backend valida o token, hash da nova senha e salva
6. Token é invalidado (removido do banco de dados)

---

## Detalhes Técnicos

### Campos Adicionados à Entidade Usuario
- `password_reset_token` (VARCHAR): Token único gerado para reset
- `password_reset_token_expiry` (DATETIME): Data/hora de expiração do token

### Validade do Token
- **Duração:** 30 minutos (configurável em `PasswordResetService.TOKEN_EXPIRY_MINUTES`)

### Segurança
- Token é um UUID único (praticamente impossível de adivinhar)
- Token expira após 30 minutos
- Token é apagado após uso bem-sucedido
- Senha é criptografada com BCrypt (strength=12)

---

## Integração com Email (Futuro)

Atualmente, o sistema **não envia emails**. Para integrar com um serviço de email (ex.: Gmail, SendGrid):

1. Adicione uma dependência no `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-mail</artifactId>
   </dependency>
   ```

2. Configure em `application.properties`:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=seu-email@gmail.com
   spring.mail.password=sua-senha-app
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

3. Crie um serviço `EmailService` para enviar emails com o token

4. Chame `emailService.sendPasswordResetEmail(usuario.getEmail(), token)` em `PasswordResetService.generatePasswordResetToken()`

---

## Testes com cURL

### Solicitar Reset
```bash
curl -X POST http://localhost:8080/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@example.com"}'
```

### Confirmar Reset
```bash
curl -X POST http://localhost:8080/auth/password-reset/confirm \
  -H "Content-Type: application/json" \
  -d '{"token":"seu-token-aqui","newPassword":"novaSenha123"}'
```

---

## Estrutura de Arquivos Criados

```
src/main/java/umc/devapp/crud_produtos/
├── dto/auth/
│   ├── PasswordResetRequest.java (novo)
│   ├── ConfirmPasswordResetRequest.java (novo)
│   └── PasswordResetResponse.java (novo)
├── service/
│   └── PasswordResetService.java (novo)
└── entity/
    └── Usuario.java (modificado - adicionados campos de reset)
```

---

## Próximos Passos

1. **Integração com Email:** Implemente o envio de emails com o link de reset
2. **Frontend:** Crie páginas de "Esqueci a Senha" e "Resetar Senha"
3. **Segurança Adicional:** Considere adicionar rate limiting para requisições de reset
4. **Auditoria:** Log de tentativas de reset de senha

