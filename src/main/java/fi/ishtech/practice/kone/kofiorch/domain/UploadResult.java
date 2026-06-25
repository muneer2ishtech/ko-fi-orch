package fi.ishtech.practice.kone.kofiorch.domain;

/**
 * Outcome of uploading a single file.
 *
 * @param fileName      name of the file that was uploaded
 * @param succeeded     whether the upload succeeded
 * @param failureReason reason for failure, null when {@code succeeded} is true
 */
public record UploadResult(String fileName, boolean succeeded, String failureReason) {

	public static UploadResult success(String fileName) {
		return new UploadResult(fileName, true, null);
	}

	public static UploadResult failure(String fileName, String failureReason) {
		return new UploadResult(fileName, false, failureReason);
	}
}
