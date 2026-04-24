package com.petify.petify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class PetifyApplication {

    public static void main(String[] args) {
        // Load .env file from project root
        loadEnvFile();
        SpringApplication.run(PetifyApplication.class, args);
    }

    private static void loadEnvFile() {
        try {
            String envPath = ".env.properties";
            if (Files.exists(Paths.get(envPath))) {
                Files.lines(Paths.get(envPath))
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            // Remove quotes if present
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length() - 1);
                            }
                            System.setProperty(key, value);
                        }
                    });
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
        }
    }
}

