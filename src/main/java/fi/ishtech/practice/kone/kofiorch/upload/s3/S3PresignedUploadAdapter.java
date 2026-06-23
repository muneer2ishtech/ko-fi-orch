package fi.ishtech.practice.kone.kofiorch.upload.s3;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

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
 * Uploads file content to S3 by issuing an HTTP PUT to a presigned URL.
 */
@Component
public class S3PresignedUploadAdapter implements UploadPort {

	private static final Logger log = LoggerFactory.getLogger(S3PresignedUploadAdapter.class);

	private final RestClient restClient;
	private final HttpClient httpClient;

	public S3PresignedUploadAdapter(RestClient restClient) {
		this.restClient = restClient;
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
	}

	@Override
	public UploadResult upload(FileHandle file, PresignedUrl presignedUrl) {
		try {
			if (!presignedUrl.signedHeaders().isEmpty()) {
				uploadWithSignedHeaders(file, presignedUrl);
			} else {
				uploadWithRestClient(file, presignedUrl);
			}
			return UploadResult.success(file.fileName());
		} catch (RestClientException | InterruptedException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			log.warn("Failed to upload file '{}' to S3", file.fileName(), e);
			return UploadResult.failure(file.fileName(), e.getMessage());
		} catch (Exception e) {
			log.warn("Failed to upload file '{}' to S3", file.fileName(), e);
			return UploadResult.failure(file.fileName(), e.getMessage());
		}
	}

	private void uploadWithSignedHeaders(FileHandle file, PresignedUrl presignedUrl) throws Exception {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create(presignedUrl.url()))
			.PUT(HttpRequest.BodyPublishers.ofByteArray(file.content()));

		applySignedHeaders(requestBuilder, presignedUrl.signedHeaders());

		HttpResponse<Void> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.discarding());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new RestClientException("S3 upload failed with status " + response.statusCode());
		}
	}

	private void applySignedHeaders(HttpRequest.Builder requestBuilder, Map<String, List<String>> signedHeaders) {
		signedHeaders.forEach((name, values) -> {
			if ("host".equalsIgnoreCase(name)) {
				return;
			}
			values.forEach(value -> requestBuilder.header(name, value));
		});
	}

	private void uploadWithRestClient(FileHandle file, PresignedUrl presignedUrl) {
		restClient.put()
			.uri(presignedUrl.url())
			.contentType(resolveContentType(file.contentType()))
			.body(file.content())
			.retrieve()
			.toBodilessEntity();
	}

	private MediaType resolveContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
		return MediaType.parseMediaType(contentType);
	}
}
