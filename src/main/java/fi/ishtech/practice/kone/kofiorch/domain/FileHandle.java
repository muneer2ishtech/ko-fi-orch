package fi.ishtech.practice.kone.kofiorch.domain;

/**
 * Downloaded file content ready to be uploaded.
 *
 * @param fileName    name of the file, used as the upload key
 * @param contentType MIME type of the file content
 * @param content     raw file bytes
 */
public record FileHandle(String fileName, String contentType, byte[] content) {
}
