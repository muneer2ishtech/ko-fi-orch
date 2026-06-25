package fi.ishtech.practice.kone.kofiorch.source.rest;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import fi.ishtech.practice.kone.kofiorch.config.SourceProperties;
import fi.ishtech.practice.kone.kofiorch.domain.FileQueryCriteria;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;
import fi.ishtech.practice.kone.kofiorch.source.SourceQueryPort;
import fi.ishtech.practice.kone.kofiorch.source.rest.dto.SourceFileResponse;
import fi.ishtech.practice.kone.kofiorch.source.rest.dto.SourceFilesResponse;

/**
 * Queries the PDM/PLM source API via {@code GET /api/v1/files}.
 */
@Component
public class RestSourceQueryAdapter implements SourceQueryPort {

	private static final String FILES_PATH = "/api/v1/files";

	private final RestClient restClient;
	private final SourceProperties sourceProperties;

	/**
	 * Creates the adapter.
	 *
	 * @param restClient       HTTP client for source API calls
	 * @param sourceProperties source API base URL settings
	 */
	public RestSourceQueryAdapter(RestClient restClient, SourceProperties sourceProperties) {
		this.restClient = restClient;
		this.sourceProperties = sourceProperties;
	}

	/**
	 * Returns files matching the given criteria.
	 *
	 * @param criteria model, optional order, and optional date range
	 * @return matching source files; may be empty when none match
	 */
	@Override
	public List<SourceFile> query(FileQueryCriteria criteria) {
		SourceFilesResponse response = restClient.get()
			.uri(buildQueryUri(criteria))
			.retrieve()
			.body(SourceFilesResponse.class);

		if (response == null || response.files() == null) {
			return List.of();
		}
		return response.files().stream().map(this::toSourceFile).toList();
	}

	private String buildQueryUri(FileQueryCriteria criteria) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(sourceProperties.baseUrl())
			.path(FILES_PATH)
			.queryParam("modelId", criteria.modelId());

		if (criteria.orderId() != null && !criteria.orderId().isBlank()) {
			builder.queryParam("orderId", criteria.orderId());
		}
		if (criteria.dateFrom() != null) {
			builder.queryParam("dateFrom", criteria.dateFrom());
		}
		if (criteria.dateTo() != null) {
			builder.queryParam("dateTo", criteria.dateTo());
		}
		return builder.build().toUriString();
	}

	private SourceFile toSourceFile(SourceFileResponse response) {
		return new SourceFile(
			response.fileId(),
			response.fileName(),
			response.contentType(),
			response.revision(),
			response.lastModified());
	}
}
