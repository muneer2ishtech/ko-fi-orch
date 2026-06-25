package fi.ishtech.practice.kone.kofiorch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Upload API settings bound from {@code ko-fi-orch.upload-api.*} application properties.
 *
 * @param baseUrl base URL of the external upload API (e.g. {@code http://localhost:9091})
 */
@ConfigurationProperties(prefix = "ko-fi-orch.upload-api")
@Validated
public record UploadApiProperties(@NotBlank String baseUrl) {
}
