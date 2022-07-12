package fr.ufc.metaobs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ContextDate extends SubContext {

    private final StringProperty dateFormat;
    private final StringProperty dateStandard;

    public ContextDate(String dateName) {
        super(dateName);
        this.dateFormat = new SimpleStringProperty();
        this.dateStandard = new SimpleStringProperty();
    }

    public String getDateFormat() {
        return dateFormat.get();
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat.set(dateFormat);
    }

    public StringProperty dateFormatProperty() {
        return dateFormat;
    }

    public String getDateStandard() {
        return dateStandard.get();
    }

    public void setDateStandard(String dateStandard) {
        this.dateStandard.set(dateStandard);
    }

    public StringProperty dateStandardProperty() {
        return dateStandard;
    }

}
