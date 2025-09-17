package org.aplication.KnowledgeGraphs;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class CytoscapeWindow {

    private Stage stage;
    private WebView webView;
    private WebEngine webEngine;

    public CytoscapeWindow() {
        stage = new Stage();
        webView = new WebView();
        webEngine = webView.getEngine();

        // Load the HTML file from resources
        String htmlPath = getClass().getResource("/html/cytoscape.html").toExternalForm();
        if (htmlPath == null) {
            System.err.println("Error: cytoscape.html not found in resources/html/");
            return;
        }
        webEngine.load(htmlPath);

        // Optional: Button to interact with the graph
        Button addNodeButton = new Button("Add Node");
        addNodeButton.setOnAction(event -> {
            if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.call("addNode", "newNode" + System.currentTimeMillis());
            }
        });

        // Layout
        VBox root = new VBox(addNodeButton, webView);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Cytoscape.js Graph Window");

        // Ensure JavaScript runs after page load
        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == Worker.State.SUCCEEDED) {
                webEngine.executeScript("console.log('Cytoscape.js loaded in JavaFX');");
            }
        });
    }

    // Method to show the window
    public void show() {
        stage.show();
    }
}