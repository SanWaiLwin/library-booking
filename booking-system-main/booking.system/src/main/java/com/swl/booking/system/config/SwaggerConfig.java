package com.swl.booking.system.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
	    info = @Info(
	        title = "Booking System API",
	        version = "v1",
	        description = "API documentation for the Booking System"
	    )
	)
public class SwaggerConfig {
}