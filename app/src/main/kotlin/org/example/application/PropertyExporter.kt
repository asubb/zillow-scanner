package org.example.application

import org.example.domain.ExportResult
import org.example.domain.Property

interface PropertyExporter {
    /**
     * Exports a list of properties to the target destination (e.g., Google Sheets).
     * @param properties The list of properties to export.
     * @param dryRun If true, does not perform actual write operations.
     * @return ExportResult containing counts of inserted/updated/failed records.
     */
    fun export(properties: List<Property>, dryRun: Boolean = false): ExportResult
}
