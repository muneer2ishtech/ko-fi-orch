package fi.ishtech.practice.kone.kofiorch.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * S3 settings bound from {@code ko-fi-orch.s3.*} application properties.
 *
 * @param region          AWS region for the S3 bucket
 * @param bucket          target S3 bucket name
 * @param presignDuration how long presigned URLs remain valid
 */
@ConfigurationProperties(prefix = "ko-fi-orch.s3")
@Validated
public record S3Properties(
		@NotBlank String region,
		@NotBlank String bucket,
		@NotNull Duration presignDuration) {
}
