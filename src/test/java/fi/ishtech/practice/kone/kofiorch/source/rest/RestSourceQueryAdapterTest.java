package fi.ishtech.practice.kone.kofiorch.source.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import fi.ishtech.practice.kone.kofiorch.config.SourceProperties;
import fi.ishtech.practice.kone.kofiorch.domain.FileQueryCriteria;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;

class RestSourceQueryAdapterTest {

	private static final String BASE_URL = "http://source-api.test";

	private MockRestServiceServer mockServer;
	private RestSourceQueryAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		adapter = new RestSourceQueryAdapter(builder.build(), new SourceProperties(BASE_URL));
	}

	@Test
	void queriesFilesWithAllCriteria() {
		FileQueryCriteria criteria = new FileQueryCriteria(
			"MODEL-42", "ORD-7", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

		mockServer.expect(requestTo(
				"http://source-api.test/api/v1/files?modelId=MODEL-42&orderId=ORD-7&dateFrom=2026-01-01&dateTo=2026-01-31"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("""
				{
				  "files": [
				    {
				      "fileId": "doc-1001",
				      "fileName": "models/MODEL-42/part-a.stp",
				      "contentType": "application/octet-stream",
				      "revision": "A",
				      "lastModified": "2026-01-15"
				    }
				  ]
				}
				""", MediaType.APPLICATION_JSON));

		List<SourceFile> files = adapter.query(criteria);

		assertThat(files).hasSize(1);
		assertThat(files.get(0).fileId()).isEqualTo("doc-1001");
		assertThat(files.get(0).fileName()).isEqualTo("models/MODEL-42/part-a.stp");
		assertThat(files.get(0).revision()).isEqualTo("A");
		assertThat(files.get(0).lastModified()).isEqualTo(LocalDate.of(2026, 1, 15));
		mockServer.verify();
	}

	@Test
	void queriesFilesWithModelIdOnly() {
		FileQueryCriteria criteria = new FileQueryCriteria("MODEL-42", null, null, null);

		mockServer.expect(requestTo("http://source-api.test/api/v1/files?modelId=MODEL-42"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("""
				{
				  "files": []
				}
				""", MediaType.APPLICATION_JSON));

		List<SourceFile> files = adapter.query(criteria);

		assertThat(files).isEmpty();
		mockServer.verify();
	}

	@Test
	void returnsEmptyListWhenResponseBodyIsNull() {
		FileQueryCriteria criteria = new FileQueryCriteria("MODEL-42", null, null, null);

		mockServer.expect(requestTo("http://source-api.test/api/v1/files?modelId=MODEL-42"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("", MediaType.APPLICATION_JSON));

		List<SourceFile> files = adapter.query(criteria);

		assertThat(files).isEmpty();
	}
}
