# Documentação — CRUD Produtos

Índice da documentação técnica e de segurança do backend Spring Boot.

| Documento | Conteúdo |
|-----------|----------|
| [security-auth-flow.md](./security-auth-flow.md) | Login em duas etapas, 2FA TOTP, sessão, brute force |
| [password-reset.md](./password-reset.md) | Recuperação de senha por e-mail e token |
| [cryptography.md](./cryptography.md) | HTTPS, BCrypt, AES-256-GCM, TOTP, SMTP |
| [security-controls.md](./security-controls.md) | Arquitetura, credenciais, ativos, endpoints |
| [security-tests.md](./security-tests.md) | Testes automatizados e roteiro manual |
| [risk-analysis.md](./risk-analysis.md) | Ameaças, vulnerabilidades, risco × contramedida |

## Mapa para documento acadêmico (seção 6.x)

| Seção ABNT | Arquivo principal |
|------------|-------------------|
| 6.2 Diagrama de arquitetura | [security-controls.md](./security-controls.md) |
| 6.3 Fluxos de autenticação e dados | [security-auth-flow.md](./security-auth-flow.md), [password-reset.md](./password-reset.md) |
| 6.4 Gestão de credenciais | [security-controls.md](./security-controls.md) |
| 6.5 Criptografia | [cryptography.md](./cryptography.md) |
| 6.6 Ativos do sistema | [security-controls.md](./security-controls.md) §6 |
| 6.7 Ameaças e vulnerabilidades | [risk-analysis.md](./risk-analysis.md) §3 |
| 6.8 Risco × contramedida | [risk-analysis.md](./risk-analysis.md) §4 |
| 6.9–6.10 Testes de segurança | [security-tests.md](./security-tests.md) |
