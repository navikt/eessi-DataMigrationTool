package eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report;

import java.util.HashMap;
import java.util.Map;

public class CasesSummaryReportError {

    private Map<String, Long> es_data;
    private Map<String, Long> non_es_data;

    public CasesSummaryReportError() {
        es_data = new HashMap<>();
        non_es_data = new HashMap<>();
    }

    public Map<String, Long> getEs_data() {
        return es_data;
    }

    public Map<String, Long> getNon_es_data() {
        return non_es_data;
    }
}