package fi.ishtech.practice.kone.kofiorch.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "ko-fi-orch.s3")
@Validated
public record S3Properties(
		@NotBlank String region,
		@NotBlank String bucket,
		@NotNull Duration presignDuration) {
}
