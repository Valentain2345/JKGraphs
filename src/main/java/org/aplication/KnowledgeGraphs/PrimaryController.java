package org.aplication.KnowledgeGraphs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.logicaGrafo.GraphService;
import org.logicaGrafo.SparqlQueryResult;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;


public class PrimaryController {
    private final CytoscapeWindow cytoscapeWindow;
    private final GraphService graphService;

    @FXML
    private TextArea queryInput;
    @FXML
    private Text errorMessage;
    @FXML
    private TableView<Map<String, String>> queryResultsTable;

    private final ObservableList<Map<String, String>> results = FXCollections.observableArrayList();

    public PrimaryController() {
        this.graphService = new GraphService();
        this.cytoscapeWindow = new CytoscapeWindow();
    }

    @FXML
    private void initialize() {
        if (queryResultsTable != null) {
            queryResultsTable.setItems(results);
        }
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void showGraph() {
        cytoscapeWindow.show();
        cytoscapeWindow.reloadGraphData();
    }

    @FXML
    private void abrirLocal() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo RDF");
        File file = fileChooser.showOpenDialog(queryInput.getScene().getWindow());
        if (file != null) {
            graphService.loadGraphFromFile(file.getAbsolutePath());
            errorMessage.setText("Archivo cargado: " + file.getName());
            updateGraphView();
        }
    }

    @FXML
    private void abrirRemoto() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Abrir grafo remoto");
        dialog.setHeaderText("Introduce la URL del grafo RDF");
        dialog.setContentText("URL:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(url -> {
            graphService.loadGraphFromUrl(url);
            errorMessage.setText("Grafo remoto cargado");
            updateGraphView();
        });
    }

    @FXML
    private void executeQuery() {
        String query = queryInput.getText();
        SparqlQueryResult result = graphService.executeQuery(query);
        results.clear();
        errorMessage.setText("");
        queryResultsTable.getColumns().clear();

        if (result.hasError()) {
            errorMessage.setText(result.getError());
            return;
        }

        List<String> variables = result.getVariables();
        for (String var : variables) {
            TableColumn<Map<String, String>, String> col = new TableColumn<>(var);
            col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(var)));
            queryResultsTable.getColumns().add(col);
        }
        results.addAll(result.getRows());

        Model modelToExport = null;
        if (result.isConstruct()) {
            modelToExport = result.getConstructedModel();
        } else if (variables.contains("s") && variables.contains("p") && variables.contains("o")) {
            modelToExport = graphService.createModelFromTriples(result.getRows());
        }

        if (modelToExport != null) {
            graphService.exportForCytoscape(modelToExport, "cytoscape-data.json");
        } else {
            graphService.exportForCytoscape(graphService.getModel(), "cytoscape-data.json");
        }
        cytoscapeWindow.reloadGraphData();
    }

    private void updateGraphView() {
        graphService.exportForCytoscape(graphService.getModel(), "cytoscape-data.json");
        cytoscapeWindow.reloadGraphData();
    }

    private void exportGraph(String format, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Grafo");
        fileChooser.setInitialFileName("export." + extension);
        File file = fileChooser.showSaveDialog(queryInput.getScene().getWindow());
        if (file != null) {
            graphService.exportGraph(format, file.getAbsolutePath());
            errorMessage.setText("Grafo exportado a " + file.getName());
        }
    }

    @FXML
    private void exportRdfXml() {
        exportGraph("RDF/XML", "rdf");
    }

    @FXML
    private void exportTurtle() {
        exportGraph("TURTLE", "ttl");
    }

    @FXML
    private void exportNTriples() {
        exportGraph("N-TRIPLES", "nt");
    }

    @FXML
    private void exportRdfJson() {
        exportGraph("RDF/JSON", "json");
    }
}