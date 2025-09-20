package org.aplication.KnowledgeGraphs;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.logicaGrafo.GrafosJena;
import org.logicaGrafo.SparqlQueryResult;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class PrimaryController {
    private CytoscapeWindow cytoscapeWindow = new CytoscapeWindow();
    private GrafosJena grafosJena = new GrafosJena();

    @FXML
    private TextArea queryInput;
    @FXML
    private Text errorMessage;
    @FXML
    private TableView<Map<String, String>> queryResultsTable;

    private ObservableList<Map<String, String>> results = FXCollections.observableArrayList();

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
    private void showGraph() throws IOException {
        cytoscapeWindow.show();
    }

    @FXML
    private void executeQuery() throws IOException {
        String query = queryInput.getText();
        SparqlQueryResult result = grafosJena.ejecutarConsultaSPARQL(query);
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
    }
}