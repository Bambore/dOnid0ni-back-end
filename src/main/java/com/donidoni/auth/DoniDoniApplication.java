package com.donidoni.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application Doni-Doni Backend.
 *
 * <p>Fournit les services d'authentification pour l'application mobile :
 * Google Sign-In et OTP par SMS, avec Keycloak comme serveur d'identité.</p>
 */
@SpringBootApplication
public class DoniDoniApplication {

    public static void main(final String[] args) {
        SpringApplication.run(DoniDoniApplication.class, args);
    }
}
