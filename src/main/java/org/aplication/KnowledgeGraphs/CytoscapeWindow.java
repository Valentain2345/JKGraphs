package org.aplication.KnowledgeGraphs;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.nio.file.Files;
import java.nio.file.Paths;

public class CytoscapeWindow {

    private final Stage stage;
    private final WebView webView;
    private final WebEngine webEngine;
    private String pendingJson = null;


    public CytoscapeWindow() {
        stage = new Stage();
        webView = new WebView();
        webEngine = webView.getEngine();

        String htmlPath = getClass().getResource("/html/cytoscape.html").toExternalForm();
        if (htmlPath == null) {
            System.err.println("Error: cytoscape.html not found in resources/html/");
            return;
        }
        webEngine.load(htmlPath);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> reloadGraphData());

        Button addNodeButton = new Button("Add Node");
        addNodeButton.setOnAction(event -> {
            if (isWebViewReady()) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.call("addNode");
            }
        });

        ToggleButton addEdgeToggleButton = new ToggleButton("Add Edge");
        addEdgeToggleButton.setOnAction(event -> {
            if (isWebViewReady()) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.call("toggleEdgeMode", addEdgeToggleButton.isSelected());
            }
        });

        HBox controls = new HBox(5, refreshButton, addNodeButton, addEdgeToggleButton);
        VBox root = new VBox(controls, webView);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Cytoscape.js Graph Window");

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

    public class JavaApp {
        public void log(String message) {
            System.out.println("JS Console: " + message);
        }
    }

    public void show() {
        stage.show();
    }

    public void reloadGraphData() {
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
    }

    private void loadDataIntoWebView(String json) {
        String safeJson = json.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", "").replace("\n", "");
        String js = "window.loadCytoscapeData && window.loadCytoscapeData(JSON.parse('" + safeJson + "'));";
        javafx.application.Platform.runLater(() -> webEngine.executeScript(js));
    }
}