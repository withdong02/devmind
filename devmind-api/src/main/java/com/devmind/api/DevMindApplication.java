package com.devmind.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.devmind")
@EntityScan(basePackages = {"com.devmind.rag.entity", "com.devmind.memory.entity", "com.devmind.harness.entity"})
@EnableJpaRepositories(basePackages = {"com.devmind.rag.repository", "com.devmind.memory.repository", "com.devmind.harness.repository"})
public class DevMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevMindApplication.class, args);
    }
}
