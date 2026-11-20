
package com.freightfox.githubsearcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI githubSearcherOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GitHub Repository Searcher API")
                        .description(
                            "A Spring Boot REST API that searches GitHub repositories, "
                            + "stores repository details in PostgreSQL, and provides "
                            + "filtering and sorting capabilities."
                        )
                        .version("1.0.0"));
    }
}
