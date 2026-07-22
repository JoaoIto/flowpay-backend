### Plano de Engenharia e Arquitetura de Software: FlowPay

Este documento estabelece as diretrizes de engenharia para a plataforma FlowPay, um ecossistema SaaS conversacional corporativo de alta disponibilidade. O sistema foi projetado para sustentar operações de missão crítica em fintechs, onde a integridade transacional e a resiliência sob carga extrema são inegociáveis.

##### 1\. Visão Geral e Estratégia Omnichannel

O FlowPay atua como uma camada de inteligência conversacional que processa eventos em tempo real provenientes de gateways externos, especificamente a API do WhatsApp Business (Meta). Para mitigar os riscos de saturação de I/O e garantir a responsividade, adotamos uma arquitetura orientada a eventos com ingestão assíncrona.**Objetivos Estratégicos:**

* **Ingestão de Alta Volumetria:**  Capacidade de absorver surtos de mensagens (picos de tráfego) sem degradação da latência percebida pelo gateway.  
* **Estabilidade do Ecossistema:**  Proteção contra o efeito de "thundering herd" (avalanche), utilizando amortecimento por filas persistentes para evitar que retentativas automáticas da Meta sobrecarreguem o backend.  
* **Separação de Preocupações:**  Isolamento físico e lógico entre a borda de recepção (Recepção) e o motor de regras de negócio (Processamento).

##### 2\. Arquitetura de Recepção de Webhooks da Meta

A integração com a Meta exige uma janela de resposta extremamente agressiva. Para evitar timeouts que resultariam em retentativas desnecessárias, implementamos o padrão  **"Receber-Persistir-Confirmar" (Receive-Persist-Acknowledge)** .**Ciclo de Vida de um Webhook (Passo a Passo):**

* **Recepção e Validação de Integridade:**  O endpoint de borda intercepta o POST. A validade é verificada via  **HMAC SHA256**  utilizando o  *App Secret* .  
* *Atenção de Engenharia:*  É imperativo utilizar o  **raw request body**  (corpo bruto da requisição) para o cálculo. O uso de middlewares que fazem o parsing automático do JSON pode alterar a sequência de bytes (especialmente em caracteres Unicode), invalidando a assinatura X-Hub-Signature-256.  
* **Persistência Temporária (Queue):**  O payload validado é inserido imediatamente em uma fila distribuída (Redis Streams), utilizando o ID único da mensagem da Meta como chave de controle para garantir a durabilidade.  
* **Confirmação Imediata (HTTP 200 em \< 100ms):**  O sistema encerra a conexão HTTP com status 200\. Esse limite de 100ms é uma barreira de segurança para evitar que o gateway da Meta interprete latência como falha, o que causaria uma tempestade de retentativas.  
* **Processamento Assíncrono (Workers):**  Operários especializados consomem a fila, aplicando lógica de desduplicação e roteamento de domínio.

##### 3\. Engenharia de Domínio e Clean Architecture

O FlowPay utiliza  **Clean Architecture**  com uma abordagem de  **Vertical Slicing (Feature-based grouping)** . Diferente da estratificação horizontal clássica, o Vertical Slicing agrupa componentes por domínio de negócio, facilitando a migração futura para microserviços independentes.**Camadas e Responsabilidades:**

* **Domínio (Core):**  Contém as Entidades e Invariantes de Negócio. Aqui reside a regra estrita de  **limite máximo de 3 chats simultâneos por agente** .  
* **Aplicação:**  Implementa os Casos de Uso (ex: RouteChat, CloseChat), orquestrando o fluxo entre domínio e adaptadores.  
* **Infraestrutura:**  Adaptadores concretos para PostgreSQL, Redis (Redisson) e mensageria.  
* **Apresentação:**  Controladores REST e provedores de  **Server-Sent Events (SSE)** .**Regra de Transição de Estados Monotônica:**  Para garantir a rastreabilidade financeira e operacional, as sessões de chat seguem um fluxo unidirecional rígido: CREATED \-\> QUEUED \-\> ACTIVE \-\> RESOLVED \-\> CLOSED

##### 4\. Gerenciamento de Concorrência e Persistência

Em ambientes distribuídos, o risco de "race conditions" na alocação de atendentes é mitigado através de uma estratégia de bloqueio duplo.| Mecanismo | Tecnologia | Propósito Principal || \------ | \------ | \------ || **Distributed Lock** | Redis (Redisson) | Sincronizar a alocação de capacidade. Utilizamos  **Redis Hash Tags**  (ex: {agent:123}) para garantir que chaves de lock e dados da sessão residam no mesmo  *hash slot*  do cluster, permitindo operações atômicas. || **Transactional Queue** | PostgreSQL (FOR UPDATE SKIP LOCKED) | Garante o processamento "at-least-once". O uso de SKIP LOCKED é fundamental para que os workers não fiquem bloqueados (hanging) aguardando registros já em processamento por outras instâncias, otimizando o  *throughput* . |

##### 5\. Backend High-Performance: Spring Boot 4 e Java 25

A escolha da JVM (Java 25\) sobre o ecossistema Node.js (NestJS) justifica-se pela robustez computacional necessária para tarefas CPU-heavy e segurança de tipos em tempo de execução.**Análise Comparativa:**

* **Spring Boot 4 (Java 25):**  Com o  **Project Loom (Virtual Threads)**  como padrão, o custo de bloqueio de threads foi eliminado. O sistema lida com milhões de requisições simultâneas sem a complexidade do modelo reativo ("callback hell"). Além disso, o uso de  **Class-Data Sharing (CDS)**  reduz o tempo de  *cold start*  em ambientes de containers.  
* **NestJS:**  Embora eficiente para I/O simples, sofre em cenários de processamento intenso de regras de negócio e carece da maturidade da JVM para gerenciamento de memória em larga escala e multithreading real (não baseado em processos isolados).

##### 6\. Telemetria em Tempo Real e Frontend (React)

Para o monitoramento operacional, o FlowPay utiliza  **Server-Sent Events (SSE)**  sobre HTTP/2. Diferente de WebSockets, o SSE é unidirecional (Servidor \-\> Cliente), o que reduz o consumo de CPU no servidor em até 40% e elimina a necessidade de gestão complexa de  *handshakes*  bidirecionais.**Métricas Operacionais (Fórmulas):**

* **Tempo Médio de Espera (TME):**   $$TME \= \\frac{\\sum\_{i=1}^{N} (T\_{\\text{atendimento\\\_i}} \- T\_{\\text{enfileiramento\\\_i}})}{N}$$  
* **Taxa de Ocupação Geral (TO):**  (Respeitando a invariante de 3 chats/agente)  $$TO \= \\frac{\\sum\_{j=1}^{M} \\text{Chats\\\_Ativos}*j}{3 \\times M*{\\text{ativos}}} \\times 100\\%$$  
* **Tempo Médio de Atendimento (TMA):**   $$TMA \= \\frac{\\sum\_{k=1}^{K} (T\_{\\text{encerramento\\\_k}} \- T\_{\\text{atendimento\\\_k}})}{K}$$**Otimização Frontend (React \+ Zustand):**  
* **Zustand (State Slicing):**  Utilizamos fatiamento de estado para garantir que apenas os componentes que assinam métricas específicas sofram re-renderização, evitando o  *lag*  visual no dashboard.  
* **Event Batching:**  Implementamos um buffer de 300ms para atualizações visuais, mitigando o "main thread jank" durante rajadas volumétricas de mensagens.

##### 7\. Estratégia de Qualidade e Testes de Carga

A validação de resiliência é conduzida via  **k6** . Optamos pelo k6 em vez do Artillery devido ao seu runtime em Go, que permite maior densidade de usuários virtuais (VUs) por máquina e suporte nativo a  *thresholds*  baseados em métricas customizadas.**Exemplo de Script k6 (Simulação de 500 VUs):**  
import http from 'k6/http';  
import { check, sleep } from 'k6';

export const options \= {  
  stages: \[  
    { duration: '30s', target: 500 }, // Ramp-up  
    { duration: '2m', target: 500 },  // Steady-state  
    { duration: '30s', target: 0 },   // Ramp-down  
  \],  
  thresholds: {  
    'http\_req\_duration': \['p(95)\<100'\], // 95% das requisições devem ser sub-100ms  
  },  
};

export default function () {  
  const payload \= JSON.stringify({ id: "wamid.ID", text: { body: "Fluxo de Carga" } });  
  const params \= { headers: { 'Content-Type': 'application/json', 'X-Hub-Signature-256': 'sha256=expected\_hash' } };  
  const res \= http.post('https://api.flowpay.com/webhooks', payload, params);  
  check(res, { 'status is 200': (r) \=\> r.status \=== 200 });  
  sleep(1);  
}

##### 8\. Conclusão e Diferenciais de Engenharia

O FlowPay não é apenas uma interface de mensagens, mas uma plataforma de engenharia robusta. A aplicação de  **idempotência na ingestão**  (via chaves Redis com TTL) assegura que falhas de rede no gateway da Meta não resultem em duplicidade de processamento. Ao isolar o domínio com Clean Architecture e proteger a concorrência com locks distribuídos via Hash Tags, garantimos que as invariantes de negócio permaneçam íntegras sob qualquer carga.**Veredito Técnico:**  A arquitetura baseada em Spring Boot 4 e Java 25, aliada ao processamento assíncrono e telemetria via SSE, posiciona o FlowPay como uma solução de alta performance pronta para os requisitos de escalabilidade e segurança de instituições financeiras modernas.  
