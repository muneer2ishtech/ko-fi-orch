package fi.ishtech.practice.kone.kofiorch.upload.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;
import fi.ishtech.practice.kone.kofiorch.upload.UploadPort;

/**
 * Uploads file content to S3 by issuing an HTTP PUT to a presigned URL, as
 * required by the S3 presigned-URL upload protocol.
 */
@Component
public class S3PresignedUploadAdapter implements UploadPort {

	private static final Logger log = LoggerFactory.getLogger(S3PresignedUploadAdapter.class);

	private final RestClient restClient;

	public S3PresignedUploadAdapter(RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public UploadResult upload(FileHandle file, PresignedUrl presignedUrl) {
		try {
			restClient.put()
				.uri(presignedUrl.url())
				.contentType(resolveContentType(file.contentType()))
				.body(file.content())
				.retrieve()
				.toBodilessEntity();

			return UploadResult.success(file.fileName());
		} catch (RestClientException e) {
			log.warn("Failed to upload file '{}' to S3", file.fileName(), e);
			return UploadResult.failure(file.fileName(), e.getMessage());
		}
	}

	private MediaType resolveContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
		return MediaType.parseMediaType(contentType);
	}
}
