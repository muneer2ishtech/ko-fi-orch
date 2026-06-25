package fi.ishtech.practice.kone.kofiorch.orchestration;

import fi.ishtech.practice.kone.kofiorch.api.dto.UploadRequest;
import fi.ishtech.practice.kone.kofiorch.api.dto.UploadResponse;

/**
 * Coordinates query, download, presign, and upload for a single upload request.
 */
public interface FileUploadOrchestrator {

	/**
	 * Runs the upload flow for files matching the request criteria.
	 *
	 * @param request query criteria and upload parameters
	 * @return per-file success and failure outcome
	 */
	UploadResponse upload(UploadRequest request);
}
