package org.logicaGrafo;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class to export a Jena Model to Cytoscape.js compatible JSON. Nodes
 * are unique by URI or blank node ID. Edges represent predicates.
 */
public class CytoscapeGraphExporter {
	/**
	 * Export a filtered RDF model to Cytoscape.js JSON using selected node and edge
	 * classes. Nodes are resources of the selected node classes. Edges are
	 * predicates of the selected edge classes. Edge label is the value of the
	 * object (literal value or URI/blank node ID).
	 */
	public static void exportFilteredModelToCytoscapeJson(Model model, List<String> nodeClasses,
			List<String> edgeClasses, String filePath) {
		if (model == null) {
			return;
		}
		Set<String> nodeClassSet = new HashSet<>(nodeClasses);
		Set<String> edgeClassSet = new HashSet<>(edgeClasses);
		Set<String> nodeIds = new HashSet<>();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		int edgeCount = 0;
		// Find all resources that are instances of the selected node classes
		Set<Resource> nodeResources = new HashSet<>();
		for (String nodeClass : nodeClassSet) {
			org.apache.jena.rdf.model.ResIterator resIt = model.listSubjectsWithProperty(RDF.type,
					model.getResource(nodeClass));
			while (resIt.hasNext()) {
				nodeResources.add(resIt.nextResource());
			}
		}
		// Add nodes
		for (Resource res : nodeResources) {
			String nodeId = getNodeId(res);
			if (!nodeIds.contains(nodeId)) {
				JSONObject node = new JSONObject();
				node.put("data", new JSONObject().put("id", nodeId).put("label", nodeId));
				nodes.put(node);
				nodeIds.add(nodeId);
			}
		}
		// Add edges
		for (Statement stmt : model.listStatements().toList()) {
			Resource subject = stmt.getSubject();
			String predicateUri = stmt.getPredicate().getURI();
			if (nodeResources.contains(subject) && edgeClassSet.contains(predicateUri)) {
				String sourceId = getNodeId(subject);
				String targetId = stmt.getObject().isResource() ? getNodeId(stmt.getObject().asResource()) : null;
				String label = stmt.getObject().isLiteral() ? stmt.getObject().asLiteral().getString()
						: targetId != null ? targetId : "";
				System.out.println(label);
				if (targetId != null && !nodeIds.contains(targetId)) {
					JSONObject node = new JSONObject();
					node.put("data", new JSONObject().put("id", targetId).put("label", targetId));
					nodes.put(node);
					nodeIds.add(targetId);
				}
				edgeCount++;
				String edgeId = "e" + edgeCount;
				JSONObject edge = new JSONObject();
				edge.put("data", new JSONObject().put("id", edgeId).put("source", sourceId)
						.put("target", targetId != null ? targetId : "").put("label", label));
				edges.put(edge);
			}
		}
		// Write to file
		try (FileWriter writer = new FileWriter(filePath)) {
			JSONObject cytoscapeJson = new JSONObject();
			JSONArray elements = new JSONArray();
			for (int i = 0; i < nodes.length(); i++) {
				elements.put(nodes.get(i));
			}
			for (int i = 0; i < edges.length(); i++) {
				elements.put(edges.get(i));
			}
			cytoscapeJson.put("elements", elements);
			writer.write(cytoscapeJson.toString(2));
		} catch (Exception e) {
			System.err.println("Error exporting Cytoscape JSON: " + e.getMessage());
		}
	}

	/**
	 * Export SELECT query results directly to Cytoscape.js JSON. Each row is
	 * interpreted as a set of nodes and edges based on selected variables.
	 */
	public static void exportSelectResultToCytoscapeJson(List<Map<String, String>> rows, List<String> nodeVars,
			List<String> edgeVars, String filePath) {
		Set<String> nodeIds = new HashSet<>();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		int edgeCount = 0;
		// Add nodes
		for (Map<String, String> row : rows) {
			for (String nodeVar : nodeVars) {
				String nodeId = row.get(nodeVar);
				if (nodeId != null && !nodeIds.contains(nodeId)) {
					JSONObject node = new JSONObject();
					node.put("data", new JSONObject().put("id", nodeId).put("label", nodeId));
					nodes.put(node);
					nodeIds.add(nodeId);
				}
			}
			// For each edge variable, create edges from source to each target
			if (nodeVars.size() > 1) {
				String source = row.get(nodeVars.get(0));
				for (int i = 1; i < nodeVars.size(); i++) {
					String target = row.get(nodeVars.get(i));
					for (String edgeVar : edgeVars) {
						String label = row.get(edgeVar);
						if (source != null && target != null) {
							edgeCount++;
							String edgeId = "e" + edgeCount;
							JSONObject edge = new JSONObject();
							edge.put("data", new JSONObject().put("id", edgeId).put("source", source)
									.put("target", target).put("label", label));
							edges.put(edge);
						}
					}
				}
			} else if (nodeVars.size() == 1) {
				String source = row.get(nodeVars.get(0));
				for (String edgeVar : edgeVars) {
					String target = row.get(edgeVar);
					String label = row.get(edgeVar);
					if (source != null && target != null) {
						edgeCount++;
						String edgeId = "e" + edgeCount;
						JSONObject edge = new JSONObject();
						edge.put("data", new JSONObject().put("id", edgeId).put("source", source).put("target", target)
								.put("label", label));
						edges.put(edge);
					}
				}
			}
		}
		// Write to file
		try (FileWriter writer = new FileWriter(filePath)) {
			JSONObject cytoscapeJson = new JSONObject();
			JSONArray elements = new JSONArray();
			for (int i = 0; i < nodes.length(); i++) {
				elements.put(nodes.get(i));
			}
			for (int i = 0; i < edges.length(); i++) {
				elements.put(edges.get(i));
			}
			cytoscapeJson.put("elements", elements);
			writer.write(cytoscapeJson.toString(2));
		} catch (Exception e) {
			System.err.println("Error exporting Cytoscape JSON from SELECT: " + e.getMessage());
		}
	}

	public static void exportToCytoscapeJson(Model model, String filePath) {
		if (model == null) {
			return;
		}
		Set<String> nodeIds = new HashSet<>();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		int edgeCount = 0;
		for (Statement stmt : model.listStatements().toList()) {
			String subjectId = getNodeId(stmt.getSubject());
			String objectId = stmt.getObject().isResource() ? getNodeId(stmt.getObject().asResource()) : null;

			// Add subject node
			if (!nodeIds.contains(subjectId)) {
				JSONObject node = new JSONObject();
				String color = String.format("#%06X", (int) (Math.random() * 0xFFFFFF));
				node.put("data", new JSONObject().put("id", subjectId).put("color", color));
				nodes.put(node);
				nodeIds.add(subjectId);
			}

			// Add object node if it's a resource
			if (objectId != null && !nodeIds.contains(objectId)) {
				JSONObject node = new JSONObject();
				String color = String.format("#%06X", (int) (Math.random() * 0xFFFFFF));
				node.put("data", new JSONObject().put("id", objectId).put("color", color));
				nodes.put(node);
				nodeIds.add(objectId);
			}

			// Add edge if object is a resource
			if (objectId != null) {
				String predicate = stmt.getPredicate().getURI();
				edgeCount++;
				String edgeId = "e" + edgeCount;
				JSONObject edge = new JSONObject();
				edge.put("data", new JSONObject().put("id", edgeId).put("source", subjectId).put("target", objectId)
						.put("label", predicate));
				edges.put(edge);
			}
		}

		// Write to file
		try (FileWriter writer = new FileWriter(filePath)) {
			JSONObject cytoscapeJson = new JSONObject();
			// Merge nodes and edges into a single array
			JSONArray elements = new JSONArray();
			for (int i = 0; i < nodes.length(); i++) {
				elements.put(nodes.get(i));
			}
			for (int i = 0; i < edges.length(); i++) {
				elements.put(edges.get(i));
			}
			cytoscapeJson.put("elements", elements);
			writer.write(cytoscapeJson.toString(2));
		} catch (Exception e) {
			System.err.println("Error exporting Cytoscape JSON: " + e.getMessage());
		}
	}

	/**
	 * Filters the given model to include only triples where: - The subject is an
	 * instance of one of the nodeClasses (rdf:type) - The predicate is an instance
	 * of one of the edgeClasses (rdf:type) Returns a new Model containing only
	 * relevant triples.
	 */
	public static Model filterModelByClasses(Model model, List<String> nodeClasses,
			java.util.List<String> edgeClasses) {
		Model filtered = ModelFactory.createDefaultModel();
		java.util.Set<String> nodeSet = new java.util.HashSet<>(nodeClasses);
		java.util.Set<String> edgeSet = new java.util.HashSet<>(edgeClasses);
		// First, find all resources that are instances of the selected node classes
		java.util.Set<org.apache.jena.rdf.model.Resource> nodeResources = new java.util.HashSet<>();
		for (String nodeClass : nodeSet) {
			org.apache.jena.rdf.model.ResIterator resIt = model.listSubjectsWithProperty(RDF.type,
					model.getResource(nodeClass));
			while (resIt.hasNext()) {
				nodeResources.add(resIt.nextResource());
			}
		}
		// Now, filter triples
		for (org.apache.jena.rdf.model.Statement stmt : model.listStatements().toList()) {
			boolean isNodeTriple = nodeResources.contains(stmt.getSubject());
			boolean isEdgeTriple = edgeSet.contains(stmt.getPredicate().getURI());
			// Include triple if subject is a selected node or predicate is a selected edge
			if (isNodeTriple || isEdgeTriple) {
				filtered.add(stmt);
			}
		}
		return filtered;
	}

	private static String getNodeId(Resource res) {
		if (res.isURIResource()) {
			return res.getURI();
		}
		if (res.isAnon()) {
			return "_:" + res.getId().getLabelString();
		}
		return res.toString();
	}
}