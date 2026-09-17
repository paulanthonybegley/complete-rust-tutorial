package com.example.apirules.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API Law 7 - treat API changes carefully.
 *
 * We enable Spring Framework 7's native API versioning support and resolve the
 * requested version from the "API-Version" request header. Every request must
 * therefore declare which version of the API it speaks. Spring then routes the
 * request to the matching controller method, or answers 400 for a missing or
 * unsupported version.
 *
 * In a Spring Boot 4 application the same thing can be configured with:
 * {@code spring.mvc.apiversion.use.header=API-Version}
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void configureApiVersioning(ApiVersionConfigurer configurer) {
		configurer.useRequestHeader("API-Version");
	}
}