package fi.ishtech.practice.kone.kofiorch.orchestration;

import org.springframework.stereotype.Service;

import fi.ishtech.practice.kone.kofiorch.api.dto.UploadRequest;
import fi.ishtech.practice.kone.kofiorch.api.dto.UploadResponse;

/**
 * Orchestrates the full upload pipeline (source query → download → presign → S3 upload).
 */
@Service
public class FileUploadOrchestratorImpl implements FileUploadOrchestrator {

	/**
	 * Runs the upload flow for files matching the request criteria.
	 *
	 * @param request query criteria and upload parameters
	 * @return per-file success and failure outcome
	 */
	@Override
	public UploadResponse upload(UploadRequest request) {
		// Source query, download, and upload API presign will be wired in follow-up branches.
		return UploadResponse.empty();
	}
}
