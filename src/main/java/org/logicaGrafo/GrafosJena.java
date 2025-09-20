package org.logicaGrafo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;

public class GrafosJena {
    /**
     * Crea un grafo RDF con Jena, añade un recurso y lo imprime por consola.
     */

	public static void crearGrafoYAgregarElemento() {
        // Crear un modelo vacío
        Model model = ModelFactory.createDefaultModel();

        // Crear un recurso y una propiedad
        Resource subject = model.createResource("http://example.org/subject");
        Property predicate = model.createProperty("http://example.org/predicate");
        Resource object = model.createResource("http://example.org/object");

        // Añadir una declaración (triple) al modelo
        Statement stmt = model.createStatement(subject, predicate, object);
        model.add(stmt);

        // Imprimir el modelo en formato RDF/XML para verificar que funciona
        model.write(System.out, "RDF/XML");

        // Cerrar el modelo para liberar recursos
        model.close();
    }


    public SparqlQueryResult ejecutarConsultaSPARQL(String consulta) {
        List<String> variables = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();
        String error = null;
        RDFConnection conn = null;
        Dataset data = null;
        try {
            conn = RDFConnection.connect("https://www.w3.org/1999/02/22-rdf-syntax-ns#");
            data = conn.fetchDataset();
        } catch (Exception e) {
            error = "Error al conectar con el endpoint SPARQL: " + e.getMessage();
        }
        if (data == null) {
            if (error == null) error = "No se pudo obtener el dataset del endpoint SPARQL.";
            return new SparqlQueryResult(variables, rows, error);
        }
        QueryExecution qExec = null;
        try {
            qExec = QueryExecutionFactory.create(consulta, data);
            ResultSet rs = qExec.execSelect();
            variables.addAll(rs.getResultVars());
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                Map<String, String> row = new HashMap<>();
                for (String var : variables) {
                    RDFNode node = qs.get(var);
                    row.put(var, node != null ? node.toString() : "");
                }
                rows.add(row);
            }
        } catch (Exception e) {
            error = "Error al ejecutar la consulta SPARQL: " + e.getMessage();
        } finally {
            if (qExec != null) qExec.close();
            if (conn != null) conn.close();
        }
        return new SparqlQueryResult(variables, rows, error);
    }

}