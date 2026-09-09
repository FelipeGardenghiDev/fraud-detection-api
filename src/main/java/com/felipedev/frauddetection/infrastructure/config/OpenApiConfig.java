package com.felipedev.frauddetection.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

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
                                .url("https://opensource.org/licenses/MIT")));
    }
}