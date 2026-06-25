package fi.ishtech.practice.kone.kofiorch.domain;

/**
 * File content ready to be uploaded to object storage.
 *
 * @param fileName    object key / file name used for the upload
 * @param contentType MIME type of the file content
 * @param content     raw file bytes
 */
public record FileHandle(String fileName, String contentType, byte[] content) {
}
