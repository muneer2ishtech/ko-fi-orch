package fi.ishtech.practice.kone.kofiorch.source.rest.dto;

import java.time.LocalDate;

/**
 * File metadata element in the source API query response.
 *
 * @param fileId       source document identifier
 * @param fileName     file path or name
 * @param contentType  MIME type
 * @param revision     document revision
 * @param lastModified last modified or release date
 */
public record SourceFileResponse(
		String fileId,
		String fileName,
		String contentType,
		String revision,
		LocalDate lastModified) {
}
