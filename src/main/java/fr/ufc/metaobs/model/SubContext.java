package fr.ufc.metaobs.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class SubContext {

    protected final StringProperty name;
    protected final ListProperty<Field> propertiesList;

    public SubContext(String name) {
        this.name = new SimpleStringProperty(name);
        this.propertiesList = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public ObservableList<Field> getPropertiesList() {
        return propertiesList.get();
    }

    public void clearProperties() {
        propertiesList.clear();
    }

    public void putProperty(String originalName, String newName, String typeSize) {
        Field field = new Field(originalName);
        field.setNewName(newName);
        FieldTypeSize fieldTypeSize;
        if (typeSize.contains("(")) {
            int indexBracket = typeSize.indexOf('(');
            String type = typeSize.substring(0, indexBracket);
            String size = typeSize.substring(indexBracket + 1, typeSize.indexOf(')'));
            fieldTypeSize = new FieldTypeSize(type);
            fieldTypeSize.setSize(size);
        } else {
            fieldTypeSize = new FieldTypeSize(typeSize);
        }
        field.setTypeSize(fieldTypeSize);
        this.propertiesList.add(field);
    }

}
