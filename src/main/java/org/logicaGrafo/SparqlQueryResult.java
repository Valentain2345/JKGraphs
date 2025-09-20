package org.logicaGrafo;

import java.util.List;
import java.util.Map;

public class SparqlQueryResult {
    private List<String> variables;
    private List<Map<String, String>> rows;
    private String error;

    public SparqlQueryResult(List<String> variables, List<Map<String, String>> rows, String error) {
        this.variables = variables;
        this.rows = rows;
        this.error = error;
    }

    public List<String> getVariables() { return variables; }
    public List<Map<String, String>> getRows() { return rows; }
    public String getError() { return error; }
    public boolean hasError() { return error != null && !error.isEmpty(); }
}
