package com.felipedev.frauddetection.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "ApiKeyAuth";

    @Bean
    public OpenAPI fraudDetectionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fraud Detection & Risk Engine API")
                        .description("API corporativa de alta performance para avaliação de risco financeiro e detecção de fraudes em tempo real (PIX, TED, Cartão). Desenvolvida em Spring Boot 3 e Java 21.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Felipe Gonçalves (FelipeGardenghiDev)")
                                .url("https://github.com/FelipeGardenghiDev")
                                .email("felipe@felipedev.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name("X-API-KEY")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Chave de autenticação inter-microsserviços. Exemplo: 'fraud-secret-key-2026'")));
    }
}