package fr.ufc.metaobs.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;

import java.util.Objects;

public class Characteristic {

    private final StringProperty name;
    private final StringProperty measure;
    private final BooleanProperty nullable;
    private final StringProperty protocol;
    private int refEntity;
    private int refObs;
    private Entity entity;
    private Observation observation;
    private String iri;
    /**
     * Unit, Classification, Tool sont des paires de String.
     * La clé de la paire est l'IRI, la valeur de la paire est le nom.
     */
    private Pair<String, String> unit;
    private Pair<String, String> classification;
    private Pair<String, String> tool;
    private String fieldType;
    private String fieldSize;

    public Characteristic(int refEntity) {
        this.refEntity = refEntity;
        this.refObs = 0;
        name = new SimpleStringProperty();
        measure = new SimpleStringProperty();
        nullable = new SimpleBooleanProperty();
        protocol = new SimpleStringProperty();
    }

    public Characteristic(int refEntity, int refObs, String name, String iri, boolean nullable, String fieldType) {
        this.refEntity = refEntity;
        this.refObs = refObs;
        this.name = new SimpleStringProperty(name);
        this.iri = iri;
        this.measure = new SimpleStringProperty(this.iri.substring(this.iri.lastIndexOf('#') + 1));
        this.nullable = new SimpleBooleanProperty(nullable);
        this.protocol = new SimpleStringProperty();
        setFieldTypeSize(fieldType);
    }

    public int getRefEntity() {
        return refEntity;
    }

    public void setRefEntity(int refEntity) {
        this.refEntity = refEntity;
    }

    public int getRefObs() {
        return refObs;
    }

    public void setRefObs(int refObs) {
        this.refObs = refObs;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
        setMeasure(this.iri.substring(this.iri.lastIndexOf('#') + 1));
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        if (this.entity != null) {
            this.entity.addCharacteristic(this);
            this.refEntity = this.entity.getId();
        }
    }

    public void removeFromEntity() {
        if (entity != null) {
            entity.removeCharacteristic(this);
        }
    }

    public Observation getObservation() {
        return observation;
    }

    public void setObservation(Observation observation) {
        this.observation = observation;
        if (this.observation != null) {
            this.observation.addCharacteristic(this);
        }
    }

    public void removeFromObservation() {
        if (observation != null) {
            observation.removeCharacteristic(this);
        }
        setRefObs(0);
        setObservation(null);
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

    public String getMeasure() {
        return measure.get();
    }

    public void setMeasure(String measure) {
        this.measure.set(measure);
    }

    public StringProperty measureProperty() {
        return measure;
    }

    public String getProtocol() {
        return protocol.get();
    }

    public void setProtocol(String protocol) {
        this.protocol.set(protocol);
    }

    public StringProperty protocolProperty() {
        return protocol;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldSize() {
        return fieldSize;
    }

    public void setFieldSize(String fieldSize) {
        this.fieldSize = fieldSize;
    }

    public void setFieldTypeSize(String fieldType) {
        if (fieldType.contains("(")) {
            int indexBracket = fieldType.indexOf('(');
            this.fieldType = fieldType.substring(0, indexBracket);
            this.fieldSize = fieldType.substring(indexBracket + 1, fieldType.indexOf(')'));
        } else {
            this.fieldType = fieldType;
        }
    }

    public boolean isNullable() {
        return nullable.get();
    }

    public void setNullable(boolean nullable) {
        this.nullable.set(nullable);
    }

    public BooleanProperty nullableProperty() {
        return nullable;
    }

    public Pair<String, String> getUnit() {
        return unit;
    }

    public void setUnit(Pair<String, String> unit) {
        this.unit = unit;
    }

    public Pair<String, String> getClassification() {
        return classification;
    }

    public void setClassification(Pair<String, String> classification) {
        this.classification = classification;
    }

    public Pair<String, String> getTool() {
        return tool;
    }

    public void setTool(Pair<String, String> tool) {
        this.tool = tool;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Characteristic that = (Characteristic) o;
        return refEntity == that.refEntity && refObs == that.refObs && name.get().equals(that.name.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(refEntity, refObs, name.get());
    }

}
