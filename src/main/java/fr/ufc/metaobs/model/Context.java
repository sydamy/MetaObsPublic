package fr.ufc.metaobs.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Context {

    private final ObjectProperty<ContextActor> contextActor;
    private final ObjectProperty<ContextLocation> contextLocation;
    private final ObjectProperty<ContextDate> contextDate;


    public Context(ContextActor contextActor, ContextLocation contextLocation, ContextDate contextDate) {
        this.contextActor = new SimpleObjectProperty<>(contextActor);
        this.contextLocation = new SimpleObjectProperty<>(contextLocation);
        this.contextDate = new SimpleObjectProperty<>(contextDate);
    }

    public ContextActor getContextActor() {
        return contextActor.get();
    }

    public void setContextActor(ContextActor contextActor) {
        this.contextActor.set(contextActor);
    }

    public ObjectProperty<ContextActor> contextActorProperty() {
        return contextActor;
    }

    public ContextLocation getContextLocation() {
        return contextLocation.get();
    }

    public void setContextLocation(ContextLocation contextLocation) {
        this.contextLocation.set(contextLocation);
    }

    public ObjectProperty<ContextLocation> contextLocationProperty() {
        return contextLocation;
    }

    public ContextDate getContextDate() {
        return contextDate.get();
    }

    public void setContextDate(ContextDate contextDate) {
        this.contextDate.set(contextDate);
    }

    public ObjectProperty<ContextDate> contextDateProperty() {
        return contextDate;
    }

}
