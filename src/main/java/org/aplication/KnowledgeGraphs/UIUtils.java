package org.aplication.KnowledgeGraphs;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class UIUtils {

	// Utility method to show error messages in a dialog
	public static void showErrorDialog(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}
	
	public void setWindowIcon(Stage stage) {
		String[] iconSizes = { "128", "48", "32", "16" };

		for (String size : iconSizes) {
			try {
				String iconPath = "/icons/app-icon-" + size + ".png";
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


	// Utility method to show information messages in a dialog
	public static void showInfoDialog(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}

	
}