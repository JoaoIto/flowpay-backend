# FlowPay — Motor de Distribuição Omnichannel 🚀

> Um desafio técnico de nível Sênior/Especialista desenvolvido para a **Ubots**. Focado em Arquitetura Hexagonal, controle extremo de concorrência e FinOps (Custo Zero).

O **FlowPay** é um simulador de SaaS conversacional projetado para instituições financeiras. Seu núcleo é um motor de roteamento de alta performance capaz de capturar mensagens (via Webhooks do WhatsApp/Meta), enfileirá-las e distribuí-las de forma justa entre atendentes disponíveis, respeitando rígidos limites de capacidade e SLAs.

---

## 📌 Sumário Rápido
- **[📖 Swagger (API Docs Online)](https://flowpay-backend-1s3f.onrender.com/swagger-ui/index.html)**
- **[🔗 Interface Frontend (Deploy)](#)** `[Em breve/Vercel]`
- **[🚀 Como Executar Localmente](#-como-executar-localmente)**
- **[📐 Arquitetura & IA (Gemini)](docs/architecture.md)**
- **[🧠 Decisões de Design (ADRs)](docs/decisions.md)**
- **[🗺️ Plano de Implementação Completo](docs/implementation-plan.md)**
- **[🚀 Plano do Desafio & Setup](docs/plano-FlowPay-java-springboot-react.md)**
- **[🔍 Pesquisa & Entendimento do Domínio](docs/research.md)**

---

## 🏗️ Arquitetura e Padrões de Projeto

O projeto foi inteiramente desenhado utilizando **Hexagonal Architecture (Ports and Adapters)**. O domínio do negócio é o coração do sistema, totalmente isolado de frameworks e tecnologias externas.

*   **Domínio Puro (Zero Leaks):** Nenhuma anotação do Spring Boot, JPA ou Jackson contamina as regras de negócio. O limite de 3 chats por agente é garantido internamente por invariantes matemáticas.
*   **Orquestração via Use Cases:** Serviços focados (ex: `RoutingEngineService`) executam operações atômicas conversando com as Portas (In/Out).
*   **CQRS Lite & Records:** Utilização de Java Records para DTOs imutáveis, separando claramente comandos de intenção (`RouteChatCommand`) e projeções de leitura (`DashboardSnapshot`).

---

## ⚡ Maestria em Concorrência (Lock Híbrido)

O maior desafio de motores de roteamento é evitar *race conditions* (dois agentes puxando o mesmo chat, ou um agente recebendo chats além do limite). Resolvemos isso com uma abordagem de **Lock Híbrido**:

1.  **Redisson Distributed Lock:** Quando uma nova mensagem entra, tentamos adquirir um lock no Redis para o time específico (`flowpay:lock:routing:team:{id}`). Se adquirido, rodamos a distribuição. Se não, ignoramos (outro pod está rodando).
2.  **PostgreSQL `SKIP LOCKED`:** Para garantir vazão máxima na fila (FIFO), o motor busca o próximo chat usando `SELECT ... FOR UPDATE SKIP LOCKED`. Múltiplas instâncias do motor não ficarão travadas esperando umas as outras na tabela de fila.
3.  **JPA Pessimistic Write:** No momento da atribuição, o agente é pinçado com um lock de linha estrito (`@Lock(LockModeType.PESSIMISTIC_WRITE)`), garantindo que seu contador de `activeChatsCount` não sofra regressão matemática sob estresse extremo.

---

## 📡 Comunicação em Tempo Real (SSE)

Em vez de sobrecarregar o banco de dados com polling ou consumir recursos de rede com WebSockets bidirecionais (overkill), optamos por:

*   **Redis Pub/Sub:** O domínio emite eventos (`ChatAssigned`, `ChatQueued`) que são publicados em um canal do Redis (`flowpay:pubsub:dashboard`).
*   **Server-Sent Events (SSE):** O `SseDashboardController` consome esses eventos e os empurra via HTTP unidirecional para o Frontend (React). O dashboard atualiza em milissegundos sem estressar a base de dados relacional.

---

## ☁️ Zero-Cost Cloud Strategy (FinOps)

Projetado para operar em **Free Tiers**, mas mantendo arquitetura corporativa:
*   **Frontend:** Vercel (Hobby Tier) servindo os estáticos e conectando via API.
*   **Backend:** Instância AWS EC2 `t2.micro`.
*   **Database:** AWS RDS PostgreSQL `db.t3.micro`.
*   **Cache:** Redis containerizado direto na EC2 (redução de overhead de ElastiCache).

---

## 🛠️ Tecnologias Utilizadas

*   **Backend:** Java 21, Spring Boot 3.3.7, Spring Data JPA, Flyway.
*   **Infra:** PostgreSQL, Redis (Redisson Client).
*   **Arquitetura:** Clean Architecture / Hexagonal, DDD, SSE, Webhooks.
*   **Frontend (Próxima Fase):** React, Vite, Tailwind CSS.

---

## 🚀 Como Executar Localmente

Você precisará do **Docker** e do **Maven** instalados. O banco de dados e o cache estão dockerizados no projeto.

1.  **Suba a infraestrutura (Postgres + Redis):**
    ```bash
    cd flowpay-backend
    docker-compose up -d
    ```
2.  **Compile e execute a aplicação Spring Boot:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    *A aplicação rodará na porta `8080`.*
    *O Flyway rodará as migrations automaticamente no *startup*.*
3. **Acesse o Endpoints Base:** `http://localhost:8080/api/v1/dashboard`

