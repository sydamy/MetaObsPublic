package fr.ufc.metaobs.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class Entity {

    private final StringProperty name;
    private final StringProperty type;
    private final Map<Integer, Observation> observationsMap; //utile pour lier les caractéristiques aux observations
    private final ListProperty<Observation> observations;
    private final ListProperty<Characteristic> characteristics;
    private int id;
    private int refEntity;
    private Entity refEntityObject;
    private String iri;
    private Context context;
    private Referential referential;
    private FieldTypeSize externalId;

    public Entity() {
        this(-1, "", "", "");
    }

    public Entity(String name, String type, String iri) {
        this(-1, name, type, iri);
    }

    public Entity(int id, String name, String type, String iri) {
        //-1 par défaut pour pas de refEntity
        this.refEntity = -1;
        this.refEntityObject = null;
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.iri = iri;
        this.externalId = null;
        this.observations = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.observationsMap = new HashMap<>();
        this.characteristics = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRefEntity() {
        return refEntity;
    }

    public void setRefEntity(int refEntity) {
        this.refEntity = refEntity;
    }

    public Entity getRefEntityObject() {
        return refEntityObject;
    }

    public void setRefEntityObject(Entity refEntityObject) {
        this.refEntityObject = refEntityObject;
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

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public FieldTypeSize getExternalId() {
        return externalId;
    }

    public void setExternalId(FieldTypeSize externalId) {
        this.externalId = externalId;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Referential getReferential() {
        return referential;
    }

    public void setReferential(Referential referential) {
        this.referential = referential;
    }

    public ObservableList<Characteristic> getCharacteristics() {
        return characteristics.get();
    }

    public void setCharacteristics(List<Characteristic> characteristics) {
        this.characteristics.set(FXCollections.observableList(characteristics));
    }

    /**
     * Ajoute une caractéristique à l'entité.
     * À ne pas utiliser directement, utiliser plutôt Characteristic#setEntity
     *
     * @param characteristic la caractéristique à ajouter
     * @see Characteristic#setEntity(Entity)
     */
    public void addCharacteristic(Characteristic characteristic) {
        this.characteristics.add(characteristic);
    }

    public void removeCharacteristic(Characteristic characteristic) {
        this.characteristics.remove(characteristic);
    }

    public ListProperty<Characteristic> characteristicsProperty() {
        return characteristics;
    }

    public ObservableList<Observation> getObservations() {
        return observations.get();
    }

    public void setObservations(ObservableList<Observation> observations) {
        this.observations.set(observations);
    }

    /**
     * Ajoute une observation à l'entité.
     * À ne pas utiliser directement, utiliser plutôt Observation#setEntity
     *
     * @param observation l'observation à ajouter
     * @see Observation#setEntity(Entity)
     */
    public void addObservation(Observation observation) {
        //si l'observation n'a pas d'id, alors il faut lui en ajouter un
        if (observation.getId() < 0) {
            int newId = 1;
            Optional<Integer> max = this.observationsMap.keySet().stream().max(Integer::compareTo);
            if (max.isPresent()) {
                newId = max.get() + 1;
            }
            observation.setId(newId);
        }
        this.observations.add(observation);
        this.observationsMap.put(observation.getId(), observation);
    }

    public void removeObservation(Observation observation) {
        this.observations.remove(observation);
        this.observationsMap.remove(observation.getId());
    }

    public ListProperty<Observation> observationsProperty() {
        return observations;
    }

    public Observation getObservationById(int id) {
        return observationsMap.get(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
