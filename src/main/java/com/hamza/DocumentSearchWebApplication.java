package com.hamza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DocumentSearchWebApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Démarrage de l'application de recherche de documents...");
        SpringApplication.run(DocumentSearchWebApplication.class, args);
    }
}
