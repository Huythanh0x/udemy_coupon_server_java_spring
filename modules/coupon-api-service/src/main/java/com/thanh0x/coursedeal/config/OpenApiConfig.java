package com.thanh0x.coursedeal.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Course Deal Server API",
                version = "v1",
                description = "REST APIs for crawling and serving 100% off Udemy coupons with authentication support.",
                contact = @Contact(name = "Thanh0x", email = "huythanh0x@gmail.com", url = "https://github.com/huythanh0x"),
                license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")
        )
)
public class OpenApiConfig {
}

