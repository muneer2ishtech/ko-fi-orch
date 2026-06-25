package fi.ishtech.practice.kone.kofiorch.domain;

import java.time.LocalDate;

/**
 * Criteria for querying PDM/PLM source files.
 *
 * @param modelId  required model identifier
 * @param orderId  optional order identifier
 * @param dateFrom optional start of date range (inclusive)
 * @param dateTo   optional end of date range (inclusive)
 */
public record FileQueryCriteria(String modelId, String orderId, LocalDate dateFrom, LocalDate dateTo) {
}
