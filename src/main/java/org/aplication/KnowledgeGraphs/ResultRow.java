package org.aplication.KnowledgeGraphs;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ResultRow {
    private final StringProperty subject;
    private final StringProperty predicate;
    private final StringProperty object;

    public ResultRow(String subject, String predicate, String object) {
        this.subject = new SimpleStringProperty(subject);
        this.predicate = new SimpleStringProperty(predicate);
        this.object = new SimpleStringProperty(object);
    }

    public String getSubject() { return subject.get(); }
    public void setSubject(String value) { subject.set(value); }
    public StringProperty subjectProperty() { return subject; }

    public String getPredicate() { return predicate.get(); }
    public void setPredicate(String value) { predicate.set(value); }
    public StringProperty predicateProperty() { return predicate; }

    public String getObject() { return object.get(); }
    public void setObject(String value) { object.set(value); }
    public StringProperty objectProperty() { return object; }
}
