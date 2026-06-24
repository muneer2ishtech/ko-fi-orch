package fi.ishtech.practice.kone.kofiorch.source.rest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import fi.ishtech.practice.kone.kofiorch.config.SourceProperties;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;
import fi.ishtech.practice.kone.kofiorch.source.SourceDownloadPort;

/**
 * Downloads file content from the PDM/PLM source API via {@code GET /api/v1/files/{fileId}/content}.
 */
@Component
public class RestSourceDownloadAdapter implements SourceDownloadPort {

	private static final String FILE_CONTENT_PATH = "/api/v1/files/{fileId}/content";

	private final RestClient restClient;
	private final SourceProperties sourceProperties;

	/**
	 * Creates the adapter.
	 *
	 * @param restClient       HTTP client for source API calls
	 * @param sourceProperties source API base URL settings
	 */
	public RestSourceDownloadAdapter(RestClient restClient, SourceProperties sourceProperties) {
		this.restClient = restClient;
		this.sourceProperties = sourceProperties;
	}

	/**
	 * Downloads the content of the given source file.
	 *
	 * @param sourceFile file metadata from the source query
	 * @return file bytes ready for upload
	 */
	@Override
	public FileHandle download(SourceFile sourceFile) {
		byte[] content = restClient.get()
			.uri(buildContentUri(sourceFile.fileId()))
			.retrieve()
			.body(byte[].class);

		return new FileHandle(
			sourceFile.fileName(),
			resolveContentType(sourceFile.contentType()),
			content);
	}

	private String buildContentUri(String fileId) {
		return UriComponentsBuilder.fromUriString(sourceProperties.baseUrl())
			.path(FILE_CONTENT_PATH)
			.buildAndExpand(fileId)
			.toUriString();
	}

	private String resolveContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		return contentType;
	}
}
