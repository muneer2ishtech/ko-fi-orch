package fi.ishtech.practice.kone.kofiorch.source;

import fi.ishtech.practice.kone.kofiorch.domain.FileHandle;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;

/**
 * Downloads file content from the PDM/PLM source system.
 */
public interface SourceDownloadPort {

	/**
	 * Downloads the content of the given source file.
	 *
	 * @param sourceFile file metadata from {@link SourceQueryPort}
	 * @return file bytes ready for upload
	 */
	FileHandle download(SourceFile sourceFile);
}
