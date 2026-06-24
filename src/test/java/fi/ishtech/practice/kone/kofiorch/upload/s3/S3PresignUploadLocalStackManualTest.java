package fi.ishtech.practice.kone.kofiorch.upload.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import fi.ishtech.practice.kone.kofiorch.config.S3Properties;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Manual end-to-end test against LocalStack at {@value #ENDPOINT}.
 * Start LocalStack first: {@code docker run -d -p 4566:4566 localstack/localstack:4.4.0}
 */
class S3PresignUploadLocalStackManualTest {

	private static final String ENDPOINT = "http://localhost:4566";
	private static final String BUCKET = "ko-fi-orch-test";
	private static final String OBJECT_KEY = "manual/part.stp";

	private static S3UploadService s3UploadService;
	private static S3Client s3Client;

	@BeforeAll
	static void setUp() {
		assumeTrue(localStackAvailable(), "LocalStack not running at " + ENDPOINT);

		s3Client = S3Client.builder()
			.endpointOverride(URI.create(ENDPOINT))
			.region(Region.US_EAST_1)
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
			.forcePathStyle(true)
			.build();

		s3Client.createBucket(builder -> builder.bucket(BUCKET));

		S3Presigner s3Presigner = S3Presigner.builder()
			.endpointOverride(URI.create(ENDPOINT))
			.region(Region.US_EAST_1)
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
			.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
			.build();

		S3Properties s3Properties = new S3Properties("us-east-1", BUCKET, Duration.ofMinutes(15));
		s3UploadService = new S3UploadService(
			new S3PresignUrlAdapter(s3Presigner, s3Properties),
			new S3PresignedUploadAdapter(RestClient.create()));
	}

	@Test
	void presignsUrlAndUploadsFileToLocalStack() {
		byte[] content = "manual-localstack-content".getBytes();
		FileHandle file = new FileHandle(OBJECT_KEY, "application/octet-stream", content);

		UploadResult result = s3UploadService.upload(file);

		assertThat(result.succeeded()).isTrue();
		assertThat(result.fileName()).isEqualTo(OBJECT_KEY);
		assertThat(objectExists(OBJECT_KEY)).isTrue();
	}

	private static boolean localStackAvailable() {
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT + "/_localstack/health")).GET().build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response.statusCode() == 200;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean objectExists(String key) {
		try {
			s3Client.headObject(HeadObjectRequest.builder().bucket(BUCKET).key(key).build());
			return true;
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {
				return false;
			}
			throw e;
		}
	}
}
