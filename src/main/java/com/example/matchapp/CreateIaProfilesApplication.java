package com.example.matchapp;

import com.example.matchapp.config.ImageGenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.example.matchapp"})
@EnableConfigurationProperties(ImageGenProperties.class)
public class CreateIaProfilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreateIaProfilesApplication.class, args);
    }
}
