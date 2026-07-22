# Architecture Decision Records (ADRs)

Este documento registra as decisões de arquitetura e tecnologia tomadas no FlowPay, juntamente com seu contexto e consequências.

## ADR 1: Adoção de Hexagonal Architecture e Domínio Puro
**Contexto:** O sistema de roteamento é complexo e sofrerá mudanças (ex: roteamento por skills no futuro).
**Decisão:** Adotada Arquitetura Hexagonal. A camada de domínio será puramente Java (sem `@Entity` ou dependências do Spring).
**Consequências:** 
- (+) Testes unitários do domínio são extremamente rápidos.
- (+) O banco de dados (Postgres/Redis) é apenas um detalhe de implementação.
- (-) Requer criação de classes de DTOs, Mappers e Entidades duplicadas, aumentando a verbosidade do código.

## ADR 2: Uso de SKIP LOCKED no PostgreSQL
**Contexto:** Ao tentar buscar o próximo chat na fila (`queue_entries`), se múltiplas instâncias rodarem a query simultaneamente, elas baterão na mesma linha (Head-of-line blocking).
**Decisão:** Utilizar native query `SELECT ... FOR UPDATE SKIP LOCKED`.
**Consequências:**
- (+) Aumenta radicalmente a vazão (throughput) de distribuição de chats.
- (+) O banco de dados resolve o problema de concorrência na fila nativamente.
- (-) Prende a aplicação a bancos relacionais modernos (PostgreSQL 9.5+, MySQL 8+).

## ADR 3: Lock Híbrido com Redisson (Distributed Lock)
**Contexto:** Quando vários webhooks entram simultaneamente para o mesmo time, precisamos disparar o motor de distribuição sem que ele conflite.
**Decisão:** Usar `RedissonClient` para adquirir um `RLock` por `teamId` antes de iterar a fila e buscar agentes.
**Consequências:**
- (+) Impede processamento duplicado pesado por diferentes nós do cluster.
- (+) O lease time automático impede deadlocks caso a aplicação crashe segurando o lock.

## ADR 4: Server-Sent Events (SSE) ao invés de WebSockets
**Contexto:** O Dashboard do React precisa de atualizações em tempo real (ex: "Novo chat na fila").
**Decisão:** Adotar SSE (Server-Sent Events) injetados via Redis Pub/Sub, descartando WebSockets.
**Consequências:**
- (+) Fluxo de dados é unidirecional (Servidor -> Cliente), perfeito para Dashboards.
- (+) Protocolo HTTP padrão, sem dor de cabeça com *Load Balancers* cortando conexão WS.
- (-) O cliente não pode "falar" de volta pela mesma conexão (fará requisições REST normais).

## ADR 5: Java Records para DTOs (CQRS Lite)
**Contexto:** Precisamos transportar dados entre os Controllers e os Use Cases sem risco de mutação.
**Decisão:** Usar Java 14+ Records (`public record RouteChatCommand(...)`).
**Consequências:**
- (+) Imutabilidade nativa e código conciso.
- (+) Fácil validação de contratos no próprio construtor compacto do Record.

## ADR 6: Zero-Cost Cloud Strategy (FinOps)
**Contexto:** O desafio deve operar em um ambiente 100% gratuito (Free Tier) com viabilidade corporativa.
**Decisão:** Backend em EC2 `t2.micro` com Docker Compose interno (App + Redis) conectando a um RDS PostgreSQL `t3.micro`. Frontend estático na Vercel (Hobby).
**Consequências:**
- (+) $0 de custo na prova de conceito.
- (+) Pelo container do Redis estar na EC2, economiza-se a contratação de um ElastiCache (que não tem free tier vitalício na AWS).
