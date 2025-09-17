package org.logicaGrafo;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;

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
    }
}