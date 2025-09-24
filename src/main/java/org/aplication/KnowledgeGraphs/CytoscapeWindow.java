package org.aplication.KnowledgeGraphs;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class CytoscapeWindow {

    private final Stage stage;
    private final WebView webView;
    private final WebEngine webEngine;
    private String pendingJson = null;


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
       
        ComboBox<String> layoutCombo = new ComboBox<>();
        layoutCombo.getItems().addAll("grid", "circle", "concentric", ""
        		+ "breadthfirst", "cose", "cose-bilkent", "dagre");
        layoutCombo.setValue("circle");
        layoutCombo.setOnAction(event -> {
        				String layout = layoutCombo.getValue();
        				setLayout(layout);
        });

        HBox controls = new HBox(5, refreshButton);
        controls.setStyle("-fx-padding: 5; -fx-background-color: #ddd;");
        controls.getChildren().add(layoutCombo);
        	  
       
        VBox root = new VBox(controls, webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
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

    // Helper to generate a color palette in Java
    private String generateColorPaletteJson(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"colors\": [");
        int avgR = 0, avgG = 0, avgB = 0;
        for (int i = 0; i < count; i++) {
            int colorInt = (int)(Math.random() * 0xFFFFFF);
            String color = String.format("#%06X", colorInt);
            sb.append('"').append(color).append('"');
            if (i < count - 1) sb.append(",");
            avgR += (colorInt >> 16) & 0xFF;
            avgG += (colorInt >> 8) & 0xFF;
            avgB += colorInt & 0xFF;
        }
        // Compute a contrasting background color
        avgR /= count; avgG /= count; avgB /= count;
        int contrastR = 255 - avgR;
        int contrastG = 255 - avgG;
        int contrastB = 255 - avgB;
        String background = String.format("#%02X%02X%02X", contrastR, contrastG, contrastB);
        sb.append("], \"background\": \"").append(background).append("\"}");
        return sb.toString();
    }
    
    public void setLayout(String layout) {
		if (isWebViewReady()) {
			webEngine.executeScript("window.setLayout && window.setLayout('" + layout + "');");
		}
	}

    private void loadDataIntoWebView(String json) {
        String paletteJson = generateColorPaletteJson(5);
        String safeJson = json.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\r", "").replace("\n", "");
        String js = "window.loadCytoscapeData && window.loadCytoscapeData(JSON.parse('" + safeJson + "'), JSON.parse('" + paletteJson + "'));";
        javafx.application.Platform.runLater(() -> webEngine.executeScript(js));
    }
}
