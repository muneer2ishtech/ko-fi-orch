package fi.ishtech.practice.kone.kofiorch.api.dto;

import java.util.List;

/**
 * Result of {@code POST /upload}.
 *
 * @param succeeded file names uploaded successfully
 * @param failed    files that failed to upload, with reasons
 */
public record UploadResponse(List<String> succeeded, List<FailedFileResponse> failed) {

	/**
	 * Creates an empty response (no files processed).
	 *
	 * @return response with empty succeeded and failed lists
	 */
	public static UploadResponse empty() {
		return new UploadResponse(List.of(), List.of());
	}
}
