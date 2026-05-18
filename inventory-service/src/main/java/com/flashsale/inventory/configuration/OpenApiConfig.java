package com.flashsale.inventory.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Inventory Service API")
                .description("Tracks product stock levels, updated via flash sale purchase events")
                .version("1.0.0")
                .contact(new Contact().name("Tran Phat Thinh").email("phatthinh123@gmail.com")));
  }
}
