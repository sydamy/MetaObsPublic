package fr.ufc.metaobs.model;

import fr.ufc.metaobs.handlers.OwlHandler;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import org.apache.jena.ontology.OntClass;

import java.util.Comparator;

public class Observatory {

    private final ObservableList<MetadataInfo> observableList;
    private final ListProperty<MetadataInfo> listInfos;

    public Observatory() {
        observableList = FXCollections.observableArrayList();
        listInfos = new SimpleListProperty<>(new SortedList<>(observableList));
    }

    public ObservableList<MetadataInfo> getListInfos() {
        return listInfos.get();
    }

    public void setListInfos(ObservableList<MetadataInfo> listInfos) {
        this.listInfos.set(listInfos);
    }

    public ListProperty<MetadataInfo> listInfosProperty() {
        return listInfos;
    }

    public MetadataInfo getInfo(String name) {
        for (MetadataInfo metadataInfo : this.observableList) {
            if (name.equals(metadataInfo.getName())) {
                return metadataInfo;
            }
        }
        return null;
    }

    public void addInfo(String name, String iri, String value) {
        this.addInfo(new MetadataInfo(name, iri, value));
    }

    public void addInfo(MetadataInfo metadataInfo) {
        this.observableList.add(metadataInfo);
    }

    public void setInfoOrders(OwlHandler owlHandler) {
        for (MetadataInfo metadataInfo : observableList) {
            OntClass ontClass = owlHandler.getClassFromURI(metadataInfo.getIri());
            int order = owlHandler.getMetadataOrder(ontClass);
            metadataInfo.setOrder(order);
        }
        observableList.sort(Comparator.comparingInt(MetadataInfo::getOrder));
    }

}
