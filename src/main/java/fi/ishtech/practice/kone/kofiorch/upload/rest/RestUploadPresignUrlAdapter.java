package fi.ishtech.practice.kone.kofiorch.upload.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import fi.ishtech.practice.kone.kofiorch.config.UploadApiProperties;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.upload.PresignUrlPort;
import fi.ishtech.practice.kone.kofiorch.upload.rest.dto.PresignedUploadUrlRequest;
import fi.ishtech.practice.kone.kofiorch.upload.rest.dto.PresignedUploadUrlResponse;

/**
 * Obtains presigned upload URLs from the external upload API via
 * {@code POST /api/v1/presigned-upload-urls}.
 */
@Component
public class RestUploadPresignUrlAdapter implements PresignUrlPort {

	private static final String PRESIGNED_URLS_PATH = "/api/v1/presigned-upload-urls";

	private final RestClient restClient;
	private final UploadApiProperties uploadApiProperties;

	/**
	 * Creates the adapter.
	 *
	 * @param restClient          HTTP client for upload API calls
	 * @param uploadApiProperties upload API base URL settings
	 */
	public RestUploadPresignUrlAdapter(RestClient restClient, UploadApiProperties uploadApiProperties) {
		this.restClient = restClient;
		this.uploadApiProperties = uploadApiProperties;
	}

	/**
	 * Requests a presigned PUT URL for the given file from the upload API.
	 *
	 * @param file file metadata used to build the presign request
	 * @return presigned URL with any required signed headers
	 */
	@Override
	public PresignedUrl presign(FileHandle file) {
		PresignedUploadUrlRequest request = new PresignedUploadUrlRequest(
			file.fileName(),
			resolveContentType(file.contentType()));

		PresignedUploadUrlResponse response = restClient.post()
			.uri(buildPresignUri())
			.contentType(MediaType.APPLICATION_JSON)
			.body(request)
			.retrieve()
			.body(PresignedUploadUrlResponse.class);

		if (response == null) {
			throw new IllegalStateException("Upload API returned empty presign response for file '" + file.fileName() + "'");
		}

		return new PresignedUrl(
			response.fileName(),
			response.url(),
			resolveSignedHeaders(response.signedHeaders()));
	}

	private String buildPresignUri() {
		return UriComponentsBuilder.fromUriString(uploadApiProperties.baseUrl())
			.path(PRESIGNED_URLS_PATH)
			.build()
			.toUriString();
	}

	private Map<String, List<String>> resolveSignedHeaders(Map<String, List<String>> signedHeaders) {
		if (signedHeaders == null) {
			return Map.of();
		}
		return signedHeaders;
	}

	private String resolveContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		return contentType;
	}
}
