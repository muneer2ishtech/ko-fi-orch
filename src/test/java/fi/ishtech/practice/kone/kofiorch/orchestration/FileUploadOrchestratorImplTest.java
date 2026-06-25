package fi.ishtech.practice.kone.kofiorch.orchestration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fi.ishtech.practice.kone.kofiorch.api.dto.UploadRequest;
import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.FileQueryCriteria;
import fi.ishtech.practice.kone.kofiorch.domain.PresignedUrl;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;
import fi.ishtech.practice.kone.kofiorch.domain.UploadResult;
import fi.ishtech.practice.kone.kofiorch.source.SourceDownloadPort;
import fi.ishtech.practice.kone.kofiorch.source.SourceQueryPort;
import fi.ishtech.practice.kone.kofiorch.upload.PresignUrlPort;
import fi.ishtech.practice.kone.kofiorch.upload.UploadPort;

@ExtendWith(MockitoExtension.class)
class FileUploadOrchestratorImplTest {

	private static final String PRESIGNED_URL = "https://bucket.s3.amazonaws.com/part.stp?X-Amz-Signature=abc";

	@Mock
	private SourceQueryPort sourceQueryPort;

	@Mock
	private SourceDownloadPort sourceDownloadPort;

	@Mock
	private PresignUrlPort presignUrlPort;

	@Mock
	private UploadPort uploadPort;

	@InjectMocks
	private FileUploadOrchestratorImpl orchestrator;

	@Test
	void returnsEmptyResponseWhenNoFilesMatch() {
		when(sourceQueryPort.query(any(FileQueryCriteria.class))).thenReturn(List.of());

		var response = orchestrator.upload(new UploadRequest("MODEL-1", null, null, null));

		assertThat(response.succeeded()).isEmpty();
		assertThat(response.failed()).isEmpty();
		verify(sourceDownloadPort, never()).download(any());
	}

	@Test
	void uploadsAllMatchingFiles() {
		SourceFile sourceA = new SourceFile("doc-1", "part-a.stp", "application/octet-stream", "A", LocalDate.of(2026, 1, 1));
		SourceFile sourceB = new SourceFile("doc-2", "part-b.stp", "application/octet-stream", "B", LocalDate.of(2026, 1, 2));
		FileHandle fileA = new FileHandle("part-a.stp", "application/octet-stream", "a".getBytes());
		FileHandle fileB = new FileHandle("part-b.stp", "application/octet-stream", "b".getBytes());
		PresignedUrl presignedA = new PresignedUrl("part-a.stp", PRESIGNED_URL);
		PresignedUrl presignedB = new PresignedUrl("part-b.stp", PRESIGNED_URL);

		when(sourceQueryPort.query(any(FileQueryCriteria.class))).thenReturn(List.of(sourceA, sourceB));
		when(sourceDownloadPort.download(sourceA)).thenReturn(fileA);
		when(sourceDownloadPort.download(sourceB)).thenReturn(fileB);
		when(presignUrlPort.presign(fileA)).thenReturn(presignedA);
		when(presignUrlPort.presign(fileB)).thenReturn(presignedB);
		when(uploadPort.upload(fileA, presignedA)).thenReturn(UploadResult.success("part-a.stp"));
		when(uploadPort.upload(fileB, presignedB)).thenReturn(UploadResult.success("part-b.stp"));

		var response = orchestrator.upload(new UploadRequest("MODEL-1", "ORD-1", null, null));

		assertThat(response.succeeded()).containsExactly("part-a.stp", "part-b.stp");
		assertThat(response.failed()).isEmpty();
	}

	@Test
	void recordsFailureWhenDownloadFails() {
		SourceFile sourceFile = new SourceFile("doc-1", "part-a.stp", null, null, null);
		when(sourceQueryPort.query(any(FileQueryCriteria.class))).thenReturn(List.of(sourceFile));
		when(sourceDownloadPort.download(sourceFile)).thenThrow(new RuntimeException("source timeout"));

		var response = orchestrator.upload(new UploadRequest("MODEL-1", null, null, null));

		assertThat(response.succeeded()).isEmpty();
		assertThat(response.failed()).hasSize(1);
		assertThat(response.failed().get(0).fileName()).isEqualTo("part-a.stp");
		assertThat(response.failed().get(0).reason()).isEqualTo("source timeout");
		verify(presignUrlPort, never()).presign(any());
	}

	@Test
	void recordsFailureWhenUploadFails() {
		SourceFile sourceFile = new SourceFile("doc-1", "part-a.stp", "application/octet-stream", null, null);
		FileHandle file = new FileHandle("part-a.stp", "application/octet-stream", "a".getBytes());
		PresignedUrl presignedUrl = new PresignedUrl("part-a.stp", PRESIGNED_URL);

		when(sourceQueryPort.query(any(FileQueryCriteria.class))).thenReturn(List.of(sourceFile));
		when(sourceDownloadPort.download(sourceFile)).thenReturn(file);
		when(presignUrlPort.presign(file)).thenReturn(presignedUrl);
		when(uploadPort.upload(file, presignedUrl)).thenReturn(UploadResult.failure("part-a.stp", "S3 error"));

		var response = orchestrator.upload(new UploadRequest("MODEL-1", null, null, null));

		assertThat(response.succeeded()).isEmpty();
		assertThat(response.failed()).hasSize(1);
		assertThat(response.failed().get(0).reason()).isEqualTo("S3 error");
	}

	@Test
	void continuesProcessingAfterOneFileFails() {
		SourceFile sourceA = new SourceFile("doc-1", "part-a.stp", null, null, null);
		SourceFile sourceB = new SourceFile("doc-2", "part-b.stp", null, null, null);
		FileHandle fileB = new FileHandle("part-b.stp", "application/octet-stream", "b".getBytes());
		PresignedUrl presignedB = new PresignedUrl("part-b.stp", PRESIGNED_URL);

		when(sourceQueryPort.query(any(FileQueryCriteria.class))).thenReturn(List.of(sourceA, sourceB));
		when(sourceDownloadPort.download(sourceA)).thenThrow(new RuntimeException("download failed"));
		when(sourceDownloadPort.download(sourceB)).thenReturn(fileB);
		when(presignUrlPort.presign(fileB)).thenReturn(presignedB);
		when(uploadPort.upload(fileB, presignedB)).thenReturn(UploadResult.success("part-b.stp"));

		var response = orchestrator.upload(new UploadRequest("MODEL-1", null, null, null));

		assertThat(response.succeeded()).containsExactly("part-b.stp");
		assertThat(response.failed()).hasSize(1);
		assertThat(response.failed().get(0).fileName()).isEqualTo("part-a.stp");
	}
}
