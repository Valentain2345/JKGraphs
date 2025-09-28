package org.aplication.KnowledgeGraphs;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

	private static Scene scene;

	private static Parent loadFXML(String fxml) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
		return fxmlLoader.load();
	}

	public static void main(String[] args) {
		launch();
	}

	static void setRoot(String fxml) throws IOException {
		scene.setRoot(loadFXML(fxml));
	}

	
	@Override
	public void start(Stage stage) throws IOException {
		(new UIUtils()).setWindowIcon(stage);

		// Load FXML
		Parent root = loadFXML("primary");

		// Create scene with initial size
		scene = new Scene(root, 900, 700);

		// Set minimum window size to prevent UI from becoming too small
		stage.setMinWidth(600);
		stage.setMinHeight(500);

		
		stage.setTitle("Knowledge Graphs Application");
		stage.setScene(scene);

		// Center the window on screen
		stage.centerOnScreen();

		stage.show();
	}
}
