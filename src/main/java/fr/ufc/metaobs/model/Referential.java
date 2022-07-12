package fr.ufc.metaobs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Referential {

    private final StringProperty repositoryName;
    private final StringProperty repositoryIri;
    private final StringProperty nameInRepository;
    private final StringProperty iriInRepository;

    public Referential(String repositoryName, String repositoryIri, String nameInRepository, String iriInRepository) {
        this.repositoryName = new SimpleStringProperty(repositoryName);
        this.repositoryIri = new SimpleStringProperty(repositoryIri);
        this.nameInRepository = new SimpleStringProperty(nameInRepository);
        this.iriInRepository = new SimpleStringProperty(iriInRepository);
    }

    public String getRepositoryName() {
        return repositoryName.get();
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName.set(repositoryName);
    }

    public StringProperty repositoryNameProperty() {
        return repositoryName;
    }

    public String getRepositoryIri() {
        return repositoryIri.get();
    }

    public void setRepositoryIri(String repositoryIri) {
        this.repositoryIri.set(repositoryIri);
    }

    public StringProperty repositoryIriProperty() {
        return repositoryIri;
    }

    public String getNameInRepository() {
        return nameInRepository.get();
    }

    public void setNameInRepository(String nameInRepository) {
        this.nameInRepository.set(nameInRepository);
    }

    public StringProperty nameInRepositoryProperty() {
        return nameInRepository;
    }

    public String getIriInRepository() {
        return iriInRepository.get();
    }

    public void setIriInRepository(String iriInRepository) {
        this.iriInRepository.set(iriInRepository);
    }

    public StringProperty iriInRepositoryProperty() {
        return iriInRepository;
    }

}
