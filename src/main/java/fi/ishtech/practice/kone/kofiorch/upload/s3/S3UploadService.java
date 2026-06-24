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

	/**
	 * Creates the service.
	 *
	 * @param presignUrlPort port used to obtain presigned upload URLs
	 * @param uploadPort     port used to upload file content via presigned URL
	 */
	public S3UploadService(PresignUrlPort presignUrlPort, UploadPort uploadPort) {
		this.presignUrlPort = presignUrlPort;
		this.uploadPort = uploadPort;
	}

	/**
	 * Returns a presigned PUT URL for the given file.
	 *
	 * @param file file metadata used to build the presigned URL
	 * @return presigned URL for uploading the file content
	 */
	public PresignedUrl presign(FileHandle file) {
		return presignUrlPort.presign(file);
	}

	/**
	 * Presigns a URL and uploads the file content to S3.
	 *
	 * @param file file content to upload
	 * @return result of the upload attempt
	 */
	public UploadResult upload(FileHandle file) {
		PresignedUrl presignedUrl = presignUrlPort.presign(file);
		return uploadPort.upload(file, presignedUrl);
	}
}
