package fr.ufc.metaobs.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SelectableCharacteristic {

    private final BooleanProperty selected;
    private final Characteristic characteristic;

    public SelectableCharacteristic(boolean selected, Characteristic characteristic) {
        this.selected = new SimpleBooleanProperty(selected);
        this.characteristic = characteristic;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public Characteristic getCharacteristic() {
        return characteristic;
    }
}
