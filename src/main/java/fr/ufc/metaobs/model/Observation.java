package fr.ufc.metaobs.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

public class Observation {

    private final StringProperty name;
    private final StringProperty multiplicity;
    private final ListProperty<Characteristic> characteristics;
    private FieldTypeSize externalId;
    private int id;
    private Entity entity;
    private Context context;

    public Observation() {
        this.id = -1;
        this.name = new SimpleStringProperty();
        this.multiplicity = new SimpleStringProperty();
        this.characteristics = new SimpleListProperty<>();
    }

    public Observation(int id, String name, String multiplicity) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.multiplicity = new SimpleStringProperty(multiplicity);
        this.characteristics = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Entity getEntity() {
        return entity;
    }

    /**
     * Définit l'entité de cette observation et ajoute cette observation aux observations de l'entité
     *
     * @param entity l'entité à laquelle cette observation sera liée
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
        this.entity.addObservation(this);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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

    public String getMultiplicity() {
        return multiplicity.get();
    }

    public void setMultiplicity(String multiplicity) {
        this.multiplicity.set(multiplicity);
    }

    public StringProperty multiplicityProperty() {
        return multiplicity;
    }

    public FieldTypeSize getExternalId() {
        return externalId;
    }

    public void setExternalId(FieldTypeSize externalId) {
        this.externalId = externalId;
    }

    public ObservableList<Characteristic> getCharacteristics() {
        return characteristics.get();
    }

    public void setCharacteristics(ObservableList<Characteristic> characteristics) {
        this.characteristics.set(characteristics);
    }

    public void addCharacteristic(Characteristic characteristic) {
        this.characteristics.add(characteristic);
    }

    public void removeCharacteristic(Characteristic characteristic) {
        this.characteristics.remove(characteristic);
        //si l'observation n'a plus de caractéristiques, on la supprime
        if (characteristics.size() < 1) {
            getEntity().removeObservation(this);
        }
    }

    public ListProperty<Characteristic> characteristicsProperty() {
        return characteristics;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Observation that = (Observation) o;
        return id == that.id && entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entity);
    }

}
