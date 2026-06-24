package fi.ishtech.practice.kone.kofiorch.source.rest.dto;

import java.util.List;

/**
 * Response body for {@code GET /api/v1/files}.
 *
 * @param files matching file metadata entries
 */
public record SourceFilesResponse(List<SourceFileResponse> files) {
}
