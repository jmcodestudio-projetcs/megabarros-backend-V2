package br.com.jmcodestudio.autenticacaotemplate.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

/**
 * Configuration class for loading environment variables from .env file.
 *
 * This configuration ensures that variables defined in the .env file are loaded
 * into the Spring environment, allowing them to be used with @Value annotations
 * throughout the application.
 *
 * The .env file is loaded from the project root directory.
 */
@Configuration
public class EnvConfig {

    static {
        // Carrega o arquivo .env se existir
        String envPath = ".env";
        if (new File(envPath).exists()) {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();

            // Injeta as variáveis do .env nas variáveis de ambiente do sistema
            dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
            );
        }
    }
}

