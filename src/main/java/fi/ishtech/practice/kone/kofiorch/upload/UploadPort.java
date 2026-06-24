package fi.ishtech.practice.kone.kofiorch.upload;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;

/**
 * Uploads file content to a storage destination using a presigned URL.
 * Implementations isolate the specifics of the destination (e.g. S3) from
 * orchestration logic.
 */
public interface UploadPort {

	/**
	 * Uploads the given file content to the location identified by the
	 * presigned URL.
	 *
	 * @param file         file content to upload
	 * @param presignedUrl presigned URL to upload to
	 * @return result of the upload attempt; never throws for upload failures,
	 *         they are reported via {@link UploadResult#succeeded()}
	 */
	UploadResult upload(FileHandle file, PresignedUrl presignedUrl);
}
