package fi.ishtech.practice.kone.kofiorch.upload.s3;

import org.springframework.stereotype.Component;

import fi.ishtech.practice.kone.kofiorch.config.S3Properties;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.upload.PresignUrlPort;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * Generates S3 presigned PUT URLs using the AWS SDK {@link S3Presigner}.
 */
@Component
public class S3PresignUrlAdapter implements PresignUrlPort {

	private final S3Presigner s3Presigner;
	private final S3Properties s3Properties;

	public S3PresignUrlAdapter(S3Presigner s3Presigner, S3Properties s3Properties) {
		this.s3Presigner = s3Presigner;
		this.s3Properties = s3Properties;
	}

	@Override
	public PresignedUrl presign(FileHandle file) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(s3Properties.bucket())
			.key(file.fileName())
			.contentType(resolveContentType(file.contentType()))
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(s3Properties.presignDuration())
			.putObjectRequest(putObjectRequest)
			.build();

		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

		return new PresignedUrl(
			file.fileName(),
			presignedRequest.url().toString(),
			presignedRequest.httpRequest().headers());
	}

	private String resolveContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return "application/octet-stream";
		}
		return contentType;
	}
}
