package fi.ishtech.practice.kone.kofiorch.upload.s3;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;
import fi.ishtech.practice.kone.kofiorch.upload.UploadPort;

/**
 * Uploads file content to S3 by issuing an HTTP PUT to a presigned URL.
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
			RestClient.RequestBodySpec request = restClient.put().uri(presignedUrl.url());

			if (!presignedUrl.signedHeaders().isEmpty()) {
				request.headers(headers -> applySignedHeaders(headers, presignedUrl.signedHeaders()));
			} else {
				request.contentType(resolveContentType(file.contentType()));
			}

			request.body(file.content())
				.retrieve()
				.toBodilessEntity();

			return UploadResult.success(file.fileName());
		} catch (RestClientException e) {
			log.warn("Failed to upload file '{}' to S3", file.fileName(), e);
			return UploadResult.failure(file.fileName(), e.getMessage());
		}
	}

	private void applySignedHeaders(HttpHeaders headers, Map<String, List<String>> signedHeaders) {
		signedHeaders.forEach((name, values) -> {
			if ("host".equalsIgnoreCase(name)) {
				return;
			}
			values.forEach(value -> headers.add(name, value));
		});
	}

	private MediaType resolveContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
		return MediaType.parseMediaType(contentType);
	}
}
