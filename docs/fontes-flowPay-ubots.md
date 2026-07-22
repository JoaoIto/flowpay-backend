**Blueprint Arquitetural FlowPay: Motor de Distribuição Omnichannel e Dashboard em Tempo Real**  
**Arquitetura de Solução e Padrões (Spring Boot)**  
A estruturação arquitetural do sistema FlowPay baseia-se nos padrões de design mais consolidados para sistemas de missão crítica no setor financeiro. A adoção da Arquitetura Hexagonal, também conhecida como Portas e Adaptadores, estabelece uma separação clara entre as regras de negócio centrais da fintech e os mecanismos de infraestrutura, tais como bancos de dados, servidores web e corretores de mensageria \[cite: 1, 2\]. Ao isolar o núcleo da aplicação, garante-se que mudanças tecnológicas — como a transição de um banco de dados relacional para NoSQL ou a substituição do protocolo de transmissão em tempo real — possam ser efetuadas com impacto mínimo nas regras operacionais \[cite: 1, 2\].  
A regra de dependência dita que todas as dependências apontam para o centro, o que significa que o domínio do sistema não possui conhecimento sobre frameworks, bibliotecas externas ou anotações específicas do Spring Framework \[cite: 1, 2\]. Essa blindagem permite a validação do motor de roteamento por meio de testes de unidade extremamente rápidos, executados de forma totalmente independente do contêiner do Spring \[cite: 1, 2, 3\].  
**Estrutura de Pacotes do Projeto Spring Boot**  
A organização dos diretórios do projeto reflete rigorosamente os limites arquiteturais da Arquitetura Hexagonal, dividindo o ecossistema em três camadas principais: domínio, aplicação e adaptadores \[cite: 2, 4\].  
com.flowpay.routing │ ├── domain/ \# Núcleo de regras de negócio (Independente de Frameworks) │ ├── model/ \# Entidades do modelo rico de domínio │ │ ├── Agent.java │ │ ├── Team.java │ │ ├── Chat.java │ │ ├── QueueEntry.java │ │ └── Status.java │ └── exception/ \# Exceções de negócio de domínio │ └── MaxCapacityExceededException.java │ ├── application/ \# Casos de uso e orquestração do fluxo do sistema │ ├── port/ \# Fronteiras do hexágono (Contratos de comunicação) │ │ ├── in/ \# Portas de Entrada (Inbound Ports / Use Cases) │ │ │ ├── RouteChatUseCase.java │ │ │ └── AgentStateUseCase.java │ │ └── out/ \# Portas de Saída (Outbound Ports / SPI) │ │ ├── AgentRepositoryPort.java │ │ ├── ChatRepositoryPort.java │ │ └── QueueRepositoryPort.java │ └── service/ \# Implementação dos serviços de caso de uso │ ├── RoutingEngineService.java │ └── AgentStateService.java │ └── adapter/ \# Detalhes de implementação tecnológica (Spring, Redisson, JPA) ├── in/ \# Adaptadores de Entrada (Primary Adapters) │ ├── web/ \# Controladores REST e manipuladores de Webhooks │ │ ├── WebhookController.java │ │ └── DashboardStreamController.java │ └── dto/ \# Objetos de Transferência de Dados (Requests/Responses) │ └── MetaWebhookPayload.java └── out/ \# Adaptadores de Saída (Secondary Adapters) ├── persistence/ \# Implementações de acesso ao banco relacional │ ├── jpa/ \# Interfaces Spring Data JPA │ │ ├── JpaAgentRepository.java │ │ ├── JpaChatRepository.java │ │ └── JpaQueueRepository.java │ ├── entity/ \# Entidades mapeadas para o banco de dados │ │ ├── AgentEntity.java │ │ ├── ChatEntity.java │ │ └── TeamEntity.java │ └── JpaAgentRepositoryAdapter.java └── cache/ \# Infraestrutura de persistência rápida e concorrência ├── redis/ \# Adaptadores Redis e Pub/Sub │ ├── RedisPubSubAdapter.java │ └── DistributedLockService.java └── config/ \# Configurações do cliente Redisson └── RedissonConfig.java  
**Motor de Roteamento e Resolução de Condições de Corrida**  
O principal desafio no desenvolvimento do sistema FlowPay é garantir a integridade do limite estrito de capacidade dos atendentes (máximo de 3 atendimentos simultâneos) sob alta concorrência \[cite: 5, 6\]. Quando dois atendentes tornam-se disponíveis no mesmo milissegundo, ou quando uma enxurrada de webhooks é recebida simultaneamente, o sistema está sujeito a condições de corrida do tipo *read-modify-write* e *check-then-act* \[cite: 5\]. Se duas threads lerem simultaneamente que um atendente possui 2 chats ativos, ambas podem atribuir um novo chat a ele, elevando sua carga para 4 atendimentos e violando uma regra de negócio intransponível \[cite: 5, 7\].  
Para solucionar este problema, uma análise comparativa dos mecanismos de concorrência é essencial para embasar a decisão de arquitetura:

| Mecanismo de Concorrência | Latência Típica | Escalabilidade Horizontal | Risco de Contenção / Gargalo | Comportamento sob Falha / Crash |
| ----- | ----- | ----- | ----- | ----- |
| **Java Local Concurrency** (`ReentrantLock`, `ConcurrentHashMap`) \[cite: 5, 8\] | \< 1 ms \[cite: 8\] | Nula (Limitado a uma única JVM) \[cite: 9, 10\] | Baixo na aplicação, mas centraliza o processamento em um único nó \[cite: 9, 11\]. | Sem impacto em outros nós; perda instantânea do estado de lock local \[cite: 9\]. |
| **Database Pessimistic Locking** (`PESSIMISTIC_WRITE`) \[cite: 12, 13\] | 5 \- 20 ms \[cite: 14\] | Excelente (Gerenciado de forma centralizada pelo RDBMS) \[cite: 6, 12\] | Alto sob tráfego intenso; bloqueia conexões do banco de dados \[cite: 6, 14\]. | Transações são revertidas automaticamente pelo banco no rollback \[cite: 12, 15\]. |
| **Distributed Locking via Redis** (`Redisson` / Valkey) \[cite: 11, 16\] | 1 \- 3 ms \[cite: 16, 17\] | Excelente (Consistência distribuída compartilhada) \[cite: 10, 16\] | Baixo; desvia a carga de concorrência da camada de persistência relacional \[cite: 16, 18\]. | Requer mecanismo de expiração automática de chaves (watchdog) para evitar starvation \[cite: 9, 16\]. |

Com base nesta análise, a FlowPay adota um modelo híbrido e resiliente \[cite: 13, 18\]. O controle primário é efetuado através de Locks Distribuídos com a biblioteca Redisson no Redis \[cite: 11, 16\]. Isso evita que múltiplos nós da aplicação tentem manipular os mesmos recursos de roteamento de forma concorrente, garantindo que o fluxo de trabalho permaneça serializado na borda \[cite: 16, 18\]. Como mecanismo de defesa em profundidade e para assegurar consistência transacional absoluta na camada de persistência, utiliza-se o bloqueio pessimista do JPA (`LockModeType.PESSIMISTIC_WRITE`), traduzido na cláusula SQL `SELECT FOR UPDATE` no banco de dados \[cite: 12, 15, 19\].  
Para viabilizar a distribuição imediata e automática assim que um espaço é liberado na agenda de um atendente, o sistema adota um padrão arquitetural baseado em eventos (Event-Driven routing) \[cite: 1\]. Quando um atendimento é encerrado, a camada de aplicação publica um evento local de liberação de capacidade. Esse evento dispara o motor de roteamento para consultar a fila e atribuir imediatamente o chat mais antigo ao atendente recém-liberado.  
Abaixo é apresentada a implementação do motor de roteamento contendo a lógica de lock híbrido e distribuição automática:  
package com.flowpay.routing.application.service;

import com.flowpay.routing.application.port.out.AgentRepositoryPort;  
import com.flowpay.routing.application.port.out.ChatRepositoryPort;  
import com.flowpay.routing.application.port.out.QueueRepositoryPort;  
import com.flowpay.routing.domain.model.Agent;  
import com.flowpay.routing.domain.model.Chat;  
import org.redisson.api.RLock;  
import org.redisson.api.RedissonClient;  
import org.springframework.stereotype.Service;  
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;  
import java.util.concurrent.TimeUnit;

@Service  
public class RoutingEngineService {

    private final RedissonClient redissonClient;  
    private final AgentRepositoryPort agentRepositoryPort;  
    private final ChatRepositoryPort chatRepositoryPort;  
    private final QueueRepositoryPort queueRepositoryPort;

    public RoutingEngineService(RedissonClient redissonClient,  
                                AgentRepositoryPort agentRepositoryPort,  
                                ChatRepositoryPort chatRepositoryPort,  
                                QueueRepositoryPort queueRepositoryPort) {  
        this.redissonClient \= redissonClient;  
        this.agentRepositoryPort \= agentRepositoryPort;  
        this.chatRepositoryPort \= chatRepositoryPort;  
        this.queueRepositoryPort \= queueRepositoryPort;  
    }

    public void dispatchPendingChats(String teamId) {  
        String lockKey \= "lock:routing:team:" \+ teamId;  
        RLock lock \= redissonClient.getLock(lockKey);  
          
        try {  
            // Tenta obter o lock distribuído por 2 segundos, com expiração de 10 segundos  
            if (lock.tryLock(2, 10, TimeUnit.SECONDS)) {  
                try {  
                    executeDistributionLoop(teamId);  
                } finally {  
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {  
                        lock.unlock();  
                    }  
                }  
            }  
        } catch (InterruptedException e) {  
            Thread.currentThread().interrupt();  
            throw new RuntimeException("Processo de distribuição distribuída interrompido", e);  
        }  
    }

    @Transactional  
    protected void executeDistributionLoop(String teamId) {  
        boolean hasQueueAndCapacity \= true;  
          
        while (hasQueueAndCapacity) {  
            Optional\<Chat\> nextChatOpt \= queueRepositoryPort.findOldestWaitingChat(teamId);  
            if (nextChatOpt.isEmpty()) {  
                hasQueueAndCapacity \= false;  
                continue;  
            }

            // O método de repositório aplica PESSIMISTIC\_WRITE para garantir exclusividade na atualização  
            Optional\<Agent\> availableAgentOpt \= agentRepositoryPort.findAvailableAgentWithWriteLock(teamId);  
              
            if (availableAgentOpt.isPresent()) {  
                Agent agent \= availableAgentOpt.get();  
                Chat chat \= nextChatOpt.get();

                agent.assignChat(chat); // Incrementa o contador interno e atualiza estado  
                chat.setStatusActive(agent.getId()); // Altera status para ATIVO

                agentRepositoryPort.save(agent);  
                chatRepositoryPort.save(chat);  
                queueRepositoryPort.remove(chat.getId());  
            } else {  
                hasQueueAndCapacity \= false;  
            }  
        }  
    }  
}

**Modelo de Dados Relacional (ERD)**  
A modelagem do banco de dados relacional é desenhada para suportar alta concorrência e conformidade regulatória bancária, exigindo normalização completa e indexação otimizada. O banco de dados físico de escolha para este sistema é o PostgreSQL \[cite: 14\].  
**Esquema Detalhado das Entidades do Banco de Dados**  
Abaixo estão detalhados os atributos, tipos de dados e restrições de integridade que compõem o modelo relacional de dados da FlowPay:  
Tabela: `teams` (Times de Atendimento)  
Armazena a definição organizacional das áreas de atendimento de suporte omnichannel.

| Atributo | Tipo de Dado | Restrições | Descrição |
| ----- | ----- | ----- | ----- |
| `id` | `UUID` | `PRIMARY KEY` | Identificador único global do time. |
| `name` | `VARCHAR(100)` | `NOT NULL, UNIQUE` | Nome do time ("Cartões", "Empréstimos", "Outros Assuntos"). |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Carimbo de data/hora de inserção do registro. |

Tabela: `agents` (Atendentes)  
Modelagem física dos agentes, contendo a capacidade máxima e o estado de ocupação atual.

| Atributo | Tipo de Dado | Restrições | Descrição |
| ----- | ----- | ----- | ----- |
| `id` | `UUID` | `PRIMARY KEY` | Identificador único global do atendente. |
| `team_id` | `UUID` | `FOREIGN KEY REFERENCES teams(id)` | Identificador do time ao qual o atendente pertence. |
| `name` | `VARCHAR(150)` | `NOT NULL` | Nome completo do atendente. |
| `max_chats` | `INT` | `NOT NULL, DEFAULT 3` | Capacidade máxima de chats simultâneos (padrão rígido de 3). |
| `active_chats_count` | `INT` | `NOT NULL, DEFAULT 0` | Número de chats sob a responsabilidade do atendente. |
| `status` | `VARCHAR(50)` | `NOT NULL` | Estado do agente (`AVAILABLE`, `BREAK`, `LOGGED_OUT`). |

O banco de dados do PostgreSQL deve aplicar uma restrição de validação para impedir em nível físico de armazenamento qualquer violação de integridade por concorrência mal controlada na aplicação:  
ALTER TABLE agents ADD CONSTRAINT chk\_active\_chats\_limit   
CHECK (active\_chats\_count \>= 0 AND active\_chats\_count \<= max\_chats);

Tabela: `chats` (Atendimentos)  
Registra o histórico de interações com os clientes, contendo o fluxo de estados fundamentais.

| Atributo | Tipo de Dado | Restrições | Descrição |
| ----- | ----- | ----- | ----- |
| `id` | `UUID` | `PRIMARY KEY` | Identificador único global da sessão de chat. |
| `team_id` | `UUID` | `FOREIGN KEY REFERENCES teams(id)` | Canal organizacional associado ao atendimento. |
| `agent_id` | `UUID` | `FOREIGN KEY REFERENCES agents(id)` | Atendente atribuído (pode ser nulo se estiver na fila). |
| `customer_id` | `VARCHAR(100)` | `NOT NULL` | Identificador de origem do cliente (ex: telefone WhatsApp). |
| `status` | `VARCHAR(50)` | `NOT NULL` | Estado atual (`AGUARDANDO`, `ATIVO`, `CONCLUIDO`). |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Data e hora em que a sessão foi inicializada pelo cliente. |
| `started_at` | `TIMESTAMP WITH TIME ZONE` | `NULL` | Data e hora da primeira resposta/atribuição do atendente. |
| `ended_at` | `TIMESTAMP WITH TIME ZONE` | `NULL` | Data e hora de conclusão final do atendimento. |

A integridade do estado e o histórico de auditoria operacional do chat contam com índices estrategicamente posicionados no banco de dados para otimização das queries mais frequentes executadas pelo motor de busca:  
CREATE INDEX idx\_chats\_team\_status ON chats (team\_id, status);  
CREATE INDEX idx\_chats\_agent\_status ON chats (agent\_id, status);

Tabela: `queue_entries` (Entradas de Fila)  
Estrutura de dados de suporte à fila de espera, que gerencia os chats que não puderam ser atribuídos imediatamente por limitação de capacidade dos agentes \[cite: 15\].

| Atributo | Tipo de Dado | Restrições | Descrição |
| ----- | ----- | ----- | ----- |
| `id` | `UUID` | `PRIMARY KEY` | Identificador único global da entrada de fila. |
| `chat_id` | `UUID` | `NOT NULL, UNIQUE, FK REFERENCES chats(id)` | Referência direta ao chat que está aguardando vaga \[cite: 15\]. |
| `team_id` | `UUID` | `NOT NULL, FK REFERENCES teams(id)` | Identificador do time associado ao chat para direcionamento \[cite: 15\]. |
| `entered_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Carimbo de data/hora de ingresso na fila (usado para FIFO) \[cite: 15\]. |

A ordenação baseada na lógica First-In-First-Out (FIFO) exige um índice composto específico na tabela de fila para otimizar a velocidade de busca pelo primeiro registro elegível:  
CREATE INDEX idx\_queue\_team\_entered ON queue\_entries (team\_id, entered\_at ASC);

**Implementação do Repositório JPA com Lock Pessimista**  
O repositório do Spring Data JPA estende a interface padrão e inclui a anotação `@Lock` configurada com `LockModeType.PESSIMISTIC_WRITE` \[cite: 14, 19, 20\]. Isso instrui o Hibernate a gerar uma query com o sufixo `FOR UPDATE` no PostgreSQL, forçando a transação a reter o bloqueio exclusivo da linha correspondente até o encerramento da unidade de trabalho transacional atual \[cite: 12, 14\].  
package com.flowpay.routing.adapter.out.persistence.jpa;

import com.flowpay.routing.adapter.out.persistence.entity.AgentEntity;  
import org.springframework.data.jpa.repository.JpaRepository;  
import org.springframework.data.jpa.repository.Lock;  
import org.springframework.data.jpa.repository.Query;  
import org.springframework.data.jpa.repository.QueryHints;  
import org.springframework.data.repository.query.Param;  
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;  
import jakarta.persistence.QueryHint;  
import java.util.Optional;  
import java.util.UUID;

@Repository  
public interface JpaAgentRepository extends JpaRepository\<AgentEntity, UUID\> {

    @Lock(LockModeType.PESSIMISTIC\_WRITE)  
    @QueryHints({@QueryHint(name \= "jakarta.persistence.lock.timeout", value \= "3000")})  
    @Query("SELECT a FROM AgentEntity a " \+  
           "WHERE a.team.id \= :teamId " \+  
           "AND a.activeChatsCount \< a.maxChats " \+  
           "AND a.status \= 'AVAILABLE' " \+  
           "ORDER BY a.activeChatsCount ASC, a.created\_at ASC")  
    Optional\<AgentEntity\> findAvailableAgentWithWriteLock(@Param("teamId") UUID teamId);  
}

**Diagrama de Fluxo e Mensageria (Eventos)**  
A operação em tempo real da FlowPay apoia-se em um pipeline de eventos totalmente assíncrono para garantir o tratamento correto das requisições e a atualização fluida dos dashboards \[cite: 1, 21\].  
**Ciclo de Vida de uma Requisição de Atendimento**  
\+-------------------+ \+-------------------+ \+-------------------+ \+---------------------+  
| Meta Graph API | | FlowPay Webhook | | Ingestion Handler | | Routing Engine | | Inbound Message | \---\> | Controller | \---\> | (Async Broker) | \---\> | (Redis/DB Locks) | \+-------------------+ \+-------------------+ \+-------------------+ \+---------------------+ | v \+-------------------+ \+-------------------+ \+-------------------+ \+---------------------+  
| React Dashboard | | Real-Time Stream | | Redis Pub/Sub | | Persistent Storage | | Update Rendered | \<--- | Controller (SSE) | \<--- | Message Channel | \<--- | (PostgreSQL Engine) | \+-------------------+ \+-------------------+ \+-------------------+ \+---------------------+  
O ciclo de vida inicia-se com o recebimento de uma mensagem do cliente via API do WhatsApp Business \[cite: 22\]. O webhook atua como a barreira de entrada da aplicação, validando os parâmetros antes de propagar as tarefas para o motor de processamento assíncrono \[cite: 23, 24\].

1. **Ingresso do Evento**: O servidor da Meta faz uma requisição HTTP `POST` contendo os dados da mensagem do usuário para o endpoint do Webhook da FlowPay \[cite: 22, 25\].  
2. **Validação Criptográfica e Integridade**: O controlador lê o payload e calcula a assinatura SHA-256 usando o App Secret configurado no Spring Boot, comparando-o em tempo constante com a assinatura recebida no cabeçalho `X-Hub-Signature-256` \[cite: 24, 26\].  
3. **Desacoplamento Assíncrono**: Após a validação, o payload bruto é postado de forma assíncrona em uma fila interna utilizando Redis Queue para liberar imediatamente a requisição da Meta, prevenindo falhas de timeout do servidor parceiro e garantindo o status HTTP `200 OK` em tempo hábil \[cite: 24, 25\].  
4. **Decisão do Roteador**: O consumidor da fila lê a mensagem, decodifica a intenção do cliente, consulta as entidades de negócio e tenta a alocação imediata de um agente livre aplicando o lock pessimista \[cite: 12, 18, 19\].  
5. **Gravação e Auditoria**: O novo estado operacional do chat é persistido no PostgreSQL \[cite: 12, 14\].  
6. **Disparo de Mensageria de Atualização**: Um evento descrevendo o estado atual de ocupação do time é enviado via Redis Pub/Sub, sendo consumido pelas instâncias do backend para atualização imediata dos dashboards \[cite: 10, 21\].

**Simulação e Mock da Meta Graph API (WhatsApp Business)**  
Para garantir integridade durante testes funcionais e homologações, o backend inclui uma interface de simulação que emula as requisições enviadas pelo ecossistema do Meta Graph API \[cite: 22, 23\].  
A API do WhatsApp exige um processo de handshake inicial baseado em um requisição `GET` com parâmetros de consulta específicos (`hub.mode`, `hub.challenge` e `hub.verify_token`) \[cite: 22, 23\]. O controlador valida o token contra o valor configurado na fintech e retorna a string de desafio bruta recebida (`hub.challenge`), sem aspas ou codificações JSON para garantir a aprovação de conformidade exigida pelo ecossistema da Meta \[cite: 22, 25\].  
Abaixo é apresentada a implementação do controlador de verificação e recebimento assíncrono das interações de mensagens recebidas:  
package com.flowpay.routing.adapter.in.web;

import org.apache.commons.codec.digest.HmacAlgorithms;  
import org.apache.commons.codec.digest.HmacUtils;  
import org.springframework.beans.factory.annotation.Value;  
import org.springframework.http.HttpStatus;  
import org.springframework.http.MediaType;  
import org.springframework.http.ResponseEntity;  
import org.springframework.web.bind.annotation.\*;

import java.security.MessageDigest;

@RestController  
@RequestMapping("/api/v1/whatsapp")  
public class WebhookController {

    private final String verifyToken;  
    private final String appSecret;

    public WebhookController(  
            @Value("${meta.whatsapp.verify-token}") String verifyToken,  
            @Value("${meta.whatsapp.app-secret}") String appSecret) {  
        this.verifyToken \= verifyToken;  
        this.appSecret \= appSecret;  
    }

    // Handshake inicial de conformidade exigido pela Meta Graph API  
    @GetMapping(value \= "/webhook", produces \= MediaType.TEXT\_PLAIN\_VALUE)  
    public ResponseEntity\<String\> verifyWebhook(  
            @RequestParam("hub.mode") String mode,  
            @RequestParam("hub.verify\_token") String token,  
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {  
            return ResponseEntity.ok(challenge);  
        }  
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  
    }

    @PostMapping("/webhook")  
    public ResponseEntity\<Void\> receiveInboundMessage(  
            @RequestHeader("X-Hub-Signature-256") String signatureHeader,  
            @RequestBody String payload) {

        if (\!validateSignature(signatureHeader, payload)) {  
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  
        }

        // Delegar para processamento assíncrono interno para responder imediatamente HTTP 200 OK  
        dispatchAsyncToBroker(payload);

        return ResponseEntity.ok().build();  
    }

    private boolean validateSignature(String signatureHeader, String payload) {  
        if (signatureHeader \== null || \!signatureHeader.startsWith("sha256=")) {  
            return false;  
        }

        String actualSignature \= signatureHeader.substring(7);  
        String expectedSignature \= new HmacUtils(HmacAlgorithms.HMAC\_SHA\_256, appSecret)  
                .hmacHex(payload);

        // Uso obrigatório de equals em tempo constante para evitar ataques de timing de canal lateral  
        return MessageDigest.isEqual(actualSignature.getBytes(), expectedSignature.getBytes());  
    }

    private void dispatchAsyncToBroker(String payload) {  
        // Implementação assíncrona de push para o Redis Queue / Broker local  
    }  
}

**Integração Real-Time e Dashboard (Spring \+ React)**  
A atualização dinâmica dos dashboards para tomada de decisões estratégicas requer uma infraestrutura de transmissão de dados de baixa latência e baixo consumo de recursos de rede \[cite: 27, 28\].  
**Comparativo Técnico: WebSockets vs Server-Sent Events (SSE)**  
Para embasar a decisão tecnológica na arquitetura do Dashboard, avalia-se o comportamento de ambos os protocolos em um cenário de alta concorrência:

| Característica de Comparação | WebSockets (STOMP) \[cite: 29, 30\] | Server-Sent Events (SSE) \[cite: 27, 30\] | Raciocínio de Seleção no FlowPay |
| ----- | ----- | ----- | ----- |
| **Padrão de Comunicação** | Bidirecional Completo (Full-Duplex) \[cite: 27, 29\]. | Unidirecional (Apenas do Servidor para o Cliente) \[cite: 28, 29\]. | **SSE**: O dashboard do gestor consome métricas passivamente, sem necessidade de enviar dados pesados continuamente ao backend \[cite: 28, 31\]. |
| **Camada de Transporte** | Upgrade de Protocolo HTTP para protocolo TCP Socket dedicado \[cite: 30, 31\]. | HTTP padrão persistente sobre conexões `text/event-stream` \[cite: 17, 27, 30\]. | **SSE**: Compatibilidade nativa com proxies reversos, balanceadores de carga, redes CDN e firewalls \[cite: 28, 31\]. |
| **Resiliência e Quedas de Rede** | Exige implementação manual e customizada de algoritmos de reconexão no frontend \[cite: 29, 30\]. | Reconexão automática integrada de forma nativa ao objeto `EventSource` no React \[cite: 29, 30, 32\]. | **SSE**: Reduz sensivelmente o volume de código de controle no ecossistema do React \[cite: 30, 32\]. |
| **Consumo de Recursos (RAM)** | Alto. Exige gerenciamento de buffer duplo ativo por conexão de socket \[cite: 30\]. | Extremamente leve. Semoverhead adicional de decodificação de frames de rede \[cite: 28, 30\]. | **SSE**: Permite maior número de visualizadores ativos simultâneos sem sobrecarregar o contêiner do Spring \[cite: 28\]. |

Por esses fatores, a equipe de arquitetura adota o protocolo **Server-Sent Events (SSE)** como a tecnologia de comunicação em tempo real para os dashboards dos gestores \[cite: 28, 32\]. A integração se beneficia do modelo não-bloqueante oferecido pelo Spring WebFlux através do uso de `Sinks.Many` e `Flux` do Project Reactor, que otimizam o uso de memória em escala \[cite: 21, 33\].  
**Escalabilidade Horizontal do SSE: O Backplane com Redis Pub/Sub**  
Como as conexões HTTP de longa duração do SSE são intrinsecamente stateful, mantidas de forma direta entre o navegador do gestor e uma instância específica da aplicação de backend, um desafio de escalabilidade surge quando o sistema é distribuído horizontalmente (com múltiplos pods do AWS ECS) \[cite: 10, 17\]. Se um evento de conclusão de atendimento ocorre em uma instância `A`, os navegadores conectados nas instâncias `B` ou `C` não serão notificados de forma nativa, gerando inconsistências visuais de monitoramento \[cite: 10, 17\].  
Para sanar este problema sem recorrer a sessões persistentes (*sticky sessions*), o FlowPay adota um padrão de design baseado em Backplane com **Redis Pub/Sub** \[cite: 10, 34\].  
\[Evento de Atribuição de Chat\] | v \[Nó Fargate \- B\] | | (Publish) v \[Redis Pub/Sub Cluster Backplane\] | \+------------------------+------------------------+  
| (Assinatura) | (Assinatura) v v \[Nó Fargate \- A\] \[Nó Fargate \- C\]  
| | | (Local SSE Emission) | (Local SSE Emission) v v \[Dashboard React \- Gestor 1\] \[Dashboard React \- Gestor 2\]  
Quando um evento de alteração de capacidade ou encerramento de chat ocorre em qualquer nó do contêiner AWS, o serviço publica o evento no tópico distribuído do Redis \[cite: 10, 34\]. Todas as instâncias em execução atuam como assinantes permanentes desse canal Redis \[cite: 10, 34\]. No momento em que a mensagem atinge o barramento central do Redis, todas as instâncias ativas capturam o payload e o propagam para seus respectivos `Sinks` de memória locais, garantindo a atualização imediata e sincronizada de todos os gestores ativos, independentemente da rota do balanceador de carga \[cite: 10, 34\].  
Sob clusters Redis 7.0+, utiliza-se sharded pub/sub (`SPUBLISH` / `SSUBSCRIBE`), garantindo que as mensagens fiquem restritas ao shard responsável pela chave do time de atendimento, atenuando consideravelmente o tráfego interno de barramento da infraestrutura de nuvem \[cite: 34\].  
**Métricas de Negócio e Indicadores-Chave de Desempenho (BI KPIs)**  
Abaixo estão detalhados os indicadores que compõem a inteligência de negócios do dashboard em tempo real do sistema FlowPay, estruturados de acordo com fórmulas matemáticas avançadas de call center \[cite: 35, 36, 37\]:  
Taxa de Ocupação Operacional (*O*  
*t*  
​  
)  
Representa o percentual de utilização real da força de trabalho alocada em relação à capacidade operacional disponível \[cite: 35, 38\]. Idealmente, deve-se buscar o equilíbrio de modo a maximizar a produtividade sem acarretar sobrecarga de agentes (burnout) \[cite: 35, 37, 39\].

Ocupa

c

¸

​

a

˜

o=(

∑

*i*\=1

*M*

​

*C*

*i*

​

∑

*i*\=1

*M*

​

*A*

*i*

​

​

)×100

*Onde*:

* *M* é o número total de agentes atualmente logados no sistema \[cite: 35\].  
* *A*  
* *i*  
* ​  
*  é o número de chats ativos sob responsabilidade do agente *i* no tempo *t* \[cite: 35\].  
* *C*  
* *i*  
* ​  
*  é a capacidade de atendimento máxima do agente *i* (neste caso, *C*  
* *i*  
* ​  
* \=3) \[cite: 35, 38\].

Tempo Médio de Resposta / Atendimento (Average Speed of Answer \- *ASA*)  
Aferição em segundos do intervalo médio de tempo que os clientes permanecem na fila aguardando vaga operacional antes da primeira interação real com o atendente \[cite: 36, 37\].

*ASA*\=

*N*

∑

*j*\=1

*N*

​

(*T*

atribuido,*j*

​

−*T*

criado,*j*

​

)

​

*Onde*:

* *N* é o número total de chats atribuídos com sucesso no período observado \[cite: 37\].  
* *T*  
* atribuido,*j*  
* ​  
*  é a data e hora do início do atendimento ativo para o chat *j*.  
* *T*  
* criado,*j*  
* ​  
*  é a data e hora do registro inicial de entrada na fila do chat *j*.

Acordo de Nível de Serviço de Atendimento (Service Level Agreement \- *SLA*)  
Métrica analítica que indica a proporção de atendimentos bem-sucedidos que tiveram sua espera na fila mantida abaixo do teto limite estipulado pela diretoria operacional (definido na FlowPay como 20 segundos) \[cite: 36, 37\].

*SLA*\=(

*C*

totais

​

*C*

≤20*s*

​

​

)×100

*Onde*:

* *C*  
* ≤20*s*  
* ​  
*  é o quantitativo de chats que saíram da fila de espera e iniciaram atendimento em até 20 segundos \[cite: 36\].  
* *C*  
* totais  
* ​  
*  é o volume total acumulado de chats registrados no sistema \[cite: 36\].

Taxa de Abandono de Fila (*R*  
*a*  
​  
)  
Informa a parcela de clientes que cancelaram ou abandonaram a requisição de suporte de forma prematura durante o período em que aguardavam a liberação de vagas nas filas \[cite: 36, 37\].

*R*

*a*

​

\=(

*F*

totais

​

*A*

*f*

​

​

)×100

*Onde*:

* *A*  
* *f*  
* ​  
*  é o número total de sessões encerradas ainda no status de `AGUARDANDO` na fila \[cite: 36, 37\].  
* *F*  
* totais  
* ​  
*  é a totalização de entradas de fila acumuladas pelo sistema \[cite: 36, 37\].

**Estratégia de Qualidade e Deploy (AWS)**  
**Testes Automatizados para Condições de Corrida Concorrentes**  
O teste JUnit 5 abaixo utiliza um `CountDownLatch` para liberar simultaneamente múltiplas threads de processamento concorrente \[cite: 18\]. Isso força uma condição de corrida intencional para verificar se o sistema impede que o atendente acumule mais do que os 3 atendimentos estipulados pela regra rígida de negócio do sistema FlowPay \[cite: 5, 18\].  
package com.flowpay.routing.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;  
import static org.mockito.Mockito.\*;

import com.flowpay.routing.application.port.out.AgentRepositoryPort;  
import com.flowpay.routing.application.port.out.ChatRepositoryPort;  
import com.flowpay.routing.application.port.out.QueueRepositoryPort;  
import com.flowpay.routing.domain.model.Agent;  
import com.flowpay.routing.domain.model.Chat;  
import org.junit.jupiter.api.Test;  
import org.junit.jupiter.api.extension.ExtendWith;  
import org.mockito.InjectMocks;  
import org.mockito.Mock;  
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;  
import java.util.UUID;  
import java.util.concurrent.CountDownLatch;  
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  
import java.util.concurrent.atomic.AtomicInteger;

@ExtendWith(MockitoExtension.class)  
class RoutingConcurrencyIntegrationTest {

    @Mock  
    private AgentRepositoryPort agentRepositoryPort;

    @Mock  
    private ChatRepositoryPort chatRepositoryPort;

    @Mock  
    private QueueRepositoryPort queueRepositoryPort;

    @InjectMocks  
    private RoutingEngineService routingEngineService;

    @Test  
    void testRegraLimiteEstritoDeCapacidadeDeAtendimentoConcorrente() throws InterruptedException {  
        UUID teamId \= UUID.randomUUID();  
          
        // Atendente com capacidade máxima definida para 3 chats  
        Agent agentModel \= new Agent(UUID.randomUUID(), teamId, "Agent FlowPay", 3);  
          
        int totalRequests \= 10;  
        ExecutorService executor \= Executors.newFixedThreadPool(totalRequests);  
        CountDownLatch starterGate \= new CountDownLatch(1);  
        CountDownLatch endGate \= new CountDownLatch(totalRequests);  
          
        AtomicInteger successfulAssignments \= new AtomicInteger(0);

        // Mocking do comportamento de concorrência com suporte ao estado interno do modelo  
        when(agentRepositoryPort.findAvailableAgentWithWriteLock(any(String.class)))  
            .thenAnswer(inv \-\> {  
                synchronized (agentModel) {  
                    if (agentModel.getActiveChatsCount() \< agentModel.getMaxChats()) {  
                        return Optional.of(agentModel);  
                    }  
                    return Optional.empty();  
                }  
            });

        when(queueRepositoryPort.findOldestWaitingChat(any(String.class)))  
            .thenAnswer(inv \-\> Optional.of(new Chat(UUID.randomUUID(), "customer-phone-009")));

        for (int i \= 0; i \< totalRequests; i++) {  
            executor.submit(() \-\> {  
                try {  
                    starterGate.await(); // Sincroniza o arranque das threads no mesmo instante  
                    routingEngineService.executeDistributionLoop(teamId.toString());  
                    successfulAssignments.incrementAndGet();  
                } catch (Exception e) {  
                    // Trata possíveis falhas de simulação de concorrência  
                } finally {  
                    endGate.countDown();  
                }  
            });  
        }

        starterGate.countDown(); // Libera todas as threads simultaneamente para simular o pico  
        endGate.await(); // Aguarda a execução de todas as threads do pool  
        executor.shutdown();

        // Garante que o estado final do agente obedece estritamente ao teto de 3 atendimentos simultâneos  
        assertEquals(3, agentModel.getActiveChatsCount(),   
                "O limite máximo de atendimentos simultâneos do agente foi violado sob estresse concorrente\!");  
    }  
}

**Arquitetura de Implantação e Nuvem na AWS**  
O ecossistema em nuvem da FlowPay é desenhado para garantir conformidade com políticas rígidas de segurança de dados financeiros, utilizando redundância geográfica ativa e isolamento de rede \[cite: 40\].  
Dockerfile Multi-Stage de Compilação Segura  
A imagem Docker é estruturada em multi-stage build para manter a imagem final do contêiner limpa e leve, prevenindo a presença de utilitários de compilação que possam ser explorados maliciosamente em ambiente de produção:  
\# Estágio de Compilação  
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder  
WORKDIR /workspace  
COPY pom.xml .  
COPY src ./src  
RUN mvn clean package \-DskipTests \--batch-mode

\# Estágio de Execução  
FROM eclipse-temurin:17-jre-alpine  
RUN addgroup \-S appgroup && adduser \-S appuser \-G appgroup  
WORKDIR /app

\# Parâmetros JVM de segurança para controle de limites de memória e otimização do Garbage Collector  
ENV JAVA\_OPTS="-XX:+UseG1GC \-XX:MaxRAMPercentage=75.0 \-XX:InitialRAMPercentage=50.0 \-XX:+ExitOnOutOfMemoryError"

COPY \--from=builder /workspace/target/flowpay-routing-\*.jar app.jar  
RUN chown \-R appuser:appgroup /app

USER appuser  
EXPOSE 8080

ENTRYPOINT \["sh", "-c", "java $JAVA\_OPTS \-jar app.jar"\]

Desenho Detalhado da Infraestrutura de Nuvem  
O fluxo de requisições inicia-se no nível do DNS gerenciado pela AWS \[cite: 40\].

1. **Camada de Borda**: O **AWS Route 53** efetua o roteamento de latência e resolução de endereços, enviando as requisições para o **AWS Application Load Balancer (ALB)** \[cite: 40\]. O ALB realiza o encerramento do protocolo SSL/TLS de forma segura e distribui as requisições HTTP de entrada para os contêineres ativos \[cite: 17, 40\].  
2. **Camada de Aplicação (AWS ECS Fargate)**: Os contêineres Spring Boot contendo a API de roteamento e barramento do SSE rodam sobre Fargate, operando de forma 100% stateless \[cite: 17\]. A infraestrutura é distribuída em zonas de disponibilidade (Multi-AZ) redundantes para garantir tolerância a falhas na infraestrutura física \[cite: 10\].  
3. **Camada de Cache e Distribuição (AWS ElastiCache for Redis)**: Utiliza uma arquitetura com Cluster Mode ativado \[cite: 40\]. Isso divide o conjunto de chaves em hash slots associados a múltiplos nós primários de processamento rápido \[cite: 40\]. As réplicas de leitura do Redis assumem operações não estruturais, aliviando consideravelmente a carga de gravação do nó principal \[cite: 40\]. Os drivers clientes do Redisson na aplicação utilizam recursos de descoberta ativa da topologia (*cluster-aware clients*), permitindo que requisições de lock sejam direcionadas ao nó primário correspondente ao slot de hash do time mapeado, evitando redirecionamentos e gargalos \[cite: 40\].  
4. **Camada de Persistência (AWS RDS PostgreSQL Multi-AZ)**: O banco de dados opera em replicação síncrona com failover automático ativo-passivo \[cite: 40\]. Para otimizar o tempo de resposta e poupar recursos computacionais, o sistema utiliza conexões pooling de tamanho finito com estratégias de reconexão baseadas em backoff exponencial com jitter para suportar picos de carga sem esgotar as conexões de banco de dados disponíveis \[cite: 40\].

**Conclusões e Recomendações Técnicas**  
A modelagem proposta neste blueprint assegura que o sistema FlowPay conte com uma infraestrutura escalável, resiliente e segura \[cite: 2, 40\]. A combinação da Arquitetura Hexagonal com locks distribuídos via Redisson e locks pessimistas no PostgreSQL garante o cumprimento rígido do limite estrito de atendimentos por atendente, protegendo o sistema de condições de corrida sob alta concorrência \[cite: 1, 12, 16\].  
Para um rollout de produção bem-sucedido, recomendam-se as seguintes diretrizes:

1. **Configuração de Timeouts Resilientes**: Definir timeouts curtos para locks distribuídos no Redis (ex: lease de 10 segundos controlado por watchdog), evitando cenários de travamento permanente caso um contêiner sofra queda repentina \[cite: 9, 16\].  
2. **Uso de Conexões Pool Limitadas**: Ajustar as propriedades do HikariCP no Spring para casar adequadamente com o volume de conexões aceito pelo PostgreSQL do RDS, prevenindo falhas de esgotamento sob estresse de concorrência \[cite: 40\].  
3. **Mitigação de Latência em Larga Escala com SSE**: Para evitar o consumo exagerado de portas no roteamento, garantir a ativação do protocolo HTTP/2 ao longo de toda a rede interna da AWS, eliminando a barreira clássica de limite de seis conexões por host imposta pelos navegadores sob HTTP/1.1 \[cite: 28, 30\].  
4. **Observabilidade das Filas e SLAs**: Estabelecer alarmes integrados com AWS CloudWatch para monitorar o tempo de fila e acionar políticas de auto-scaling de agentes com base no indicador SLA parametrizado em tempo real no dashboard \[cite: 36, 37\].

\--------------------------------------------------------------------------------

1. Hexagonal Architecture with Spring Boot Part 1 \- Vitelco Technologies, [https://vitelco.com/vitelco-blog/hexagonal-architecture-with-spring-boot](https://vitelco.com/vitelco-blog/hexagonal-architecture-with-spring-boot)  
2. Hexagonal Architecture with Spring Boot | by Leandro Franchi \- Medium, [https://leandrofranchi.medium.com/hexagonal-architecture-with-spring-boot-building-truly-scalable-systems-7948472406ed](https://leandrofranchi.medium.com/hexagonal-architecture-with-spring-boot-building-truly-scalable-systems-7948472406ed)  
3. Hexagonal Architecture With Spring Boot | Shouldn't Be Hard \- Code With Arho, [https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/](https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/)  
4. Hexagonal Architecture in Spring Boot: A Practical Guide \- DEV Community, [https://dev.to/jhonifaber/hexagonal-architecture-or-port-adapters-23ed](https://dev.to/jhonifaber/hexagonal-architecture-or-port-adapters-23ed)  
5. Race Conditions and Critical Sections | Concurrency Interview \- AlgoMaster.io, [https://algomaster.io/learn/concurrency-interview/race-conditions-and-critical-sections](https://algomaster.io/learn/concurrency-interview/race-conditions-and-critical-sections)  
6. Race Conditions Explained: The Concurrency Bug Every Backend Developer Should Understand | Billy Okeyo, [https://billyokeyo.dev/posts/race-conditions-explained/](https://billyokeyo.dev/posts/race-conditions-explained/)  
7. Dealing with Race Condition in Spring Boot | by Ari Prayoga \- Medium, [https://medium.com/@ariprayoga007/dealing-with-race-condition-in-spring-boot-3c3fd615e35a](https://medium.com/@ariprayoga007/dealing-with-race-condition-in-spring-boot-3c3fd615e35a)  
8. Distributed Locks :: Spring Integration, [https://docs.spring.io/spring-integration/reference/distributed-locks.html](https://docs.spring.io/spring-integration/reference/distributed-locks.html)  
9. How to Avoid Race Conditions in your Microservice Application, [https://blog.avenuecode.com/how-to-avoid-race-conditions-in-your-microservice-application](https://blog.avenuecode.com/how-to-avoid-race-conditions-in-your-microservice-application)  
10. Setting Up Server-Sent Events (SSE) with Spring Boot on Kubernetes \- Medium, [https://medium.com/@rohanreddym/setting-up-server-sent-events-sse-with-spring-boot-on-kubernetes-df906b799dbf](https://medium.com/@rohanreddym/setting-up-server-sent-events-sse-with-spring-boot-on-kubernetes-df906b799dbf)  
11. Implementing Distributed Locks in Spring Boot with Redisson \- Dev Genius, [https://blog.devgenius.io/implementing-distributed-locks-in-spring-boot-with-redisson-2967149bcb7c](https://blog.devgenius.io/implementing-distributed-locks-in-spring-boot-with-redisson-2967149bcb7c)  
12. Pessimistic Locking in JPA \- Baeldung, [https://www.baeldung.com/jpa-pessimistic-locking](https://www.baeldung.com/jpa-pessimistic-locking)  
13. Optimistic and Pessimistic Transaction Locks in Spring Boot Data JPA | Coding Shuttle, [https://www.codingshuttle.com/spring-boot-handbook/optimistic-and-pessimistic-transaction-locks-in-spring-boot-data-jpa](https://www.codingshuttle.com/spring-boot-handbook/optimistic-and-pessimistic-transaction-locks-in-spring-boot-data-jpa)  
14. Locking Strategies in Concurrent Applications: Pessimistic vs. Optimistic Locking with Spring Boot | CodeWiz, [https://codewiz.info/blog/locking-strategies-spring-boot/](https://codewiz.info/blog/locking-strategies-spring-boot/)  
15. Database Race conditions \- GitHub, [https://gist.github.com/valarpirai/9e2d36e34ca42304da4694b41c136f5f](https://gist.github.com/valarpirai/9e2d36e34ca42304da4694b41c136f5f)  
16. How to Use Redis Locks in Java with Redisson, [https://redisson.pro/blog/how-to-use-redis-locks-in-java.html](https://redisson.pro/blog/how-to-use-redis-locks-in-java.html)  
17. Scaling Real-Time Applications with Server-Sent Events(SSE) \- SurveySparrow Engineering, [https://engineering.surveysparrow.com/scaling-real-time-applications-with-server-sent-events-sse-abd91f70a5c9](https://engineering.surveysparrow.com/scaling-real-time-applications-with-server-sent-events-sse-abd91f70a5c9)  
18. Evaluating Redisson Distributed Lock (tryLock) to prevent overselling in a Spring Boot E-commerce app \- Stack Overflow, [https://stackoverflow.com/questions/79916654/evaluating-redisson-distributed-lock-trylock-to-prevent-overselling-in-a-sprin](https://stackoverflow.com/questions/79916654/evaluating-redisson-distributed-lock-trylock-to-prevent-overselling-in-a-sprin)  
19. Enabling Transaction Locks in Spring Data JPA \- GeeksforGeeks, [https://www.geeksforgeeks.org/advance-java/enabling-transaction-locks-in-spring-data-jpa/](https://www.geeksforgeeks.org/advance-java/enabling-transaction-locks-in-spring-data-jpa/)  
20. How to enable LockModeType.PESSIMISTIC\_WRITE when looking up entities with Spring Data JPA? \- Stack Overflow, [https://stackoverflow.com/questions/16159396/how-to-enable-lockmodetype-pessimistic-write-when-looking-up-entities-with-sprin](https://stackoverflow.com/questions/16159396/how-to-enable-lockmodetype-pessimistic-write-when-looking-up-entities-with-sprin)  
21. Reactive Real-Time Notifications with SSE, Spring Boot, and Redis Pub/Sub \- InfoQ, [https://www.infoq.com/articles/reactive-notification-system-server-sent-events/](https://www.infoq.com/articles/reactive-notification-system-server-sent-events/)  
22. Guide to WhatsApp Webhooks: Features and Best Practices \- Hookdeck, [https://hookdeck.com/webhooks/platforms/guide-to-whatsapp-webhooks-features-and-best-practices](https://hookdeck.com/webhooks/platforms/guide-to-whatsapp-webhooks-features-and-best-practices)  
23. WhatsApp Business API \+ Webhook Integration With MongoDB \- DEV Community, [https://dev.to/mongodb/whatsapp-business-api-webhook-integration-with-mongodb-3mjc](https://dev.to/mongodb/whatsapp-business-api-webhook-integration-with-mongodb-3mjc)  
24. WhatsApp Webhooks Explained (2026): Setup, Events, Payloads & Best Practices, [https://messagebot.in/blog/whatsapp-webhooks-explained/](https://messagebot.in/blog/whatsapp-webhooks-explained/)  
25. WhatsApp API Webhook Setup Guide For Developers \- Notify Africa, [https://notify.africa/blogs/whatsapp-api-webhook-setup-guide](https://notify.africa/blogs/whatsapp-api-webhook-setup-guide)  
26. go-whatsapp-web-multidevice/docs/webhook-payload.md at main \- GitHub, [https://github.com/aldinokemal/go-whatsapp-web-multidevice/blob/main/docs/webhook-payload.md](https://github.com/aldinokemal/go-whatsapp-web-multidevice/blob/main/docs/webhook-payload.md)  
27. WebSockets vs Server-Sent-Events vs Long-Polling vs WebRTC vs WebTransport | RxDB \- JavaScript Database, [https://rxdb.info/articles/websockets-sse-polling-webrtc-webtransport.html](https://rxdb.info/articles/websockets-sse-polling-webrtc-webtransport.html)  
28. Server-Sent Events Beat WebSockets for 95% of Real-Time Apps (Here's Why), [https://dev.to/polliog/server-sent-events-beat-websockets-for-95-of-real-time-apps-heres-why-a4l](https://dev.to/polliog/server-sent-events-beat-websockets-for-95-of-real-time-apps-heres-why-a4l)  
29. WebSocket vs SSE: Which One Should You Use?, [https://websocket.org/comparisons/sse/](https://websocket.org/comparisons/sse/)  
30. WebSocket vs Server-Sent Events: How to choose for real-time apps \- Vercel, [https://vercel.com/i/websocket-vs-server-sent-events](https://vercel.com/i/websocket-vs-server-sent-events)  
31. Server-Sent Events vs WebSockets: Key Differences and Use Cases in 2026 \- Nimble, [https://www.nimbleway.com/blog/server-sent-events-vs-websockets-what-is-the-difference-2026-guide](https://www.nimbleway.com/blog/server-sent-events-vs-websockets-what-is-the-difference-2026-guide)  
32. SSE vs WebSockets — most devs default to WebSockets even when they don't need two-way communication : r/webdev \- Reddit, [https://www.reddit.com/r/webdev/comments/1rkvqkt/sse\_vs\_websockets\_most\_devs\_default\_to\_websockets/](https://www.reddit.com/r/webdev/comments/1rkvqkt/sse_vs_websockets_most_devs_default_to_websockets/)  
33. Reactive Real-Time Notifications with SSE, Spring Boot, and Redis Pub/Sub \- daily.dev, [https://daily.dev/posts/reactive-real-time-notifications-with-sse-spring-boot-and-redis-pub-sub-o7wuxwnbs](https://daily.dev/posts/reactive-real-time-notifications-with-sse-spring-boot-and-redis-pub-sub-o7wuxwnbs)  
34. How to Scale Redis Pub/Sub Across Multiple Servers \- OneUptime, [https://oneuptime.com/blog/post/2026-03-31-redis-scale-pubsub-multiple-servers/view](https://oneuptime.com/blog/post/2026-03-31-redis-scale-pubsub-multiple-servers/view)  
35. Call Center Occupancy Rate: How to Calculate It & Why It Matters \- Nextiva, [https://www.nextiva.com/blog/call-center-occupancy.html](https://www.nextiva.com/blog/call-center-occupancy.html)  
36. Call Center Productivity: Ultimate Guide to Metrics, and Strategies (2026) | AmplifAI, [https://www.amplifai.com/blog/call-center-productivity](https://www.amplifai.com/blog/call-center-productivity)  
37. Call center productivity: How to measure and improve it \- Zoom, [https://www.zoom.com/en/blog/call-center-productivity/](https://www.zoom.com/en/blog/call-center-productivity/)  
38. Occupancy Call Center Fully Examined Plus Challenges, Solutions and How-To's \- Giva, [https://www.givainc.com/blog/occupancy-call-center/](https://www.givainc.com/blog/occupancy-call-center/)  
39. Call Center Metrics | 8x8, [https://www.8x8.com/s/call-center-metrics](https://www.8x8.com/s/call-center-metrics)  
40. Best practices: Valkey/Redis OSS clients and Amazon ElastiCache | AWS Database Blog, [https://aws.amazon.com/blogs/database/best-practices-valkey-redis-oss-clients-and-amazon-elasticache/](https://aws.amazon.com/blogs/database/best-practices-valkey-redis-oss-clients-and-amazon-elasticache/)

