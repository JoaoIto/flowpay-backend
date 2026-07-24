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
                        .description("API para o motor de roteamento omnichannel de conversas financeiras. Suporta conexões em tempo real (SSE) para o painel de monitoramento e endpoints para integração com provedores de mensageria.")
                        .version("v0.1.0")
                        .contact(new Contact().name("FlowPay Engineering").email("dev@flowpay.com")));
    }
}
