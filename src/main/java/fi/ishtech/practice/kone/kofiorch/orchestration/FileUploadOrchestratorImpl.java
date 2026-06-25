package fi.ishtech.practice.kone.kofiorch.orchestration;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fi.ishtech.practice.kone.kofiorch.api.dto.FailedFileResponse;
import fi.ishtech.practice.kone.kofiorch.api.dto.UploadRequest;
import fi.ishtech.practice.kone.kofiorch.api.dto.UploadResponse;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.FileQueryCriteria;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;
import fi.ishtech.practice.kone.kofiorch.source.SourceDownloadPort;
import fi.ishtech.practice.kone.kofiorch.source.SourceQueryPort;
import fi.ishtech.practice.kone.kofiorch.upload.PresignUrlPort;
import fi.ishtech.practice.kone.kofiorch.upload.UploadPort;

/**
 * Orchestrates the full upload pipeline (source query → download → presign → S3 upload).
 */
@Service
public class FileUploadOrchestratorImpl implements FileUploadOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(FileUploadOrchestratorImpl.class);

	private final SourceQueryPort sourceQueryPort;
	private final SourceDownloadPort sourceDownloadPort;
	private final PresignUrlPort presignUrlPort;
	private final UploadPort uploadPort;

	/**
	 * Creates the orchestrator.
	 *
	 * @param sourceQueryPort    port for querying PDM/PLM source files
	 * @param sourceDownloadPort port for downloading file content from source
	 * @param presignUrlPort     port for obtaining presigned upload URLs
	 * @param uploadPort         port for uploading file content via presigned URL
	 */
	public FileUploadOrchestratorImpl(
			SourceQueryPort sourceQueryPort,
			SourceDownloadPort sourceDownloadPort,
			PresignUrlPort presignUrlPort,
			UploadPort uploadPort) {
		this.sourceQueryPort = sourceQueryPort;
		this.sourceDownloadPort = sourceDownloadPort;
		this.presignUrlPort = presignUrlPort;
		this.uploadPort = uploadPort;
	}

	/**
	 * Runs the upload flow for files matching the request criteria.
	 *
	 * @param request query criteria and upload parameters
	 * @return per-file success and failure outcome
	 */
	@Override
	public UploadResponse upload(UploadRequest request) {
		FileQueryCriteria criteria = toCriteria(request);
		List<SourceFile> sourceFiles = sourceQueryPort.query(criteria);

		if (sourceFiles.isEmpty()) {
			return UploadResponse.empty();
		}

		List<String> succeeded = new ArrayList<>();
		List<FailedFileResponse> failed = new ArrayList<>();

		for (SourceFile sourceFile : sourceFiles) {
			processFile(sourceFile, succeeded, failed);
		}

		return new UploadResponse(List.copyOf(succeeded), List.copyOf(failed));
	}

	private void processFile(SourceFile sourceFile, List<String> succeeded, List<FailedFileResponse> failed) {
		FileHandle file;
		try {
			file = sourceDownloadPort.download(sourceFile);
		} catch (Exception e) {
			log.warn("Failed to download file '{}' from source", sourceFile.fileName(), e);
			failed.add(new FailedFileResponse(sourceFile.fileName(), e.getMessage()));
			return;
		}

		PresignedUrl presignedUrl;
		try {
			presignedUrl = presignUrlPort.presign(file);
		} catch (Exception e) {
			log.warn("Failed to presign upload URL for file '{}'", file.fileName(), e);
			failed.add(new FailedFileResponse(file.fileName(), e.getMessage()));
			return;
		}

		UploadResult uploadResult = uploadPort.upload(file, presignedUrl);
		if (uploadResult.succeeded()) {
			succeeded.add(uploadResult.fileName());
		} else {
			failed.add(new FailedFileResponse(uploadResult.fileName(), uploadResult.failureReason()));
		}
	}

	private FileQueryCriteria toCriteria(UploadRequest request) {
		return new FileQueryCriteria(
			request.modelId(),
			request.orderId(),
			request.dateFrom(),
			request.dateTo());
	}
}
