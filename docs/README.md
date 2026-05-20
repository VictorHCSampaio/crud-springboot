# Documentacao — CRUD Produtos

Indice da documentacao tecnica e de seguranca do backend Spring Boot.

| Documento | Conteudo |
|-----------|----------|
| [security-auth-flow.md](./security-auth-flow.md) | Login em duas etapas, 2FA TOTP, sessao, brute force |
| [password-reset.md](./password-reset.md) | Recuperacao de senha por e-mail e token |
| [cryptography.md](./cryptography.md) | HTTPS, BCrypt, AES-256-GCM, TOTP, SMTP |
| [security-controls.md](./security-controls.md) | Arquitetura, credenciais, ativos, endpoints |
| [security-tests.md](./security-tests.md) | Testes automatizados e roteiro manual |
| [risk-analysis.md](./risk-analysis.md) | Ameacas, vulnerabilidades, risco × contramedida |

## Mapa para documento academico (secao 6.x)

| Secao ABNT | Arquivo principal |
|------------|-------------------|
| 6.2 Diagrama de arquitetura | [security-controls.md](./security-controls.md) |
| 6.3 Fluxos de autenticacao e dados | [security-auth-flow.md](./security-auth-flow.md), [password-reset.md](./password-reset.md) |
| 6.4 Gestao de credenciais | [security-controls.md](./security-controls.md) |
| 6.5 Criptografia | [cryptography.md](./cryptography.md) |
| 6.6 Ativos do sistema | [security-controls.md](./security-controls.md) §6 |
| 6.7 Ameacas e vulnerabilidades | [risk-analysis.md](./risk-analysis.md) §3 |
| 6.8 Risco × contramedida | [risk-analysis.md](./risk-analysis.md) §4 |
| 6.9–6.10 Testes de seguranca | [security-tests.md](./security-tests.md) |
