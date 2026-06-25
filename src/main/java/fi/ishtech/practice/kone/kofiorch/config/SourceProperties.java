package fi.ishtech.practice.kone.kofiorch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * PDM/PLM source API settings bound from {@code ko-fi-orch.source.*} application properties.
 *
 * @param baseUrl base URL of the source API (e.g. {@code http://localhost:9090})
 */
@ConfigurationProperties(prefix = "ko-fi-orch.source")
@Validated
public record SourceProperties(@NotBlank String baseUrl) {
}
