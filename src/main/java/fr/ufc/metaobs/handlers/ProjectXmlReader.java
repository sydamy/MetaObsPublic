package fr.ufc.metaobs.handlers;

import fr.ufc.metaobs.model.*;
import javafx.util.Pair;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe permettant de lire un fichier de projet
 */
public class ProjectXmlReader {

    private IRI baseIri = null;

    public Project open(File fileProject, String baseUri) throws IOException, JDOMException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(fileProject);
        Element root = doc.getRootElement();
        this.baseIri = IRIFactory.iriImplementation().construct(baseUri);
        Project project = readProject(root, baseUri);
        project.linkEntities();
        project.linkCharacteristics();
        return project;
    }


    private Project readProject(Element projectElement, String baseUri) {
        String name = projectElement.getAttributeValue("name");
        String ontology = resolve(projectElement.getAttributeValue("ontology"));
        Project project = new Project(name, ontology, baseUri);
        List<Element> children = projectElement.getChildren();
        for (Element child : children) {
            switch (child.getName()) {
                case "Observatory":
                    project.setObservatory(readObservatory(child));
                    break;
                case "Entities":
                    project.setEntities(readEntitiesList(child));
                    break;
                case "Characteristics":
                    project.setCharacteristics(readCharacteristicsList(child));
                    break;
            }
        }
        return project;
    }

    private Observatory readObservatory(Element observatoryElement) {
        Observatory observatory = new Observatory();
        List<Element> children = observatoryElement.getChildren();
        for (Element infoElement : children) {
            String name = infoElement.getAttributeValue("name");
            String iri = resolve(infoElement.getAttributeValue("IRI"));
            String value = infoElement.getValue();
            observatory.addInfo(name, iri, value);
        }
        return observatory;
    }

    private List<Entity> readEntitiesList(Element entitiesElement) {
        List<Entity> entities = new ArrayList<>();
        List<Element> children = entitiesElement.getChildren();
        for (Element child : children) {
            entities.add(readEntity(child));
        }
        return entities;
    }

    private Entity readEntity(Element entityElement) {
        int id = Integer.parseInt(entityElement.getAttributeValue("id"));
        String name = entityElement.getAttributeValue("name");
        String type = entityElement.getAttributeValue("type");
        String iri = entityElement.getAttributeValue("IRI");
        Entity entity = new Entity(id, name, type, iri);
        if (entityElement.getAttributeValue("refEntity") != null) {
            int refEntity = Integer.parseInt(entityElement.getAttributeValue("refEntity"));
            entity.setRefEntity(refEntity);
        }
        List<Element> children = entityElement.getChildren();
        for (Element child : children) {
            switch (child.getName()) {
                case "Context":
                    Context context = readContext(child);
                    entity.setContext(context);
                    break;
                case "Observation":
                    Observation observation = readObservation(child);
                    observation.setEntity(entity);
                    break;
                case "Referential":
                    Referential referential = readReferential(child);
                    entity.setReferential(referential);
                    break;
            }
        }
        return entity;
    }

    private List<Characteristic> readCharacteristicsList(Element characteristicsElement) {
        List<Characteristic> characteristics = new ArrayList<>();
        List<Element> children = characteristicsElement.getChildren();
        for (Element child : children) {
            characteristics.add(readCharacteristic(child));
        }
        return characteristics;
    }

    private Characteristic readCharacteristic(Element characteristicElement) {
        String name = characteristicElement.getAttributeValue("name");
        int refEntity = Integer.parseInt(characteristicElement.getAttributeValue("refEntity"));
        int refObs = Integer.parseInt(characteristicElement.getAttributeValue("refObs"));
        String iri = resolve(characteristicElement.getAttributeValue("IRI"));
        boolean nullable = Boolean.parseBoolean(characteristicElement.getAttributeValue("nullable"));
        String fieldType = characteristicElement.getAttributeValue("fieldType");
        Characteristic characteristic = new Characteristic(refEntity, refObs, name, iri, nullable, fieldType);
        List<Element> children = characteristicElement.getChildren();
        for (Element child : children) {
            switch (child.getName()) {
                case "Unit":
                    characteristic.setUnit(new Pair<>(resolve(child.getAttributeValue("IRI")), child.getValue()));
                    break;
                case "Classification":
                    characteristic.setClassification(new Pair<>(resolve(child.getAttributeValue("IRI")), child.getValue()));
                    break;
                case "Tool":
                    characteristic.setTool(new Pair<>(resolve(child.getAttributeValue("IRI")), child.getValue()));
                    break;
                case "Protocol":
                    characteristic.setProtocol(child.getValue());
                    break;
            }
        }
        return characteristic;
    }

    private Observation readObservation(Element observationElement) {
        int id = Integer.parseInt(observationElement.getAttributeValue("id"));
        String name = observationElement.getAttributeValue("name");
        String multiplicity = observationElement.getAttributeValue("multiplicity");
        Observation observation = new Observation(id, name, multiplicity);
        String fieldTypeSizeStr = observationElement.getAttributeValue("idExternal");
        if (fieldTypeSizeStr != null && !fieldTypeSizeStr.isBlank()) {
            observation.setExternalId(new FieldTypeSize(fieldTypeSizeStr));
        }
        List<Element> children = observationElement.getChildren();
        if (children.size() == 1) {
            observation.setContext(readContext(children.get(0)));
        }
        return observation;
    }

    private Referential readReferential(Element referentialElement) {
        String repositoryName = referentialElement.getAttributeValue("NameRef");
        String repositoryIri = referentialElement.getAttributeValue("IRIRef");
        String nameInRepository = referentialElement.getAttributeValue("NameInRef");
        String iriInRepository = referentialElement.getAttributeValue("IRIInRef");
        return new Referential(repositoryName, repositoryIri, nameInRepository, iriInRepository);
    }

    private Context readContext(Element contextElement) {
        Element actorElement = null;
        Element locationElement = null;
        Element dateElement = null;
        List<Element> children = contextElement.getChildren();
        for (Element child : children) {
            switch (child.getName()) {
                case "Actor":
                    actorElement = child;
                    break;
                case "Location":
                    locationElement = child;
                    break;
                case "Date":
                    dateElement = child;
                    break;
            }
        }
        ContextActor contextActor = actorElement != null ? readContextActor(actorElement) : new ContextActor("");
        ContextLocation contextLocation = locationElement != null ? readContextLocation(locationElement) : new ContextLocation("");
        ContextDate contextDate = dateElement != null ? readContextDate(dateElement) : new ContextDate("");
        return new Context(contextActor, contextLocation, contextDate);
    }

    private ContextActor readContextActor(Element actorElement) {
        String actorName = actorElement.getAttributeValue("name");
        String typeActor = actorElement.getAttributeValue("type");
        ContextActor contextActor = new ContextActor(actorName);
        contextActor.setTypeActor(typeActor);
        List<Element> children = actorElement.getChildren();
        for (Element child : children) {
            if ("Actor".equals(child.getName())) {
                contextActor.setRefContextActor(readContextActor(child));
            }
            if ("Properties".equals(child.getName())) {
                contextActor.putProperty(
                        child.getAttributeValue("originalName"),
                        child.getAttributeValue("name"),
                        child.getAttributeValue("type")
                );
            }
        }
        return contextActor;
    }

    private ContextLocation readContextLocation(Element locationElement) {
        String locationName = locationElement.getAttributeValue("name");
        ContextLocation contextLocation = new ContextLocation(locationName);
        List<Element> children = locationElement.getChildren();
        for (Element child : children) {
            if ("Location".equals(child.getName())) {
                contextLocation.setRefLocation(readContextLocation(child));
            }
            if ("Properties".equals(child.getName())) {
                contextLocation.putProperty(
                        child.getAttributeValue("originalName"),
                        child.getAttributeValue("name"),
                        child.getAttributeValue("type")
                );
            }
        }
        return contextLocation;
    }

    private ContextDate readContextDate(Element dateElement) {
        List<Element> children = dateElement.getChildren();
        ContextDate contextDate = new ContextDate(dateElement.getAttributeValue("name"));
        for (Element child : children) {
            switch (child.getName()) {
                case "Format":
                    contextDate.setDateFormat(child.getAttributeValue("value"));
                    break;
                case "Standard":
                    contextDate.setDateStandard(child.getAttributeValue("value"));
                    break;
                case "Properties":
                    contextDate.putProperty(
                            child.getAttributeValue("originalName"),
                            child.getAttributeValue("name"),
                            child.getAttributeValue("type")
                    );
            }
        }
        return contextDate;
    }

    private String resolve(String uri) {
        return baseIri.resolve(uri).toString();
    }

}
