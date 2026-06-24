package fi.ishtech.practice.kone.kofiorch.domain;

/**
 * Outcome of uploading a single file.
 *
 * @param fileName      name of the file that was uploaded
 * @param succeeded     whether the upload succeeded
 * @param failureReason reason for failure, {@code null} when {@code succeeded} is {@code true}
 */
public record UploadResult(String fileName, boolean succeeded, String failureReason) {

	/**
	 * Creates a successful upload result.
	 *
	 * @param fileName name of the uploaded file
	 * @return successful upload result
	 */
	public static UploadResult success(String fileName) {
		return new UploadResult(fileName, true, null);
	}

	/**
	 * Creates a failed upload result.
	 *
	 * @param fileName      name of the file that failed to upload
	 * @param failureReason description of why the upload failed
	 * @return failed upload result
	 */
	public static UploadResult failure(String fileName, String failureReason) {
		return new UploadResult(fileName, false, failureReason);
	}
}
