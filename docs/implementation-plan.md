# FlowPay - Plano de Implementação (Roadmap)

Este documento registra o roadmap do projeto e seu progresso atual.

## BACKEND (Spring Boot) - STATUS: FINALIZADO ✅

### Fase 1: Domínio e Entidades Principais ✅
- [x] Setup do projeto Spring Boot e arquitetura de pastas (Ports & Adapters).
- [x] Configuração Docker (Postgres, Redis).
- [x] Definição de Entidades Ricas (Agent, ChatSession, Team, QueueEntry).
- [x] Value Objects, Enums, Exceptions e proteção de Invariantes (ex: max 3 chats).

### Fase 2 e 3: Portas e Casos de Uso (Application Layer) ✅
- [x] Definição de Input Ports (`RouteChatUseCase`, `DashboardQueryUseCase`).
- [x] Definição de Output Ports (`AgentRepositoryPort`, `EventPublisherPort`, etc).
- [x] DTOs de Request/Response usando Java Records.
- [x] `RoutingEngineService`: Lógica do loop de distribuição e lock distribuído.
- [x] `ChatLifecycleService` e `DashboardMetricsService`.

### Fase 4 e 5: Infraestrutura (DB, Cache e Adapters) ✅
- [x] Flyway Migrations (Schema).
- [x] Entidades JPA (`@Entity`) e Mappers (Entity <-> Domain).
- [x] Repositórios Spring Data com Native Queries (`FOR UPDATE SKIP LOCKED`, `@Lock(PESSIMISTIC_WRITE)`).
- [x] Implementação dos Adaptadores de Saída de Banco (PostgreSQL).
- [x] Implementação dos Adaptadores de Lock e Pub/Sub (Redisson).

### Fase 6: Camada de Entrada e API (Web Adapters) ✅
- [x] Tratamento Global de Exceções (`@RestControllerAdvice`).
- [x] Controllers REST (Agents, Teams, Chats, Dashboard).
- [x] Webhook Controller (Integração WhatsApp/Meta).
- [x] SSE Controller (Dashboard em tempo real via Redis).

---

## FRONTEND (React) - STATUS: PRONTO PARA INICIAR 🚀

### Fase 7: Setup Inicial do Frontend
- [ ] Inicialização via Vite (React + TypeScript).
- [ ] Instalação e configuração do Tailwind CSS.
- [ ] Estruturação de pastas (Component-Based) e Roteamento.
- [ ] Conexão HTTP (Axios) e captura da stream SSE (EventSource).

### Fase 8: Desenvolvimento da UI/UX (Dashboard)
- [ ] Layout Base (Sidebar, Header, Glassmorphism, Dark/Light theme).
- [ ] Painel de Métricas Globais (Cards de SLA, Ocupação, TME).
- [ ] Visualização Dinâmica das Filas.
- [ ] Lista de Agentes e status em tempo real.

### Fase 9: Refinamento e CI/CD
- [ ] Mock/Seed data integration tests.
- [ ] Configuração do Deploy automático para Vercel.
- [ ] Entrega final.
