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
    private final MessageService messageService;

    @FXML
    private TextArea queryInput;
    @FXML
    private Text bottomMessage;
    @FXML
    private TableView<Map<String, String>> queryResultsTable;

    private final ObservableList<Map<String, String>> results = FXCollections.observableArrayList();

    public PrimaryController() {
        this.graphService = new GraphService();
        this.cytoscapeWindow = new CytoscapeWindow();
        this.messageService = new MessageService();
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
            try {
                SparqlQueryResult result = graphService.loadGraphFromFile(file.getAbsolutePath());
                messageService.updateMessage(bottomMessage, result.getError());
                if (!result.hasError()) {
                    updateGraphView();
                }
            } catch (Exception e) {
                UIUtils.showErrorDialog("Error", "No se pudo cargar el archivo: " + e.getMessage());
            }
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
            try {
                SparqlQueryResult loadResult = graphService.loadGraphFromUrl(url);
                messageService.updateMessage(bottomMessage, loadResult.getError());
                if (!loadResult.hasError()) {
                    updateGraphView();
                }
            } catch (Exception e) {
                UIUtils.showErrorDialog("Error", "No se pudo cargar el grafo remoto: " + e.getMessage());
            }
        });
    }

    @FXML
    private void executeQuery() {
        String query = queryInput.getText();
        SparqlQueryResult result = graphService.executeQuery(query);
        results.clear();
        messageService.clearMessage(bottomMessage);
        queryResultsTable.getColumns().clear();
        queryResultsTable.setVisible(false);

        if (result.hasError()) {
            messageService.updateMessage(bottomMessage, result.getError());
            return;
        }

        if (result.isSelect()) {
            List<String> variables = result.getVariables();
            for (String var : variables) {
                TableColumn<Map<String, String>, String> col = new TableColumn<>(var);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(var)));
                queryResultsTable.getColumns().add(col);
            }
            results.addAll(result.getRows());
            queryResultsTable.setVisible(true);
        } else if (result.isAsk()) {
            messageService.updateMessage(bottomMessage, "ASK result: " + (result.getAskResult() != null ? result.getAskResult().toString() : "null"));
        } else if (result.isConstruct() || result.isDescribe()) {
            messageService.updateMessage(bottomMessage, (result.isConstruct() ? "CONSTRUCT" : "DESCRIBE") + " query executed. Graph updated.");
        }

        Model modelToExport = null;
        if (result.isConstruct() || result.isDescribe()) {
            modelToExport = result.getModelResult();
        } else if (result.isSelect() && result.getVariables().contains("s") && result.getVariables().contains("p") && result.getVariables().contains("o")) {
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
            messageService.updateMessage(bottomMessage, "Grafo exportado a " + file.getName());
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

class MessageService {
    public void updateMessage(Text textElement, String message) {
        if (textElement != null) {
            textElement.setText(message);
        }
    }

    public void clearMessage(Text textElement) {
        if (textElement != null) {
            textElement.setText("");
        }
    }
}