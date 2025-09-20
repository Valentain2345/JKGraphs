package org.logicaGrafo;

import org.apache.jena.rdf.model.Model;
import java.util.List;
import java.util.Map;

/**
 * Service layer to handle graph-related operations.
 */
public class GraphService {

    private final GrafosJena grafosJena;

    
    public GraphService() {
        this.grafosJena = new GrafosJena();
    }

    public void loadGraphFromFile(String filePath) {
        grafosJena.cargarGrafoDesdeArchivo(filePath);
    }

    public void loadGraphFromUrl(String url) {
        grafosJena.cargarGrafoDesdeUrl(url);
    }

    public SparqlQueryResult executeQuery(String query) {
        return grafosJena.ejecutarConsultaSPARQL(query);
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