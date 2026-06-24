package fi.ishtech.practice.kone.kofiorch.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the PDM/PLM source API client.
 */
@Configuration
@EnableConfigurationProperties(SourceProperties.class)
public class SourceApiConfig {
}
