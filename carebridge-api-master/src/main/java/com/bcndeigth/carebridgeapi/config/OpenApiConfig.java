package com.bcndeigth.carebridgeapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI careBridgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareBridge API")
                        .version("v1")
                        .description("API para cadastro e gerenciamento de usuários da plataforma CareBridge."));
    }
}