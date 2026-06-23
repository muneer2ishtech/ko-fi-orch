package fi.ishtech.practice.kone.kofiorch.upload.s3;

import org.springframework.stereotype.Service;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;
import fi.ishtech.practice.kone.kofiorch.upload.PresignUrlPort;
import fi.ishtech.practice.kone.kofiorch.upload.UploadPort;

/**
 * Orchestrates S3 upload: presign a PUT URL, then upload file bytes to it.
 */
@Service
public class S3UploadService {

	private final PresignUrlPort presignUrlPort;
	private final UploadPort uploadPort;

	public S3UploadService(PresignUrlPort presignUrlPort, UploadPort uploadPort) {
		this.presignUrlPort = presignUrlPort;
		this.uploadPort = uploadPort;
	}

	public PresignedUrl presign(FileHandle file) {
		return presignUrlPort.presign(file);
	}

	public UploadResult upload(FileHandle file) {
		PresignedUrl presignedUrl = presignUrlPort.presign(file);
		return uploadPort.upload(file, presignedUrl);
	}
}
