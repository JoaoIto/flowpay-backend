# FlowPay — Base de Pesquisa e Contexto de Negócio

Este documento compila o embasamento teórico, regras de negócio e conceitos de Engenharia de Filas aplicados no desenvolvimento da plataforma **FlowPay**.

---

## 1. Contexto da Empresa (Ubots) & Desafio Técnico

O **FlowPay** foi idealizado no contexto do processo seletivo para a vaga de **Desenvolvedor(a) Sênior Fullstack** na **Ubots**. A Ubots é uma empresa especializada em plataformas SaaS de atendimento conversacional, inteligência artificial e relacionamento omnichannel.

### O Desafio
Instituições financeiras e fintechs enfrentam um grande volume diário de contatos via canais digitais (como WhatsApp, Webchat e aplicativos móveis). O desafio central não reside apenas em responder às mensagens, mas em **distribuir o atendimento de forma eficiente e justa**, garantindo que:
* NENHUM atendente fique sobrecarregado acima de sua capacidade operacional.
* NENHUM cliente aguarde indefinidamente sem atendimento ou atualização de fila.
* Todas as métricas de tempo e SLAs (Service Level Agreements) fiquem visíveis aos supervisores em tempo real.

---

## 2. Regras de Negócio & Invariantes de Domínio

O core do FlowPay opera segundo regras rígidas que garantem estabilidade operacional e alocação justa de recursos:

### A. Estrutura de Times Específicos
O sistema é configurado com 3 times de atendimento especializados:
1. **Time de Cartões:** Atendimento dedicado a faturas, bloqueio, segunda via e limites.
2. **Time de Empréstimos:** Dúvidas e negociações de crédito pessoal, consignado e financiamentos.
3. **Time de Outros Assuntos:** Demandas gerais, suporte técnico e informações institucionais.

### B. Limite Estrito de Capacidade (Capacity Limit)
* **Invariante 1:** Cada agente possui uma capacidade máxima estrita de **3 chats simultâneos ativos**.
* **Invariante 2:** Um chat só pode ser atribuído a um agente se o status do agente for `ONLINE` e sua contagem de chats ativos for `< 3`.
* **Invariante 3:** Se a capacidade do agente atingir 3, o sistema rejeita novas atribuições automáticas para este agente até que um dos chats atuais seja finalizado (`RESOLVED` ou `CLOSED`).

### C. Fila FIFO (First-In, First-Out)
* Quando todos os agentes de um determinado time estiverem com capacidade esgotada (3/3), o novo chamado é colocado na **Fila de Espera FIFO** associada àquele time.
* O posicionamento na fila é rigorosamente determinado pelo *timestamp* de chegada (`created_at`).

### D. Re-roteamento e Distribuição Automática
* Assim que qualquer agente de um time encerra um chamado ou altera seu status para `ONLINE`, o motor de roteamento (*Routing Engine*) consulta a Fila FIFO do time correspondente e desempilha o chamado mais antigo para atribuição imediata.

---

## 3. Teoria de Filas & Métricas de Atendimento

O cálculo e a monitoria das métricas operacionais do FlowPay baseiam-se em modelos matemáticos de Teoria de Filas (especialmente sistemas de filas $M/M/c$).

### Formulário de Métricas Chave

#### 1. Tempo Médio de Espera (TME / ASA — Average Speed of Answer)
Mede o intervalo de tempo entre o momento em que o cliente entra na fila (`QUEUED`) e o momento em que é atribuído a um agente (`ACTIVE`).

$$\text{TME} = \frac{\sum_{i=1}^{N} (t_{\text{atribuição}, i} - t_{\text{chegada}, i})}{N}$$

*Onde $N$ é o total de chamados atendidos no período.*

#### 2. Taxa de Ocupação dos Agentes (TO / Utilization Rate)
Representa a porcentagem da capacidade total instalada que está sendo efetivamente utilizada pelos agentes em um determinado instante ou janela de tempo.

$$\text{TO} = \left( \frac{\sum \text{Chats Ativos Atuais}}{\text{Total de Agentes Online} \times 3} \right) \times 100\%$$

#### 3. Service Level Agreement (SLA ≤ 20 segundos)
Indicador percentual de chamados cujo tempo de espera na fila foi menor ou igual à meta estabelecida (20 segundos).

$$\text{SLA (\%)} = \left( \frac{\text{Quantidade de Chats com TME} \le 20\text{s}}{\text{Total de Chats Entrados na Fila}} \right) \times 100\%$$

#### 4. Taxa de Abandono (Abandonment Rate)
Percentual de clientes que encerraram a sessão antes de receberem atendimento de um agente humano.

$$\text{Taxa de Abandono (\%)} = \left( \frac{\text{Total de Chats Cancelados pelo Cliente na Fila}}{\text{Total de Chats Recebidos}} \right) \times 100\%$$

---

## 4. Referências Bibliográficas e Documentações de Apoio

1. **Baeldung — JPA Pessimistic Locking:**
   * *Referência:* Guias práticos sobre o uso de `LockModeType.PESSIMISTIC_WRITE` e bloqueio de registros em JPA/Hibernate.
   * *URL:* [https://www.baeldung.com/jpa-pessimistic-locking](https://www.baeldung.com/jpa-pessimistic-locking)
2. **Redisson Documentation — Distributed Locks & Redis Patterns:**
   * *Referência:* Documentação oficial de implementação do `RLock`, renovação de lease time (Watchdog) e tratamento de falhas em clusters Redis.
   * *URL:* [https://redisson.org/docs/data-access/locks-and-synchronizers/](https://redisson.org/docs/data-access/locks-and-synchronizers/)
3. **Spring Framework Reference — Server-Sent Events (SSE):**
   * *Referência:* Guias da documentação Spring Boot sobre a classe `SseEmitter`, controle de timeout e tratamento assíncrono HTTP.
   * *URL:* [https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)
4. **Meta WhatsApp Business API Documentation — Cloud API & Webhooks:**
   * *Referência:* Especificações oficiais de payloads de entrada de mensagens e verificação de tokens de webhook.
   * *URL:* [https://developers.facebook.com/docs/whatsapp/cloud-api/webhooks/components](https://developers.facebook.com/docs/whatsapp/cloud-api/webhooks/components)
