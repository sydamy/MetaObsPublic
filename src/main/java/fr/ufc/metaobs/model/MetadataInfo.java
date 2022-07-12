package fr.ufc.metaobs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class MetadataInfo implements Comparable<MetadataInfo> {

    private final StringProperty name;
    private final StringProperty iri;
    private final StringProperty value;
    private int order;

    public MetadataInfo(String name, String iri, String value) {
        this.name = new SimpleStringProperty(name);
        this.iri = new SimpleStringProperty(iri);
        this.value = new SimpleStringProperty(value);
        this.order = 0;
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

    public String getIri() {
        return iri.get();
    }

    public void setIri(String iri) {
        this.iri.set(iri);
    }

    public StringProperty iriProperty() {
        return iri;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public StringProperty valueProperty() {
        return value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataInfo that = (MetadataInfo) o;
        return order == that.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(order);
    }

    @Override
    public int compareTo(MetadataInfo metadataInfo) {
        return order - metadataInfo.order;
    }

}
