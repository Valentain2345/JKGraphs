package org.aplication.KnowledgeGraphs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.logicaGrafo.GraphService;
import org.logicaGrafo.SparqlQueryResult;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

class MessageService {
	public void clearMessage(Text textElement) {
		if (textElement != null) {
			textElement.setText("");
		}
	}

	public void updateMessage(Text textElement, String message) {
		if (textElement != null) {
			textElement.setText(message);
		}
	}
}

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
	private SparqlQueryResult lastQueryresult;
	private final ObservableList<Map<String, String>> results = FXCollections.observableArrayList();
	private ProgressIndicator exportProgressIndicator = new ProgressIndicator();
	@FXML
	private HBox menuBar;

	public PrimaryController() {
		graphService = new GraphService();
		cytoscapeWindow = new CytoscapeWindow();
		messageService = new MessageService();
	}

	@FXML
	private void openLocalDataset() {
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
	private void openMultipleDatasets() {
	
	}

	@FXML
	private void openRemoteDataset() {
		
		TextInputDialog dialog = new TextInputDialog();
		new UIUtils().setWindowIcon((Stage) dialog.getDialogPane().getScene().getWindow());
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

	/**
	 * Distributes columns evenly across the table width
	 */
	private void distributeColumnsEvenly() {
		if (queryResultsTable.getColumns().isEmpty()) {
			return;
		}

		// Force the table to layout
		queryResultsTable.layout();

		int columnCount = queryResultsTable.getColumns().size();

		// Bind each column's preferred width to the table's width divided by column
		// count
		for (TableColumn<?, ?> column : queryResultsTable.getColumns()) {
			column.prefWidthProperty().bind(queryResultsTable.widthProperty().subtract(2) // Account for table borders
					.divide(columnCount));
		}
	}

	@FXML
	private void executeQuery() {
		String query = queryInput.getText();
		SparqlQueryResult result = graphService.executeQuery(query);
		lastQueryresult = result; // <-- Ensure lastQueryresult is updated
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

			// Calculate the base width for each column
			double tableWidth = queryResultsTable.getWidth();
			if (tableWidth <= 0) {
				tableWidth = 700; // Default width if table width is not yet set
			}
			double columnWidth = tableWidth / variables.size();

			for (int i = 0; i < variables.size(); i++) {
				String var = variables.get(i);
				TableColumn<Map<String, String>, String> col = new TableColumn<>(var);

				// Set cell value factory
				col.setCellValueFactory(cellData -> {
					String value = cellData.getValue().get(var);
					return new SimpleStringProperty(value != null ? value : "");
				});

				// Set column width properties for even distribution
				col.setMinWidth(80); // Minimum width to ensure readability
				col.setPrefWidth(columnWidth);
				col.setMaxWidth(Double.MAX_VALUE);

				// Enable text wrapping in cells for long content
				col.setCellFactory(tc -> {
					TableCell<Map<String, String>, String> cell = new TableCell<Map<String, String>, String>() {
						@Override
						protected void updateItem(String item, boolean empty) {
							super.updateItem(item, empty);
							if (empty || item == null) {
								setText(null);
								setGraphic(null);
							} else {
								// Create a label that wraps text
								Label label = new Label(item);
								label.setWrapText(true);
								label.setMaxWidth(Double.MAX_VALUE);
								setGraphic(label);
								setText(null);
							}
						}
					};
					cell.setPrefHeight(Region.USE_COMPUTED_SIZE);
					return cell;
				});

				// Make columns sortable
				col.setSortable(true);

				// Add column to table
				queryResultsTable.getColumns().add(col);
			}

			// Ensure equal width distribution after columns are added
			distributeColumnsEvenly();

			results.addAll(result.getRows());
			queryResultsTable.setVisible(true);

		} else if (result.isAsk()) {
			messageService.updateMessage(bottomMessage,
					"ASK result: " + (result.getAskResult() != null ? result.getAskResult().toString() : "null"));
		} else if (result.isConstruct() || result.isDescribe()) {
			messageService.updateMessage(bottomMessage,
					(result.isConstruct() ? "CONSTRUCT" : "DESCRIBE") + " query executed. Graph updated.");
		}

	}

	private void exportGraph(String format, String extension) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Exportar Grafo");
		fileChooser.setInitialFileName("export." + extension);
		File file = fileChooser.showSaveDialog(queryInput.getScene().getWindow());
		if (file != null) {
			graphService.exportGraph(format, file.getAbsolutePath());
			messageService.updateMessage(bottomMessage, "Exported graph to " + file.getName());
		}
	}

	@FXML
	private void exportNTriples() {
		exportGraph("N-TRIPLES", "nt");
	}

	@FXML
	private void exportRdfJson() {
		exportGraph("RDF/JSON", "json");
	}

	@FXML
	private void exportRdfXml() {
		exportGraph("RDF/XML", "rdf");
	}

	// New export method for SELECT query results
	private void exportSelectResultToCytoscape(List<String> nodeVars, List<String> edgeVars) {
		if (lastQueryresult == null || !lastQueryresult.isSelect()) {
			UIUtils.showErrorDialog("Error", "No SELECT query result available.");
			return;
		}
		exportProgressIndicator.setVisible(true);
		Task<Void> exportTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				List<Map<String, String>> rows = lastQueryresult.getRows();
				// Use the new exporter to build the graph directly from SELECT results
				org.logicaGrafo.CytoscapeGraphExporter.exportSelectResultToCytoscapeJson(rows, nodeVars, edgeVars,
						"cytoscape-data.json");
				cytoscapeWindow.reloadGraphData();
				return null;
			}
		};
		exportTask.setOnSucceeded(e -> {
			exportProgressIndicator.setVisible(false);
			cytoscapeWindow.show();
			cytoscapeWindow.reloadGraphData();
		});
		exportTask.setOnFailed(e -> {
			exportProgressIndicator.setVisible(false);
			UIUtils.showErrorDialog("Export Error", "Failed to export graph for Cytoscape.");
		});
		new Thread(exportTask).start();
	}

	@FXML
	private void exportTurtle() {
		exportGraph("TURTLE", "ttl");
	}

	@FXML
	private void initialize() {
		if (queryResultsTable != null) {
			queryResultsTable.setItems(results);
			// Set column resize policy - this is important for even distribution
			queryResultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		}
		// Ensure MenuItem style classes are set programmatically for JavaFX 21
		// compatibility
		if (menuBar != null) {
			for (javafx.scene.Node node : menuBar.getChildren()) {
				if (node instanceof javafx.scene.control.MenuButton mb) {
					for (javafx.scene.control.MenuItem mi : mb.getItems()) {
						if (!mi.getStyleClass().contains("menu-item")) {
							mi.getStyleClass().add("menu-item");
						}
					}
				}
			}
		}
	}

	@FXML
	private void showGraph() {
		if (lastQueryresult == null || !lastQueryresult.isSelect() || lastQueryresult.getVariables().isEmpty()) {
			UIUtils.showErrorDialog("No SELECT query results", "Please run a SELECT query first.");
			return;
		}
		List<String> variables = lastQueryresult.getVariables();
		// Create dialog window
		Stage dialogStage = new Stage();
		dialogStage.setTitle("Select Node and Edge Variables");
		dialogStage.setResizable(true);
		(new UIUtils()).setWindowIcon(dialogStage);
		VBox vbox = new VBox(18);
		vbox.setPadding(new javafx.geometry.Insets(24));
		vbox.setStyle(
				"-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0.5, 0, 5); -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1;");

		Label nodeLabel = new Label("Select Node Variables:");
		nodeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
		VBox nodeBox = new VBox(8);
		for (String var : variables) {
			CheckBox cb = new CheckBox(var);
			cb.setStyle("-fx-font-size: 15px; -fx-text-fill: #212121; -fx-padding: 6 0 6 0;");
			nodeBox.getChildren().add(cb);
		}
		ScrollPane nodeScroll = new ScrollPane(nodeBox);
		nodeScroll.setFitToWidth(true);
		nodeScroll.setPrefHeight(Math.min(variables.size() * 32 + 16, 220));
		nodeScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

		Label edgeLabel = new Label("Select Edge Variables:");
		edgeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #764ba2;");
		VBox edgeBox = new VBox(8);
		for (String var : variables) {
			CheckBox cb = new CheckBox(var);
			cb.setStyle("-fx-font-size: 15px; -fx-text-fill: #212121; -fx-padding: 6 0 6 0;");
			edgeBox.getChildren().add(cb);
		}
		ScrollPane edgeScroll = new ScrollPane(edgeBox);
		edgeScroll.setFitToWidth(true);
		edgeScroll.setPrefHeight(Math.min(variables.size() * 32 + 16, 220));
		edgeScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

		javafx.scene.control.Button acceptButton = new javafx.scene.control.Button("Accept");
		acceptButton.getStyleClass().add("button");
		acceptButton.setStyle(
				"-fx-font-size: 17px; -fx-padding: 12 30 12 30; -fx-font-weight: bold; -fx-background-radius: 15; -fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
		acceptButton.setOnMouseEntered(e -> acceptButton.setStyle(
				"-fx-background-color: linear-gradient(to right, #5a67d8, #6b46c1); -fx-font-size: 17px; -fx-padding: 12 30 12 30; -fx-font-weight: bold; -fx-background-radius: 15; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.5), 20, 0, 0, 5);"));
		acceptButton.setOnMouseExited(e -> acceptButton.setStyle(
				"-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-font-size: 17px; -fx-padding: 12 30 12 30; -fx-font-weight: bold; -fx-background-radius: 15; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"));
		acceptButton.setOnAction(e -> {
			List<String> selectedNodes = new java.util.ArrayList<>();
			for (javafx.scene.Node n : nodeBox.getChildren()) {
				if (n instanceof CheckBox cb && cb.isSelected()) {
					selectedNodes.add(cb.getText());
				}
			}
			List<String> selectedEdges = new java.util.ArrayList<>();
			for (javafx.scene.Node n : edgeBox.getChildren()) {
				if (n instanceof CheckBox cb && cb.isSelected()) {
					selectedEdges.add(cb.getText());
				}
			}
			dialogStage.close();
			exportSelectResultToCytoscape(selectedNodes, selectedEdges);
		});
		// Add ProgressIndicator to dialog
		exportProgressIndicator.setVisible(false);
		vbox.getChildren().add(exportProgressIndicator);
		vbox.getChildren().addAll(nodeLabel, nodeScroll, edgeLabel, edgeScroll, acceptButton);
		javafx.scene.Scene scene = new javafx.scene.Scene(vbox);
		scene.getStylesheets()
				.add(getClass().getResource("/org/aplication/KnowledgeGraphs/app-style.css").toExternalForm());
		scene.getStylesheets()
				.add(getClass().getResource("/org/aplication/KnowledgeGraphs/button-style.css").toExternalForm());
		dialogStage.setScene(scene);
		dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
		dialogStage.centerOnScreen();
		dialogStage.showAndWait();
	}

	@FXML
	private void switchToSecondary() throws IOException {
		App.setRoot("secondary");
	}

	private void updateGraphView() {
		graphService.exportForCytoscape(graphService.getModel(), "cytoscape-data.json");
		cytoscapeWindow.reloadGraphData();
	}

}
