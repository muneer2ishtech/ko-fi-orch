package fi.ishtech.practice.kone.kofiorch.domain;

/**
 * Presigned URL issued by the upload API for a single file.
 *
 * @param fileName name of the file this URL is valid for
 * @param url      presigned URL to PUT the file content to
 */
public record PresignedUrl(String fileName, String url) {
}
