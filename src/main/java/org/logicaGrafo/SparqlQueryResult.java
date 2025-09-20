package org.logicaGrafo;

import org.apache.jena.rdf.model.Model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SparqlQueryResult {
    private final List<String> variables;
    private final List<Map<String, String>> rows;
    private final String error;
    private final boolean isConstruct;
    private final Model constructedModel;

    // Constructor for SELECT queries
    public SparqlQueryResult(List<String> variables, List<Map<String, String>> rows, String error) {
        this.variables = variables;
        this.rows = rows;
        this.error = error;
        this.isConstruct = false;
        this.constructedModel = null;
    }

    // Constructor for CONSTRUCT queries
    public SparqlQueryResult(Model constructedModel, String error) {
        this.variables = Collections.emptyList();
        this.rows = Collections.emptyList();
        this.constructedModel = constructedModel;
        this.error = error;
        this.isConstruct = true;
    }

    public List<String> getVariables() { return variables; }
    public List<Map<String, String>> getRows() { return rows; }
    public String getError() { return error; }
    public boolean hasError() { return error != null && !error.isEmpty(); }
    public boolean isConstruct() { return isConstruct; }
    public Model getConstructedModel() { return constructedModel; }
}