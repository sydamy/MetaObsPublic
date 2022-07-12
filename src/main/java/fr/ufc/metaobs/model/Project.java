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
     * Définit le(s) fichier(s) owl pour les ontologies du projet.
     *
     * @param projectOwls les fichiers owl
     */
    public void setProjectOwls(File[] projectOwls) {
        this.projectOwls = projectOwls;
    }

    /**
     * Charge les ontologies du projet en réinitialisant le modèle avant.
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
     * Ajoute une entité au projet et lui définit un id si nécessaire.
     *
     * @param entity l'entité à ajouter
     */
    public void addEntity(Entity entity) {
        //si l'entité n'a pas d'id, alors il faut lui en ajouter un
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
     * Retourne une entité récupérée par son id.
     *
     * @param id l'identifiant de l'entité à trouver
     * @return une entité dont l'id est égal à l'id passé en paramètres
     */
    public Entity getEntityById(int id) {
        return entitiesMap.get(id);
    }

    /**
     * Supprime une entité et ses caractéristiques du projet.
     *
     * @param entity l'entité à supprimer
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
     * Ajoute une caractéristique au projet et la lie à l'entité et l'observation auxquelles elle doit être liée.
     *
     * @param characteristic la caractéristique à ajouter
     */
    public void addCharacteristic(Characteristic characteristic) {
        characteristics.add(characteristic);
        linkCharacteristic(characteristic);
    }

    /**
     * Supprime une caractérisrtique du projet, nécessite de la supprimer aussi des entités et des observations liées.
     *
     * @param characteristic la caractéristique à supprimer
     */
    public void removeCharacteristic(Characteristic characteristic) {
        characteristics.remove(characteristic);
        characteristic.removeFromEntity();
        characteristic.removeFromObservation();
    }

    /**
     * Ajoute une observation au projet en spécifiant son entité et lui définit un id si nécessaire.
     *
     * @param entity      l'entité pour laquelle ajouter l'observation
     * @param observation l'observation à ajouter
     */
    public void addObservation(Entity entity, Observation observation) {
        //si l'observation n'a pas d'id, alors il faut lui en ajouter un
        if (observation.getId() < 0) {
            int newId = 1;
            //on récupère l'id max des observations de l'entité donnée
            Optional<Observation> maxObservation = entity.getObservations().stream().max(Comparator.comparingInt(Observation::getId));
            if (maxObservation.isPresent()) {
                newId = maxObservation.get().getId() + 1;
            }
            observation.setId(newId);
        }
        observation.setEntity(entity);
        //on définit la propriété refObs des caractéristiques seulement après avoir donné un id à l'observation
        for (Characteristic characteristic : observation.getCharacteristics()) {
            characteristic.setRefObs(observation.getId());
        }
    }

    /**
     * Supprime une observation du projet.
     *
     * @param observation l'observation à supprimer
     */
    public void removeObservation(Observation observation) {
        //on libère les caractéristiques qui étaient liées à cette observation
        for (Characteristic characteristic : observation.getCharacteristics()) {
            characteristic.setRefObs(0);
            characteristic.setObservation(null);
        }
        observation.getEntity().removeObservation(observation);
    }

    /**
     * Lie les entités aux autres entités auxquelles elles doivent être liées, les entités de type 'Sample'
     * doivent référencer une autre entité (de type 'SampleUnit' par exemple).
     */
    public void linkEntities() {
        for (Entity entity : entities) {
            if (entity.getRefEntity() > -1) {
                entity.setRefEntityObject(getEntityById(entity.getRefEntity()));
            }
        }
    }

    /**
     * Lie les caractéristiques aux entités et aux observations auxquelles elles doivent être liées.
     */
    public void linkCharacteristics() {
        for (Characteristic characteristic : characteristics) {
            linkCharacteristic(characteristic);
        }
    }

    /**
     * Lie une caractéristique à l'entité et à l'observation à laquelle elle doit être liée.
     *
     * @param characteristic la caractéristique à lier
     */
    public void linkCharacteristic(Characteristic characteristic) {
        Entity refEntity = getEntityById(characteristic.getRefEntity());
        Observation refObs = refEntity.getObservationById(characteristic.getRefObs());
        characteristic.setEntity(refEntity);
        characteristic.setObservation(refObs);
    }

}
