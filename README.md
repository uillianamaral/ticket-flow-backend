# Backend — TicketFlow (Spring Boot 3 + PostgreSQL + Flyway)

O backend do TicketFlow é uma API corporativa desenvolvida com **Spring Boot 3.x** e **Java 21**, projetada para alta integridade de dados, transações ACID, controle rigoroso de concorrência e conformidade de segurança.

---

## 🏛️ Organização por Funcionalidade (Package-by-Feature)

A aplicação reside no pacote base `com.ticketflow` e é estruturada por domínio funcional:

```text
com.ticketflow/
├── auth/                               # Login, sessão HttpOnly, UserDetailsService, CookieHandler
├── users/                              # Gestão de usuários, perfis (Solicitante, Atendente, Admin)
├── teams/                              # Gestão de equipes técnicas e associação de membros
├── tickets/                            # Abertura, ciclo de vida, comentários, anexos e auditoria
├── sla/                                # Motor de cálculo de prazos de 1ª resposta e resolução (24/7)
├── notifications/                      # Sistema de avisos do usuário e deduplicação de alertas
└── common/                             # Tratamento global de exceções, DTOs utilitários e paginação
```

### Subcamadas Padronizadas em Cada Funcionalidade

- `controllers/`: Exposição dos endpoints REST, anotações de validação (`@Valid`), autorização (`@PreAuthorize`).
- `services/`: Regras de negócio do domínio, orquestração de transações (`@Transactional`) e invariantes.
- `repositories/`: Interfaces Spring Data JPA com consultas tipadas e índices otimizados.
- `entities/`: Classes JPA anotadas com `@Entity`, `@Table` e `@Version` para controle de concorrência.
- `dtos/`: Java Records imutáveis para transporte limpo de dados, garantindo que entidades do banco não vazem para a API.

---

## 🗄️ Versionamento de Banco de Dados com Flyway

Todas as alterações na estrutura do PostgreSQL são controladas por migrações versionadas em `src/main/resources/db/migration/`:

```text
db/migration/
├── V1__init_schema.sql                 # Criação das tabelas base, chaves primárias, estrangeiras e índices
├── V2__seed_admin_and_categories.sql   # Criação do usuário admin inicial, equipes e categorias padrão
└── V3__create_sla_policies.sql         # Criação das políticas de SLA iniciais por prioridade
```

> **Atenção**: Nunca altere um script de migração já aplicado. Crie um novo arquivo incremental (`V4__...sql`).

---

## 🛡️ Segurança e Autenticação

1. **Sessão Segura**:
   - Autenticação com sessão gerenciada e entrega de cookie `TICKETFLOW_SESSION` com `HttpOnly=true`, `SameSite=Strict` e `Secure=true` (em perfil de produção).
   - Senhas criptografadas utilizando `BCryptPasswordEncoder` com custo 12.
2. **Proteção CSRF**:
   - `CsrfTokenRepository` configurado para gerar o token no cookie `XSRF-TOKEN` e exigir validação no header `X-XSRF-TOKEN`.
3. **Controle de Acesso Fino (Row-Level Security)**:
   - Os serviços filtram chamados e consultas com base no usuário autenticado no `SecurityContextHolder`.
   - As notas internas (`is_internal = true`) são excluídas em nível de repositório/serviço para usuários com perfil "SOLICITANTE".
4. **Controle de Concorrência**:
   - A entidade `Ticket` possui o atributo `@Version private Integer version`.
   - Requisições concorrentes de atendentes tentando assumir o mesmo chamado geram `OptimisticLockingFailureException`, convertida em HTTP 409 Conflict amigável pelo `GlobalExceptionHandler`.

---

## 🧪 Estratégia de Testes e Testcontainers

- **Testes Unitários**: Testam serviços e validadores isolados utilizando JUnit 5 e Mockito.
- **Testes de Integração**: Utilizam a integração oficial do Spring Boot com **Testcontainers**, iniciando uma instância real do PostgreSQL em contêiner Docker para executar as migrações Flyway e validar transações reais sem depender de mocks.

---

## 💻 Comandos de Execução Local

```bash
# Iniciar o banco de dados PostgreSQL via Docker Compose
docker compose up -d postgres

# Executar a aplicação com Spring Boot (porta 8080)
./mvnw spring-boot:run

# Executar testes unitários e de integração com Testcontainers
./mvnw test

# Executar verificação completa com build de pacote JAR
./mvnw verify
```
