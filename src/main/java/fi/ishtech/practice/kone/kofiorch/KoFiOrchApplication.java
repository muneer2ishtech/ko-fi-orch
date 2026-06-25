package fi.ishtech.practice.kone.kofiorch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the ko-fi-orch Spring Boot application.
 */
@SpringBootApplication
public class KoFiOrchApplication {

	/**
	 * Starts the application.
	 *
	 * @param args command-line arguments passed to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(KoFiOrchApplication.class, args);
	}

}
