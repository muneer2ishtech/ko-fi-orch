package fi.ishtech.practice.kone.kofiorch.source.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fi.ishtech.practice.kone.kofiorch.config.SourceProperties;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;

class RestSourceDownloadAdapterTest {

	private static final String BASE_URL = "http://source-api.test";

	private MockRestServiceServer mockServer;
	private RestSourceDownloadAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		adapter = new RestSourceDownloadAdapter(builder.build(), new SourceProperties(BASE_URL));
	}

	@Test
	void downloadsFileContent() {
		SourceFile sourceFile = new SourceFile(
			"doc-1001",
			"models/MODEL-42/part-a.stp",
			"application/octet-stream",
			"A",
			LocalDate.of(2026, 1, 15));
		byte[] content = "stp-file-content".getBytes();

		mockServer.expect(requestTo("http://source-api.test/api/v1/files/doc-1001/content"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(content, MediaType.APPLICATION_OCTET_STREAM));

		FileHandle file = adapter.download(sourceFile);

		assertThat(file.fileName()).isEqualTo("models/MODEL-42/part-a.stp");
		assertThat(file.contentType()).isEqualTo("application/octet-stream");
		assertThat(file.content()).isEqualTo(content);
		mockServer.verify();
	}

	@Test
	void defaultsContentTypeWhenMissing() {
		SourceFile sourceFile = new SourceFile("doc-1001", "models/MODEL-42/part-a.stp", null, "A", null);

		mockServer.expect(requestTo("http://source-api.test/api/v1/files/doc-1001/content"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("content".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

		FileHandle file = adapter.download(sourceFile);

		assertThat(file.contentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
	}

	@Test
	void throwsWhenSourceRespondsWithError() {
		SourceFile sourceFile = new SourceFile("doc-1001", "models/MODEL-42/part-a.stp", null, null, null);

		mockServer.expect(requestTo("http://source-api.test/api/v1/files/doc-1001/content"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withServerError());

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> adapter.download(sourceFile))
			.isInstanceOf(RestClientException.class);
	}
}
