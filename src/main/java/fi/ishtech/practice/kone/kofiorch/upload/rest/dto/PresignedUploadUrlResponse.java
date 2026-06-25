package fi.ishtech.practice.kone.kofiorch.upload.rest.dto;

import java.util.List;
import java.util.Map;

/**
 * Response body for {@code POST /api/v1/presigned-upload-urls}.
 *
 * @param fileName      file name the presigned URL is valid for
 * @param url           presigned URL to PUT the file content to
 * @param signedHeaders headers that must be sent with the PUT request; may be empty
 */
public record PresignedUploadUrlResponse(
		String fileName,
		String url,
		Map<String, List<String>> signedHeaders) {
}
