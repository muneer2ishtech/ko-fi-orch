package fi.ishtech.practice.kone.kofiorch.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fi.ishtech.practice.kone.kofiorch.api.dto.UploadRequest;
import fi.ishtech.practice.kone.kofiorch.api.dto.UploadResponse;
import fi.ishtech.practice.kone.kofiorch.orchestration.FileUploadOrchestrator;
import jakarta.validation.Valid;

/**
 * HTTP API for the data orchestration upload flow.
 */
@RestController
public class UploadController {

	private final FileUploadOrchestrator fileUploadOrchestrator;

	/**
	 * Creates the controller.
	 *
	 * @param fileUploadOrchestrator orchestration service for the upload pipeline
	 */
	public UploadController(FileUploadOrchestrator fileUploadOrchestrator) {
		this.fileUploadOrchestrator = fileUploadOrchestrator;
	}

	/**
	 * Queries source files by criteria and uploads them to object storage.
	 *
	 * @param request model ID, optional order ID, optional date range
	 * @return file names that succeeded and failed
	 */
	@PostMapping(path = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public UploadResponse upload(@Valid @RequestBody UploadRequest request) {
		return fileUploadOrchestrator.upload(request);
	}
}
