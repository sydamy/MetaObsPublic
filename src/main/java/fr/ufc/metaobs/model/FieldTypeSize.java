package fr.ufc.metaobs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FieldTypeSize {

    private final StringProperty type;
    private final StringProperty size;

    public FieldTypeSize(String type) {
        this.type = new SimpleStringProperty(type);
        this.size = new SimpleStringProperty();
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getSize() {
        return size.get();
    }

    public void setSize(String size) {
        this.size.set(size);
    }

    public StringProperty sizeProperty() {
        return size;
    }

    @Override
    public String toString() {
        return getSize() != null ? getType() + "(" + getSize() + ')' : getType();
    }
}
