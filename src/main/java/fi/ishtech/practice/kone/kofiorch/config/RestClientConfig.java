package fi.ishtech.practice.kone.kofiorch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Spring configuration for HTTP clients.
 */
@Configuration
public class RestClientConfig {

	/**
	 * Provides a default {@link RestClient} for outbound HTTP calls.
	 *
	 * @return configured {@link RestClient} instance
	 */
	@Bean
	RestClient restClient() {
		return RestClient.create();
	}
}
