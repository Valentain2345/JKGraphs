package org.logicaGrafo;

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


    public void ejecutarConsultaSPARQL(String consulta) {
    	RDFConnection conn = RDFConnection.connect("https://www.w3.org/1999/02/22-rdf-syntax-ns#");
    			Dataset data= conn.fetchDataset() ;
    			QueryExecution qExec = QueryExecutionFactory.create("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 100", data) ;
    			ResultSet rs = qExec.execSelect() ;
    			while(rs.hasNext()) {
    			  QuerySolution qs = rs.next() ;
    			  Resource s = qs.getResource("s") ;
    			  Resource p = qs.getResource("p");
    			  RDFNode o = qs.get("o") ;
    			  System.out.println(s+" "+p+" "+o.toString()) ;
    			}
    			qExec.close() ;
    			conn.close() ;
	}

}