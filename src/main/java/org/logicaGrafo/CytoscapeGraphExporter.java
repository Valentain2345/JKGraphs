package org.logicaGrafo;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Set;
import java.io.FileWriter;

/**
 * Utility class to export a Jena Model to Cytoscape.js compatible JSON.
 * Nodes are unique by URI or blank node ID. Edges represent predicates.
 */
public class CytoscapeGraphExporter {
    public static void exportToCytoscapeJson(Model model, String filePath) {
        if (model == null) return;
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
                node.put("data", new JSONObject().put("id", subjectId));
                nodes.put(node);
                nodeIds.add(subjectId);
            }

            // Add object node if it's a resource
            if (objectId != null && !nodeIds.contains(objectId)) {
                JSONObject node = new JSONObject();
                node.put("data", new JSONObject().put("id", objectId));
                nodes.put(node);
                nodeIds.add(objectId);
            }

            // Add edge if object is a resource
            if (objectId != null) {
                String predicate = stmt.getPredicate().getURI();
                String edgeId = "e" + (++edgeCount);
                JSONObject edge = new JSONObject();
                edge.put("data", new JSONObject()
                    .put("id", edgeId)
                    .put("source", subjectId)
                    .put("target", objectId)
                    .put("label", predicate));
                edges.put(edge);
            }
        }

        // Write to file
        try (FileWriter writer = new FileWriter(filePath)) {
            JSONObject cytoscapeJson = new JSONObject();
            // Merge nodes and edges into a single array
            JSONArray elements = new JSONArray();
            for (int i = 0; i < nodes.length(); i++) elements.put(nodes.get(i));
            for (int i = 0; i < edges.length(); i++) elements.put(edges.get(i));
            cytoscapeJson.put("elements", elements);
            writer.write(cytoscapeJson.toString(2));
        } catch (Exception e) {
            System.err.println("Error exporting Cytoscape JSON: " + e.getMessage());
        }
    }

    private static String getNodeId(Resource res) {
        if (res.isURIResource()) return res.getURI();
        if (res.isAnon()) return "_:" + res.getId().getLabelString();
        return res.toString();
    }
}