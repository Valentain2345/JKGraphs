package org.logicaGrafo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;

public class GrafosJena {

	Dataset dataset=DatasetFactory.create();

	// Método para cargar un grafo RDF desde un archivo
	public SparqlQueryResult cargarGrafoDesdeArchivo(String rutaArchivo) {
		try {
			String resolvedPath = rutaArchivo;
			File file = new File(rutaArchivo);
			
			if (!file.exists() || !file.canRead()) {
				return SparqlQueryResult.forBottomMsg("El archivo no existe o no se puede leer: " + resolvedPath);
			}

		
			Model model;
			try {
				model = RDFDataMgr.loadModel(resolvedPath);
				} catch (Exception e) {
				return SparqlQueryResult.forBottomMsg("Error al leer el archivo RDF: " + e.getMessage());
				}

			if (model.isEmpty()) {
				return SparqlQueryResult.forBottomMsg("El modelo RDF está vacío después de cargar el archivo. Verifique el formato y el contenido.");
			}

			dataset.setDefaultModel(model);

			try {
				dataset = RDFDataMgr.loadDataset(resolvedPath);
			} catch (Exception e) {
				SparqlQueryResult.forBottomMsg(" Error al cargar el dataset: " + e.getMessage());
			}

			return SparqlQueryResult.forBottomMsg("Archivo cargado correctamente");
		} catch(Exception e) {
			return SparqlQueryResult.forBottomMsg("Error al cargar el grafo desde el archivo: " + e.getMessage());
		}
	}

	public SparqlQueryResult cargarGrafoFormatoInusual(String rutaArchivo, Lang formato) {
		    // The parsers will do the necessary character set conversion.  
		    try (FileInputStream in = new FileInputStream(rutaArchivo)) {
		        dataset = 
		            RDFParser.create()
		                .source(in)
		                .lang(formato) 
		                .errorHandler(ErrorHandlerFactory.errorHandlerStrict)
		                .base("http://example/base")
		                .toDataset();
		    } catch (FileNotFoundException e) {
		    	return SparqlQueryResult.forBottomMsg("Archivo no encontrado "+ e.getMessage());
			} catch (IOException e) {
				
				return SparqlQueryResult.forBottomMsg("Error leyendo archivo "+ e.getMessage());
			}
		    
		    return SparqlQueryResult.forBottomMsg("Archivo cargado correctamente en formato "+formato.getName() );
	}

	// Método para cargar un grafo RDF desde una URL
	public SparqlQueryResult cargarGrafoDesdeUrl(String url) {
		try (RDFConnection conn = RDFConnection.connect(url)) {
			Model model = conn.fetch();
			dataset.setDefaultModel(model);
			return SparqlQueryResult.forBottomMsg("Grafo remoto cargado correctamente");
		} catch (Exception e) {
			return SparqlQueryResult.forBottomMsg("Error al cargar el grafo desde la URL: " + e.getMessage());
		}
	}



    public SparqlQueryResult ejecutarConsultaSPARQL(String consulta) {
        if (dataset.isEmpty()) {
            return SparqlQueryResult.forBottomMsg("El dataset está vacío. No se puede ejecutar la consulta.");
        }

        try {
            Query query = QueryFactory.create(consulta);
            try (QueryExecution qExec = QueryExecutionFactory.create(query, dataset)) {
                if (query.isSelectType()) {
                    ResultSet rs = qExec.execSelect();
                    List<String> variables = new ArrayList<>(rs.getResultVars());
                    List<Map<String, String>> rows = new ArrayList<>();
                    while (rs.hasNext()) {
                        QuerySolution qs = rs.next();
                        Map<String, String> row = new HashMap<>();
                        for (String var : variables) {
                            RDFNode node = qs.get(var);
                            row.put(var, node != null ? node.toString() : "");
                        }
                        rows.add(row);
                    }
                    return SparqlQueryResult.forSelect(variables, rows);
                } else if (query.isConstructType()) {
                    Model constructedModel = qExec.execConstruct();
                    return SparqlQueryResult.forConstruct(constructedModel);
                } else if (query.isDescribeType()) {
                    Model describedModel = qExec.execDescribe();
                    return SparqlQueryResult.forDescribe(describedModel);
                } else if (query.isAskType()) {
                    boolean askResult = qExec.execAsk();
                    return SparqlQueryResult.forAsk(askResult);
                } else {
                    return SparqlQueryResult.forBottomMsg("Tipo de consulta no soportado: " + query.queryType());
                }
            }
        } catch (Exception e) {
            return SparqlQueryResult.forBottomMsg("Error al ejecutar la consulta SPARQL: " + e.getMessage());
        }
    }

    // Export the current model to RDF/JSON file
    public void exportModelAsRdfJson(String filePath) {
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(filePath)) {
            Model model = dataset.getDefaultModel();
            if (model != null) {
                model.write(out, "RDF/JSON");
            }
        } catch (Exception e) {
            System.err.println("Error exporting model as RDF/JSON: " + e.getMessage());
        }
    }

    // Export the current model to Cytoscape.js compatible JSON file
    public void exportModelAsCytoscapeJson(String filePath) {
        CytoscapeGraphExporter.exportToCytoscapeJson(getModel(), filePath);
    }

    // Getter for the current Jena model
    public Model getModel() {
        return dataset.getDefaultModel();
    }

    public void addTriple(String subject, String predicate, String object) {
        Model model = getModel();
        if (model != null) {
            try {
                Resource s = model.createResource(subject);
                org.apache.jena.rdf.model.Property p = model.createProperty(predicate);
                RDFNode o;
                if (object.startsWith("http://") || object.startsWith("https://") || object.startsWith("urn:")) {
                    o = model.createResource(object);
                } else {
                    o = model.createLiteral(object);
                }
                model.add(s, p, o);
            } catch (Exception e) {
                System.err.println("Error adding triple: " + e.getMessage());
            }
        }
    }

    // Create a model from SELECT ?s ?p ?o results
    public Model createModelFromTriples(List<Map<String, String>> rows) {
        Model model = ModelFactory.createDefaultModel();
        for (Map<String, String> row : rows) {
            String s = row.get("s");
            String p = row.get("p");
            String o = row.get("o");
            if (s != null && p != null && o != null) {
                try {
                    // Try to add as resource or literal
                    if (o.startsWith("http://") || o.startsWith("https://") || o.startsWith("urn:")) {
                        model.add(model.createResource(s), model.createProperty(p), model.createResource(o));
                    } else if (o.startsWith("_:") || o.startsWith("[")) {
                        model.add(model.createResource(s), model.createProperty(p), model.createResource(o));
                    } else {
                        model.add(model.createResource(s), model.createProperty(p), model.createLiteral(o));
                    }
                } catch (Exception e) {
                    System.err.println("Error adding triple to model: " + e.getMessage());
                }
            }
        }
        return model;
    }
}