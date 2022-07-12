package fr.ufc.metaobs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class Field {

    private static int static_id = 0;
    private final int id;
    private final StringProperty originalName;
    private final StringProperty newName;
    private FieldTypeSize typeSize;

    public Field(String originalName) {
        id = static_id++;
        this.originalName = new SimpleStringProperty(originalName);
        this.newName = new SimpleStringProperty();
    }

    public String getName() {
        if (newName.isNotNull().get() && !newName.get().isBlank()) {
            return newName.get();
        }
        return originalName.get();
    }

    public String getOriginalName() {
        return originalName.get();
    }

    public StringProperty originalNameProperty() {
        return originalName;
    }

    public String getNewName() {
        return newName.get();
    }

    public void setNewName(String newName) {
        this.newName.set(newName);
    }

    public StringProperty newNameProperty() {
        return newName;
    }

    public FieldTypeSize getTypeSize() {
        return typeSize;
    }

    public void setTypeSize(FieldTypeSize typeSize) {
        this.typeSize = typeSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return Objects.equals(getOriginalName(), field.getOriginalName()) && id == field.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getOriginalName());
    }
}
