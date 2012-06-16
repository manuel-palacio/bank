package com.palace.smallapp.bank.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath*:*Config.xml")
@ComponentScan({"com.palace.smallapp.bank"})
public class ApplicationConfig {
}
