package fi.ishtech.practice.kone.kofiorch.upload.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;
import fi.ishtech.practice.kone.kofiorch.upload.PresignUrlPort;
import fi.ishtech.practice.kone.kofiorch.upload.UploadPort;

@ExtendWith(MockitoExtension.class)
class S3UploadServiceTest {

	private static final String PRESIGNED_URL = "https://bucket.s3.amazonaws.com/part.stp?X-Amz-Signature=abc";

	@Mock
	private PresignUrlPort presignUrlPort;

	@Mock
	private UploadPort uploadPort;

	@InjectMocks
	private S3UploadService s3UploadService;

	@Test
	void uploadPresignsThenUploadsFile() {
		FileHandle file = new FileHandle("part.stp", "application/octet-stream", "content".getBytes());
		PresignedUrl presignedUrl = new PresignedUrl("part.stp", PRESIGNED_URL);
		UploadResult uploadResult = UploadResult.success("part.stp");

		when(presignUrlPort.presign(file)).thenReturn(presignedUrl);
		when(uploadPort.upload(file, presignedUrl)).thenReturn(uploadResult);

		UploadResult result = s3UploadService.upload(file);

		assertThat(result.succeeded()).isTrue();
		verify(presignUrlPort).presign(file);
		verify(uploadPort).upload(file, presignedUrl);
	}
}
