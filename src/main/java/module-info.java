module org.aplication.KnowledgeGraphs {
    requires javafx.controls;
    requires javafx.fxml;
	requires hgdb;
	requires java.transaction.xa;
	requires java.naming;
	requires hgbdbje;
	requires je;
	requires java.sql;
	
    opens org.aplication.KnowledgeGraphs to javafx.fxml;
    exports org.aplication.KnowledgeGraphs;
}
