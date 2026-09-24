package com.example.postgresstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PostgresStackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgresStackApplication.class, args);
    }
}