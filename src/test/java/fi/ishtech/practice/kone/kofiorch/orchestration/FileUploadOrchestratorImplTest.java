package fi.ishtech.practice.kone.kofiorch.orchestration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import fi.ishtech.practice.kone.kofiorch.api.dto.UploadRequest;

class FileUploadOrchestratorImplTest {

	private final FileUploadOrchestratorImpl orchestrator = new FileUploadOrchestratorImpl();

	@Test
	void returnsEmptyResponseUntilSourceIntegrationIsWired() {
		UploadRequest request = new UploadRequest("MODEL-1", "ORD-1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

		var response = orchestrator.upload(request);

		assertThat(response.succeeded()).isEmpty();
		assertThat(response.failed()).isEmpty();
	}
}
