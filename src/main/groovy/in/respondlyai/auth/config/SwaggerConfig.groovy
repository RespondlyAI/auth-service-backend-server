package in.respondlyai.auth.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RespondlyAI Auth Service API")
                .description("Authentication and authorization service for RespondlyAI platform")
                .version("1.0.0")
                .contact(new Contact()
                    .name("RespondlyAI")
                    .email("support@respondly.ai")
                )
                .license(new License()
                    .name("Private")
                )
            )
    }
}
