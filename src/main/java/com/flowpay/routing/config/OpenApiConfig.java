package com.flowpay.routing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flowpayOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("FlowPay Routing API")
                        .description("API robusta para o motor de roteamento omnichannel de conversas financeiras (FlowPay). Oferece endpoints para Gestão de Agentes, Equipes, Integração via Webhooks (WhatsApp/Meta), Roteamento com IA (Gemini) e Monitoramento em Tempo Real via Server-Sent Events (SSE).")
                        .version("v0.1.0")
                        .contact(new Contact().name("João Victor PFR").email("joaovictorpfr@gmail.com")));
    }
}
