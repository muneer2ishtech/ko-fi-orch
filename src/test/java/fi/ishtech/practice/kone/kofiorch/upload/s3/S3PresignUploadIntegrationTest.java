package fi.ishtech.practice.kone.kofiorch.upload.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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

@EnabledIfDockerAvailable
@Testcontainers
class S3PresignUploadIntegrationTest {

	private static final String BUCKET = "ko-fi-orch-test";
	private static final String OBJECT_KEY = "integration/part.stp";

	@Container
	static LocalStackContainer localStack = new LocalStackContainer(
			DockerImageName.parse("localstack/localstack:4.4.0"))
		.withServices(LocalStackContainer.Service.S3);

	private static S3UploadService s3UploadService;
	private static S3Client s3Client;

	@BeforeAll
	static void setUp() {
		s3Client = S3Client.builder()
			.endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
			.region(Region.of(localStack.getRegion()))
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
			.build();

		s3Client.createBucket(builder -> builder.bucket(BUCKET));

		S3Presigner s3Presigner = S3Presigner.builder()
			.endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
			.region(Region.of(localStack.getRegion()))
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
			.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
			.build();

		S3Properties s3Properties = new S3Properties(localStack.getRegion(), BUCKET, Duration.ofMinutes(15));
		S3PresignUrlAdapter presignUrlAdapter = new S3PresignUrlAdapter(s3Presigner, s3Properties);
		S3PresignedUploadAdapter uploadAdapter = new S3PresignedUploadAdapter(RestClient.create());
		s3UploadService = new S3UploadService(presignUrlAdapter, uploadAdapter);
	}

	@Test
	void presignsUrlAndUploadsFileToS3() {
		byte[] content = "integration-test-content".getBytes();
		FileHandle file = new FileHandle(OBJECT_KEY, "application/octet-stream", content);

		UploadResult result = s3UploadService.upload(file);

		assertThat(result.succeeded()).isTrue();
		assertThat(result.fileName()).isEqualTo(OBJECT_KEY);
		assertThat(objectExists(OBJECT_KEY)).isTrue();
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
