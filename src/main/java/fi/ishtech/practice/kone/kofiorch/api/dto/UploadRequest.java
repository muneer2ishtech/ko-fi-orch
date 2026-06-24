package fi.ishtech.practice.kone.kofiorch.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

/**
 * Input for {@code POST /upload}.
 *
 * @param modelId  required model identifier to query source files
 * @param orderId  optional order identifier to narrow the query
 * @param dateFrom optional start of date range (inclusive); must be set with {@code dateTo}
 * @param dateTo   optional end of date range (inclusive); must be set with {@code dateFrom}
 */
public record UploadRequest(
		@NotBlank String modelId,
		String orderId,
		LocalDate dateFrom,
		LocalDate dateTo) {

	/**
	 * Validates that both date bounds are present or both omitted.
	 *
	 * @return {@code true} when the date range is complete or empty
	 */
	@AssertTrue(message = "dateFrom and dateTo must both be set or both omitted")
	public boolean isCompleteDateRange() {
		return (dateFrom == null) == (dateTo == null);
	}

	/**
	 * Validates that {@code dateFrom} is not after {@code dateTo}.
	 *
	 * @return {@code true} when the range is ordered correctly
	 */
	@AssertTrue(message = "dateFrom must be on or before dateTo")
	public boolean isValidDateRange() {
		if (dateFrom == null || dateTo == null) {
			return true;
		}
		return !dateFrom.isAfter(dateTo);
	}
}
