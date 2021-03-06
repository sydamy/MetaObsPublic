package fr.ufc.metaobs.model;

import fr.ufc.metaobs.utils.FileSystemUtils;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Project {

    private final StringProperty name;
    private final Map<Integer, Entity> entitiesMap;
    private final ListProperty<Entity> entities;
    private final ListProperty<Characteristic> characteristics;
    private OntModel projectModel;
    private File[] projectOwls;
    private String repProject;
    private String ontology;
    private String baseUri;
    private Observatory observatory;

    public Project(String name, String ontology, String baseUri) {
        this.name = new SimpleStringProperty(name);
        this.ontology = ontology;
        this.baseUri = baseUri;
        observatory = new Observatory();
        entitiesMap = new HashMap<>();
        entities = new SimpleListProperty<>(FXCollections.observableArrayList());
        characteristics = new SimpleListProperty<>(FXCollections.observableArrayList());
        projectModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
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

    public String getRepProject() {
        return repProject;
    }

    public void setRepProject(String repProject) {
        this.repProject = repProject;
    }

    public String getOntology() {
        return ontology;
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    public OntModel getProjectModel() {
        return projectModel;
    }

    /**
     * D??finit le(s) fichier(s) owl pour les ontologies du projet.
     *
     * @param projectOwls les fichiers owl
     */
    public void setProjectOwls(File[] projectOwls) {
        this.projectOwls = projectOwls;
    }

    /**
     * Charge les ontologies du projet en r??initialisant le mod??le avant.
     */
    public void loadProjectOwls() {
        projectModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        if (projectOwls != null) {
            for (File file : projectOwls) {
                String path = FileSystemUtils.addFileUriScheme(file.getAbsolutePath());
                projectModel.read(path, "RDF/XML");
            }
        }
    }

    public Observatory getObservatory() {
        return observatory;
    }

    public void setObservatory(Observatory observatory) {
        this.observatory = observatory;
    }

    public ObservableList<Entity> getEntities() {
        return entities.get();
    }

    public void setEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            addEntity(entity);
        }
    }

    public ListProperty<Entity> entitiesProperty() {
        return entities;
    }

    public ObservableList<Characteristic> getCharacteristics() {
        return characteristics.get();
    }

    public void setCharacteristics(List<Characteristic> characteristics) {
        this.characteristics.set(FXCollections.observableList(characteristics));
    }

    public ListProperty<Characteristic> characteristicsProperty() {
        return characteristics;
    }

    /**
     * Ajoute une entit?? au projet et lui d??finit un id si n??cessaire.
     *
     * @param entity l'entit?? ?? ajouter
     */
    public void addEntity(Entity entity) {
        //si l'entit?? n'a pas d'id, alors il faut lui en ajouter un
        if (entity.getId() < 0) {
            int newId = 1;
            Optional<Integer> max = entitiesMap.keySet().stream().max(Integer::compareTo);
            if (max.isPresent()) {
                newId = max.get() + 1;
            }
            entity.setId(newId);
        }
        entitiesMap.put(entity.getId(), entity);
        entities.add(entity);
    }

    /**
     * Retourne une entit?? r??cup??r??e par son id.
     *
     * @param id l'identifiant de l'entit?? ?? trouver
     * @return une entit?? dont l'id est ??gal ?? l'id pass?? en param??tres
     */
    public Entity getEntityById(int id) {
        return entitiesMap.get(id);
    }

    /**
     * Supprime une entit?? et ses caract??ristiques du projet.
     *
     * @param entity l'entit?? ?? supprimer
     */
    public void removeEntity(Entity entity) {
        for (Characteristic characteristic : entity.getCharacteristics()) {
            characteristics.remove(characteristic);
        }
        List<Entity> entityList = entities.stream()
                .filter(otherEntity -> otherEntity.getRefEntity() == entity.getId())
                .collect(Collectors.toList());
        Set<Map.Entry<Integer, Entity>> entrySet = entitiesMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getRefEntity() == entity.getId())
                .collect(Collectors.toSet());
        for (Entity value : entityList) {
            removeEntity(value);
        }
        for (Map.Entry<Integer, Entity> entry : entrySet) {
            removeEntity(entry.getValue());
        }
        entitiesMap.remove(entity.getId());
        entities.remove(entity);
    }

    public boolean entityIsReferenced(Entity entity) {
        for (Entity otherEntity : entities) {
            if (otherEntity.getRefEntity() == entity.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ajoute une caract??ristique au projet et la lie ?? l'entit?? et l'observation auxquelles elle doit ??tre li??e.
     *
     * @param characteristic la caract??ristique ?? ajouter
     */
    public void addCharacteristic(Characteristic characteristic) {
        characteristics.add(characteristic);
        linkCharacteristic(characteristic);
    }

    /**
     * Supprime une caract??risrtique du projet, n??cessite de la supprimer aussi des entit??s et des observations li??es.
     *
     * @param characteristic la caract??ristique ?? supprimer
     */
    public void removeCharacteristic(Characteristic characteristic) {
        characteristics.remove(characteristic);
        characteristic.removeFromEntity();
        characteristic.removeFromObservation();
    }

    /**
     * Ajoute une observation au projet en sp??cifiant son entit?? et lui d??finit un id si n??cessaire.
     *
     * @param entity      l'entit?? pour laquelle ajouter l'observation
     * @param observation l'observation ?? ajouter
     */
    public void addObservation(Entity entity, Observation observation) {
        //si l'observation n'a pas d'id, alors il faut lui en ajouter un
        if (observation.getId() < 0) {
            int newId = 1;
            //on r??cup??re l'id max des observations de l'entit?? donn??e
            Optional<Observation> maxObservation = entity.getObservations().stream().max(Comparator.comparingInt(Observation::getId));
            if (maxObservation.isPresent()) {
                newId = maxObservation.get().getId() + 1;
            }
            observation.setId(newId);
        }
        observation.setEntity(entity);
        //on d??finit la propri??t?? refObs des caract??ristiques seulement apr??s avoir donn?? un id ?? l'observation
        for (Characteristic characteristic : observation.getCharacteristics()) {
            characteristic.setRefObs(observation.getId());
        }
    }

    /**
     * Supprime une observation du projet.
     *
     * @param observation l'observation ?? supprimer
     */
    public void removeObservation(Observation observation) {
        //on lib??re les caract??ristiques qui ??taient li??es ?? cette observation
        for (Characteristic characteristic : observation.getCharacteristics()) {
            characteristic.setRefObs(0);
            characteristic.setObservation(null);
        }
        observation.getEntity().removeObservation(observation);
    }

    /**
     * Lie les entit??s aux autres entit??s auxquelles elles doivent ??tre li??es, les entit??s de type 'Sample'
     * doivent r??f??rencer une autre entit?? (de type 'SampleUnit' par exemple).
     */
    public void linkEntities() {
        for (Entity entity : entities) {
            if (entity.getRefEntity() > -1) {
                entity.setRefEntityObject(getEntityById(entity.getRefEntity()));
            }
        }
    }

    /**
     * Lie les caract??ristiques aux entit??s et aux observations auxquelles elles doivent ??tre li??es.
     */
    public void linkCharacteristics() {
        for (Characteristic characteristic : characteristics) {
            linkCharacteristic(characteristic);
        }
    }

    /**
     * Lie une caract??ristique ?? l'entit?? et ?? l'observation ?? laquelle elle doit ??tre li??e.
     *
     * @param characteristic la caract??ristique ?? lier
     */
    public void linkCharacteristic(Characteristic characteristic) {
        Entity refEntity = getEntityById(characteristic.getRefEntity());
        Observation refObs = refEntity.getObservationById(characteristic.getRefObs());
        characteristic.setEntity(refEntity);
        characteristic.setObservation(refObs);
    }

}
