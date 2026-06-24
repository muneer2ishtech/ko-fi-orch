package fi.ishtech.practice.kone.kofiorch.api.dto;

/**
 * A file that failed to upload.
 *
 * @param fileName name of the file
 * @param reason   why the upload failed
 */
public record FailedFileResponse(String fileName, String reason) {
}
