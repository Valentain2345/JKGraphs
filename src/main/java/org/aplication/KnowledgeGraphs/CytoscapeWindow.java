package org.aplication.KnowledgeGraphs;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.aplication.KnowledgeGraphs.UIUtils;

public class CytoscapeWindow {

	public class JavaApp {
		public void log(String message) {
			System.out.println("JS Console: " + message);
		}
	}

	private final Stage stage;
	private final WebView webView;
	private final WebEngine webEngine;

	private String pendingJson = null;
	private ComboBox<String> layoutCombo; // Make ComboBox a field
	private ComboBox<String> nodeSizeMetricCombo; // Node size metric selector

	public CytoscapeWindow() {
		stage = new Stage();
		webView = new WebView();
		webEngine = webView.getEngine();

	
		String htmlPath = getClass().getResource("/Cytoscape/cytoscape.html").toExternalForm();
		if (htmlPath == null) {
			System.err.println("Error: cytoscape.html not found in resources/html/");
			return;
		}
		webEngine.load(htmlPath);

		Button refreshButton = new Button("Refresh");
		refreshButton.setOnAction(event -> reloadGraphData());
		refreshButton.getStyleClass().add("menu-button");

		Label layoutLabel = new Label("Layout:");
		layoutLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 8 0 8; -fx-alignment: center; -fx-font-size: 14px; -fx-text-fill: #1A4A7A;");
		layoutCombo = new ComboBox<>();
		layoutCombo.getItems().addAll("grid", "circle", "concentric", "breadthfirst", "cose", "random", "avsdf","cola", "dagre", "elk", "fcose", "cose-bilkent", "cise");
		layoutCombo.setValue("circle");
		layoutCombo.setOnAction(event -> {
			String layout = layoutCombo.getValue();
			setLayout(layout);
		});
		layoutCombo.getStyleClass().add("menu-button");

		Label metricLabel = new Label("Node Size Metric:");
		metricLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 8 0 8; -fx-alignment: center; -fx-font-size: 14px; -fx-text-fill: #1A4A7A;");
		nodeSizeMetricCombo = new ComboBox<>();
		nodeSizeMetricCombo.getItems().addAll("Outgoing edges", "Ingoing edges");
		nodeSizeMetricCombo.setValue("Outgoing edges");
		nodeSizeMetricCombo.setOnAction(event -> {
			String metric = nodeSizeMetricCombo.getValue().equals("Ingoing edges") ? "in" : "out";
			if (isWebViewReady()) {
				webEngine.executeScript("window.setNodeSizeMetric('" + metric + "')");
			}
		});
		nodeSizeMetricCombo.getStyleClass().add("menu-button");

		Button saveButton = new Button("Save as PNG");
		saveButton.setOnAction(event -> {
			if (isWebViewReady()) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save Graph as PNG");
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
				File file = fileChooser.showSaveDialog(stage);
				if (file != null) {
					try {
						Object result = webEngine.executeScript("window.getPNGBase64 && window.getPNGBase64();");
						if (result instanceof String) {
							byte[] imageBytes = Base64.getDecoder().decode((String) result);
							try (FileOutputStream fos = new FileOutputStream(file)) {
								fos.write(imageBytes);
							}
						}
					} catch (Exception e) {
						System.err.println("Error saving PNG: " + e.getMessage());
					}
				}
			}
		});
		saveButton.getStyleClass().add("menu-button");
		// Add saveButton to controls

		HBox controls = new HBox(9, refreshButton,layoutLabel ,layoutCombo, metricLabel, nodeSizeMetricCombo, saveButton);
		controls.setStyle("-fx-padding: 10; -fx-background-color: #ffffff;");
		controls.getStyleClass().add("menu-bar");
		controls.setMaxWidth(Double.MAX_VALUE);
		VBox.setVgrow(controls, Priority.NEVER);

		VBox root = new VBox();
		root.getChildren().addAll(controls, webView);
		VBox.setVgrow(webView, Priority.ALWAYS);
		root.setFillWidth(true);
		root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		root.setPadding(Insets.EMPTY);
		root.getStyleClass().add("content-container");

		Scene scene = new Scene(root, 800, 600);
		scene.getStylesheets().add(getClass().getResource("app-style.css").toExternalForm());
		stage.setScene(scene);
		stage.setTitle("Cytoscape.js Graph Window");
		stage.setResizable(true);
		(new UIUtils()).setWindowIcon(stage);
		
		webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
			if (newVal == Worker.State.SUCCEEDED) {
				JSObject window = (JSObject) webEngine.executeScript("window");
				window.setMember("javaApp", new JavaApp());
				webEngine.executeScript("console.log = function(message) { javaApp.log(message); };");
				webEngine.executeScript("console.log('Cytoscape.js loaded in JavaFX');");
				if (pendingJson != null) {
					loadDataIntoWebView(pendingJson);
					pendingJson = null;
				}
			}
		});
	}

	private boolean isWebViewReady() {
		return webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED;
	}

	private void loadDataIntoWebView(String json) {
		String safeJson = json.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", "")
				.replace("\n", "");
		String js = "window.loadCytoscapeData && window.loadCytoscapeData(JSON.parse('" + safeJson + "'));";
		javafx.application.Platform.runLater(() -> webEngine.executeScript(js));
	}

	public void reloadGraphData() {
		javafx.application.Platform.runLater(() -> {
			layoutCombo.setValue("circle"); // Set ComboBox to circle
			try {
				String json = new String(Files.readAllBytes(Paths.get("cytoscape-data.json")));
				if (isWebViewReady()) {
					loadDataIntoWebView(json);
				} else {
					pendingJson = json;
				}
			} catch (Exception e) {
				System.err.println("No cytoscape-data.json to load: " + e.getMessage());
			}
		});
	}

	public void setLayout(String layout) {
		if (isWebViewReady()) {
			webEngine.executeScript("window.setLayout && window.setLayout('" + layout + "');");
		}
	}

	public void show() {
		stage.show();
	}
}
