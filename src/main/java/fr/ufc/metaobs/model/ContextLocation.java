package fr.ufc.metaobs.model;

public class ContextLocation extends SubContext {

    private ContextLocation refLocation;

    public ContextLocation(String locationName) {
        super(locationName);
    }

    public ContextLocation getRefLocation() {
        return refLocation;
    }

    public void setRefLocation(ContextLocation refLocation) {
        this.refLocation = refLocation;
    }

}
