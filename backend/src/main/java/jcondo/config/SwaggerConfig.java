package jcondo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuracao do Swagger - abre em http://localhost:8080/swagger-ui/index.html
 *
 * O esquema "bearer" adiciona o botao Authorize na interface: basta chamar
 * POST /api/auth/login, copiar o token da resposta e colar ali para testar
 * os demais endpoints direto pelo navegador, sem precisar do aplicativo.
 */
@Configuration
public class SwaggerConfig {

    private static final String ESQUEMA = "token";

    @Bean
    public OpenAPI jcondoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JCondo API")
                        .description("API REST de gestão condominial consumida pelo "
                                + "aplicativo JCondo Mobile (Android). "
                                + "Usuários de teste: ana.souza@email.com / 123456 "
                                + "e sindico@jcondo.com / admin123.")
                        .version("2.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA))
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme()
                                .name(ESQUEMA)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("Token devolvido por POST /api/auth/login")));
    }
}
