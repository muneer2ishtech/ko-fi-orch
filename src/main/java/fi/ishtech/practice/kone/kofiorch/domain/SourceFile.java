package fi.ishtech.practice.kone.kofiorch.domain;

import java.time.LocalDate;

/**
 * File metadata returned from the PDM/PLM source system.
 *
 * @param fileId       source document identifier
 * @param fileName     file path or name in the source system
 * @param contentType  MIME type of the file content
 * @param revision     document revision (e.g. CAD revision)
 * @param lastModified last modified or release date in the source system
 */
public record SourceFile(
		String fileId,
		String fileName,
		String contentType,
		String revision,
		LocalDate lastModified) {
}
