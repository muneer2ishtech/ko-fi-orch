package fi.ishtech.practice.kone.kofiorch.upload.rest.dto;

/**
 * Request body for {@code POST /api/v1/presigned-upload-urls}.
 *
 * @param fileName    object key / file name for the upload
 * @param contentType MIME type of the file content
 */
public record PresignedUploadUrlRequest(String fileName, String contentType) {
}
