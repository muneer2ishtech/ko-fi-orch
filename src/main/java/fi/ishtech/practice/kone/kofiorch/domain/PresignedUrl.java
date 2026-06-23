package fi.ishtech.practice.kone.kofiorch.domain;

import java.util.List;
import java.util.Map;

/**
 * Presigned URL for uploading a single file to object storage.
 *
 * @param fileName       name of the file this URL is valid for
 * @param url            presigned URL to PUT the file content to
 * @param signedHeaders  headers that must be sent with the PUT request
 */
public record PresignedUrl(String fileName, String url, Map<String, List<String>> signedHeaders) {

	public PresignedUrl(String fileName, String url) {
		this(fileName, url, Map.of());
	}
}
