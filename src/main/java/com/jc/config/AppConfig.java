package com.jc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DataBaseConfig.class)
@ComponentScan("com.jc.service")
public class AppConfig {
	// various @Bean definitions ...
}