package fi.ishtech.practice.kone.kofiorch.upload;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;

/**
 * Issues presigned upload URLs for object storage destinations.
 */
public interface PresignUrlPort {

	/**
	 * Creates a presigned PUT URL for the given file.
	 *
	 * @param file file metadata used to build the storage key and headers
	 * @return presigned URL for uploading the file content
	 */
	PresignedUrl presign(FileHandle file);
}
