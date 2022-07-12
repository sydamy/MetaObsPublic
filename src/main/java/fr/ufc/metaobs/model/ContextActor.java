package fr.ufc.metaobs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ContextActor extends SubContext {

    private final StringProperty typeActor;
    private ContextActor refContextActor;

    public ContextActor(String actorName) {
        super(actorName);
        this.typeActor = new SimpleStringProperty();
    }

    public String getTypeActor() {
        return typeActor.get();
    }

    public void setTypeActor(String typeActor) {
        this.typeActor.set(typeActor);
    }

    public ContextActor getRefContextActor() {
        return refContextActor;
    }

    public void setRefContextActor(ContextActor refContextActor) {
        this.refContextActor = refContextActor;
    }

}
