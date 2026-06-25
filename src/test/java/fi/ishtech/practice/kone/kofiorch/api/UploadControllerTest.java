package fi.ishtech.practice.kone.kofiorch.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fi.ishtech.practice.kone.kofiorch.api.dto.FailedFileResponse;
import fi.ishtech.practice.kone.kofiorch.api.dto.UploadResponse;
import fi.ishtech.practice.kone.kofiorch.orchestration.FileUploadOrchestrator;

@WebMvcTest(UploadController.class)
class UploadControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FileUploadOrchestrator fileUploadOrchestrator;

	@Test
	void uploadReturnsOrchestratorResult() throws Exception {
		when(fileUploadOrchestrator.upload(any())).thenReturn(new UploadResponse(
			List.of("part-a.stp"),
			List.of(new FailedFileResponse("part-b.stp", "timeout"))));

		mockMvc.perform(post("/upload")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "modelId": "MODEL-42",
					  "orderId": "ORD-7",
					  "dateFrom": "2026-01-01",
					  "dateTo": "2026-01-31"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.succeeded[0]").value("part-a.stp"))
			.andExpect(jsonPath("$.failed[0].fileName").value("part-b.stp"))
			.andExpect(jsonPath("$.failed[0].reason").value("timeout"));

		verify(fileUploadOrchestrator).upload(any());
	}

	@Test
	void uploadRejectsMissingModelId() throws Exception {
		mockMvc.perform(post("/upload")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "orderId": "ORD-7"
					}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void uploadRejectsPartialDateRange() throws Exception {
		mockMvc.perform(post("/upload")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "modelId": "MODEL-42",
					  "dateFrom": "2026-01-01"
					}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void uploadRejectsInvertedDateRange() throws Exception {
		mockMvc.perform(post("/upload")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "modelId": "MODEL-42",
					  "dateFrom": "2026-02-01",
					  "dateTo": "2026-01-01"
					}
					"""))
			.andExpect(status().isBadRequest());
	}
}
