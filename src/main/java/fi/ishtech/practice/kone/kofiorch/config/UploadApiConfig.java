package fi.ishtech.practice.kone.kofiorch.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the external upload API client.
 */
@Configuration
@EnableConfigurationProperties(UploadApiProperties.class)
public class UploadApiConfig {
}
