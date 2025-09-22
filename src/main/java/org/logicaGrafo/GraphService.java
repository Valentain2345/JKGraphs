package org.logicaGrafo;

import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.logicaGrafo.query.SparqlQueryExecutor;
import org.logicaGrafo.query.SparqlQueryExecutorFactory;

/**
 * Service layer to handle graph-related operations.
 */
public class GraphService {

    private final GrafosJena grafosJena;

    public GraphService() {
        this.grafosJena = new GrafosJena();
    }

    public SparqlQueryResult loadGraphFromFile(String filePath) {
        return grafosJena.cargarGrafoDesdeArchivo(filePath);
    }

    public SparqlQueryResult loadGraphFromUrl(String url) {
        return grafosJena.cargarGrafoDesdeUrl(url);
    }

    public SparqlQueryResult executeQuery(String queryStr) {
        if (grafosJena.getModel() == null) {
            return SparqlQueryResult.forBottomMsg("No RDF model loaded.");
        }
        try {
            Query query = QueryFactory.create(queryStr);
            SparqlQueryExecutor executor = SparqlQueryExecutorFactory.getExecutor(query);
            return executor.execute(query, grafosJena.dataset);
        } catch (Exception e) {
            return SparqlQueryResult.forBottomMsg("Error executing query: " + e.getMessage());
        }
    }

    public void exportGraph(String format, String filePath) {
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(filePath)) {
            Model model = grafosJena.getModel();
            if (model != null) {
                model.write(out, format);
            }
        } catch (Exception e) {
            System.err.println("Error exporting model as " + format + ": " + e.getMessage());
        }
    }

    public void exportForCytoscape(String filePath) {
        grafosJena.exportModelAsCytoscapeJson(filePath);
    }

    public void exportForCytoscape(Model model, String filePath) {
        CytoscapeGraphExporter.exportToCytoscapeJson(model, filePath);
    }

    public Model createModelFromTriples(List<Map<String, String>> rows) {
        return grafosJena.createModelFromTriples(rows);
    }

    public Model getModel() {
        return grafosJena.getModel();
    }

    public void addTriple(String subject, String predicate, String object) {
        grafosJena.addTriple(subject, predicate, object);
    }
}