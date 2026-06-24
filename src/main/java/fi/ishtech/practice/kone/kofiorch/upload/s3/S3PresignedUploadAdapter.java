package fi.ishtech.practice.kone.kofiorch.upload.s3;

import java.net.URI;
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

	/**
	 * Creates the adapter.
	 *
	 * @param restClient HTTP client used for presigned PUT requests
	 */
	public S3PresignedUploadAdapter(RestClient restClient) {
		this.restClient = restClient;
	}

	/**
	 * Uploads file bytes to the presigned URL.
	 *
	 * @param file         file content to upload
	 * @param presignedUrl presigned URL and optional signed headers
	 * @return success or failure result; HTTP errors are not thrown
	 */
	@Override
	public UploadResult upload(FileHandle file, PresignedUrl presignedUrl) {
		try {
			RestClient.RequestBodySpec request = restClient.put().uri(URI.create(presignedUrl.url()));

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

	/**
	 * Copies signed headers onto the request.
	 *
	 * @param headers       mutable request headers
	 * @param signedHeaders headers required by the presigned URL
	 */
	private void applySignedHeaders(HttpHeaders headers, Map<String, List<String>> signedHeaders) {
		signedHeaders.forEach((name, values) -> {
			if ("host".equalsIgnoreCase(name)) {
				return;
			}
			values.forEach(value -> headers.add(name, value));
		});
	}

	/**
	 * Resolves the content type for the PUT request.
	 *
	 * @param contentType MIME type from the file, may be blank
	 * @return resolved {@link MediaType}, defaulting to octet-stream
	 */
	private MediaType resolveContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
		return MediaType.parseMediaType(contentType);
	}
}
