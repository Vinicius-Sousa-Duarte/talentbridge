package com.dunkweb.talentbridge.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI talentBridgeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TalentBridge API")
                        .description("API para gestao de recrutadores e acompanhamento de interacoes.")
                        .version("v1")
                        .contact(new Contact().name("TalentBridge Team")));
    }
}
