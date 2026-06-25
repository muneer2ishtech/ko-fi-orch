package fi.ishtech.practice.kone.kofiorch.upload.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import fi.ishtech.practice.kone.kofiorch.config.S3Properties;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

class S3PresignUrlAdapterTest {

	private static final String PRESIGNED_URL =
		"https://test-bucket.s3.eu-west-1.amazonaws.com/part.stp?X-Amz-Signature=abc";

	private S3Presigner s3Presigner;
	private S3PresignUrlAdapter adapter;

	@BeforeEach
	void setUp() {
		s3Presigner = mock(S3Presigner.class);
		S3Properties s3Properties = new S3Properties("eu-west-1", "test-bucket", Duration.ofMinutes(15));
		adapter = new S3PresignUrlAdapter(s3Presigner, s3Properties);
	}

	@Test
	void presignsPutObjectUrlForFile() {
		PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
		when(presignedRequest.url()).thenReturn(toUrl(PRESIGNED_URL));
		when(presignedRequest.httpRequest()).thenReturn(signedHttpRequest());
		when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

		FileHandle file = new FileHandle("part.stp", "application/octet-stream", new byte[0]);
		PresignedUrl result = adapter.presign(file);

		assertThat(result.fileName()).isEqualTo("part.stp");
		assertThat(result.url()).isEqualTo(PRESIGNED_URL);

		ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
		verify(s3Presigner).presignPutObject(captor.capture());
		assertThat(captor.getValue().putObjectRequest().bucket()).isEqualTo("test-bucket");
		assertThat(captor.getValue().putObjectRequest().key()).isEqualTo("part.stp");
		assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("application/octet-stream");
		assertThat(captor.getValue().signatureDuration()).isEqualTo(Duration.ofMinutes(15));
	}

	@Test
	void defaultsContentTypeWhenMissing() {
		PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
		when(presignedRequest.url()).thenReturn(toUrl(PRESIGNED_URL));
		when(presignedRequest.httpRequest()).thenReturn(signedHttpRequest());
		when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

		adapter.presign(new FileHandle("part.stp", null, new byte[0]));

		ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
		verify(s3Presigner).presignPutObject(captor.capture());
		assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("application/octet-stream");
	}

	private static URL toUrl(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static SdkHttpRequest signedHttpRequest() {
		return SdkHttpRequest.builder()
			.method(SdkHttpMethod.PUT)
			.uri(URI.create(PRESIGNED_URL))
			.putHeader("Content-Type", "application/octet-stream")
			.build();
	}
}
