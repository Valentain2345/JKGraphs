package org.logicaGrafo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;

public class SparqlQueryResult {
    private final List<String> variables;
    private final List<Map<String, String>> rows;
    private final String bottomMsg;
    private final boolean isSelect;
    private final boolean isConstruct;
    private final boolean isAsk;
    private final boolean isDescribe;
    private final Model modelResult;
    private final Boolean askResult;

    // Constructor now takes a single DTO
    public SparqlQueryResult(SparqlQueryResultData data) {
        this.variables = data.variables != null ? data.variables : Collections.emptyList();
        this.rows = data.rows != null ? data.rows : Collections.emptyList();
        this.modelResult = data.modelResult;
        this.askResult = data.askResult;
        this.bottomMsg = data.bottomMsg;
        this.isSelect = data.isSelect;
        this.isConstruct = data.isConstruct;
        this.isAsk = data.isAsk;
        this.isDescribe = data.isDescribe;
    }

    // Factory methods
    public static SparqlQueryResult forSelect(List<String> variables, List<Map<String, String>> rows) {
        SparqlQueryResultData data = new SparqlQueryResultData();
        data.variables = variables;
        data.rows = rows;
        data.isSelect = true;
        return new SparqlQueryResult(data);
    }
    public static SparqlQueryResult forConstruct(Model model) {
        SparqlQueryResultData data = new SparqlQueryResultData();
        data.modelResult = model;
        data.isConstruct = true;
        return new SparqlQueryResult(data);
    }
    public static SparqlQueryResult forDescribe(Model model) {
        SparqlQueryResultData data = new SparqlQueryResultData();
        data.modelResult = model;
        data.isDescribe = true;
        return new SparqlQueryResult(data);
    }
    public static SparqlQueryResult forAsk(boolean result) {
        SparqlQueryResultData data = new SparqlQueryResultData();
        data.askResult = result;
        data.isAsk = true;
        return new SparqlQueryResult(data);
    }
    public static SparqlQueryResult forBottomMsg(String error) {
        SparqlQueryResultData data = new SparqlQueryResultData();
        data.bottomMsg = error;
        return new SparqlQueryResult(data);
    }

    // Getters
    public List<String> getVariables() { return variables; }
    public List<Map<String, String>> getRows() { return rows; }
    public String getError() { return bottomMsg; }
    public boolean hasError() { return bottomMsg != null && !bottomMsg.isEmpty(); }
    public boolean isSelect() { return isSelect; }
    public boolean isConstruct() { return isConstruct; }
    public boolean isAsk() { return isAsk; }
    public boolean isDescribe() { return isDescribe; }
    public Model getModelResult() { return modelResult; }
    public Boolean getAskResult() { return askResult; }
}