package fi.ishtech.practice.kone.kofiorch.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Spring configuration for AWS S3 presigning.
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class AwsS3Config {

	/**
	 * Creates an {@link S3Presigner} for the configured region.
	 *
	 * @param s3Properties S3 configuration properties
	 * @return S3 presigner bean, closed on context shutdown
	 */
	@Bean(destroyMethod = "close")
	S3Presigner s3Presigner(S3Properties s3Properties) {
		return S3Presigner.builder()
			.region(Region.of(s3Properties.region()))
			.build();
	}
}
