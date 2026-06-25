package fi.ishtech.practice.kone.kofiorch.upload.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fi.ishtech.practice.kone.kofiorch.config.UploadApiProperties;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;

class RestUploadPresignUrlAdapterTest {

	private static final String BASE_URL = "http://upload-api.test";
	private static final String PRESIGNED_URL = "https://bucket.s3.amazonaws.com/part.stp?X-Amz-Signature=abc";

	private MockRestServiceServer mockServer;
	private RestUploadPresignUrlAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		adapter = new RestUploadPresignUrlAdapter(builder.build(), new UploadApiProperties(BASE_URL));
	}

	@Test
	void presignsUploadUrlForFile() {
		FileHandle file = new FileHandle("models/MODEL-42/part-a.stp", "application/octet-stream", new byte[0]);

		mockServer.expect(requestTo("http://upload-api.test/api/v1/presigned-upload-urls"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("""
				{
				  "fileName": "models/MODEL-42/part-a.stp",
				  "contentType": "application/octet-stream"
				}
				"""))
			.andRespond(withSuccess("""
				{
				  "fileName": "models/MODEL-42/part-a.stp",
				  "url": "%s",
				  "signedHeaders": {
				    "Content-Type": ["application/octet-stream"]
				  }
				}
				""".formatted(PRESIGNED_URL), MediaType.APPLICATION_JSON));

		PresignedUrl result = adapter.presign(file);

		assertThat(result.fileName()).isEqualTo("models/MODEL-42/part-a.stp");
		assertThat(result.url()).isEqualTo(PRESIGNED_URL);
		assertThat(result.signedHeaders()).containsKey("Content-Type");
		mockServer.verify();
	}

	@Test
	void presignsUploadUrlWithoutSignedHeaders() {
		FileHandle file = new FileHandle("part.stp", "application/octet-stream", new byte[0]);

		mockServer.expect(requestTo("http://upload-api.test/api/v1/presigned-upload-urls"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess("""
				{
				  "fileName": "part.stp",
				  "url": "%s"
				}
				""".formatted(PRESIGNED_URL), MediaType.APPLICATION_JSON));

		PresignedUrl result = adapter.presign(file);

		assertThat(result.signedHeaders()).isEmpty();
		mockServer.verify();
	}

	@Test
	void defaultsContentTypeWhenMissing() {
		FileHandle file = new FileHandle("part.stp", null, new byte[0]);

		mockServer.expect(requestTo("http://upload-api.test/api/v1/presigned-upload-urls"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().json("""
				{
				  "fileName": "part.stp",
				  "contentType": "application/octet-stream"
				}
				"""))
			.andRespond(withSuccess("""
				{
				  "fileName": "part.stp",
				  "url": "%s"
				}
				""".formatted(PRESIGNED_URL), MediaType.APPLICATION_JSON));

		adapter.presign(file);

		mockServer.verify();
	}

	@Test
	void throwsWhenUploadApiRespondsWithError() {
		FileHandle file = new FileHandle("part.stp", "application/octet-stream", new byte[0]);

		mockServer.expect(requestTo("http://upload-api.test/api/v1/presigned-upload-urls"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withServerError());

		assertThatThrownBy(() -> adapter.presign(file))
			.isInstanceOf(RestClientException.class);
	}
}
