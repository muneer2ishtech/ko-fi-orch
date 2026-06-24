package fi.ishtech.practice.kone.kofiorch.source;

import java.util.List;

import fi.ishtech.practice.kone.kofiorch.domain.FileQueryCriteria;
import fi.ishtech.practice.kone.kofiorch.domain.SourceFile;

/**
 * Queries the PDM/PLM source system for file metadata.
 */
public interface SourceQueryPort {

	/**
	 * Returns files matching the given criteria.
	 *
	 * @param criteria model, optional order, and optional date range
	 * @return matching source files; may be empty when none match
	 */
	List<SourceFile> query(FileQueryCriteria criteria);
}
