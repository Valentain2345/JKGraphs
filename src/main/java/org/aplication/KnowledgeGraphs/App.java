package org.aplication.KnowledgeGraphs;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    
	@Override
    public void start(Stage stage) throws IOException {
		setApplicationIcon(stage);
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
	    }


	private void setApplicationIcon(Stage stage) {
	    String[] iconSizes = {"128", "48", "32", "16"};
	    
	    for (String size : iconSizes) {
	        try {
	            String iconPath = "/Icons/app-icon-" + size + ".png";
	            Image icon = new Image(getClass().getResourceAsStream(iconPath));
	            if (!icon.isError()) {
	                stage.getIcons().add(icon);
	                System.out.println("Icon loaded: " + size + "x" + size);
	                break;
	            }
	        } catch (Exception e) {
	            System.err.println("Error loading " + size + " icon: " + e.getMessage());
	        }
	    }
	}
	
	
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
    	
        launch();
    }

}