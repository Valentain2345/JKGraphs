package org.aplication.KnowledgeGraphs;

import java.io.IOException;
import javafx.fxml.FXML;

public class PrimaryController {
	private CytoscapeWindow cytoscapeWindow=new CytoscapeWindow();
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    private void showGraph() throws IOException {
        cytoscapeWindow.show();
    }
    
    
}
