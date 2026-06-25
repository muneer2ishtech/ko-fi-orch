package fi.ishtech.practice.kone.kofiorch.upload.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;

class S3PresignedUploadAdapterTest {

	private static final String PRESIGNED_URL = "https://bucket.s3.amazonaws.com/part.stp?X-Amz-Signature=abc";

	private MockRestServiceServer mockServer;
	private S3PresignedUploadAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		adapter = new S3PresignedUploadAdapter(builder.build());
	}

	@Test
	void uploadsFileContentViaHttpPut() {
		byte[] content = "stp-file-content".getBytes();
		FileHandle file = new FileHandle("part.stp", "application/octet-stream", content);
		PresignedUrl presignedUrl = new PresignedUrl("part.stp", PRESIGNED_URL);

		mockServer.expect(requestTo(PRESIGNED_URL))
			.andExpect(method(HttpMethod.PUT))
			.andExpect(content().bytes(content))
			.andRespond(withSuccess());

		UploadResult result = adapter.upload(file, presignedUrl);

		assertThat(result.succeeded()).isTrue();
		assertThat(result.fileName()).isEqualTo("part.stp");
		assertThat(result.failureReason()).isNull();
		mockServer.verify();
	}

	@Test
	void returnsFailureResultWhenS3RespondsWithError() {
		FileHandle file = new FileHandle("part.stp", "application/octet-stream", "content".getBytes());
		PresignedUrl presignedUrl = new PresignedUrl("part.stp", PRESIGNED_URL);

		mockServer.expect(requestTo(PRESIGNED_URL))
			.andExpect(method(HttpMethod.PUT))
			.andRespond(withServerError());

		UploadResult result = adapter.upload(file, presignedUrl);

		assertThat(result.succeeded()).isFalse();
		assertThat(result.fileName()).isEqualTo("part.stp");
		assertThat(result.failureReason()).isNotBlank();
	}
}
