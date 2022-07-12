package fr.ufc.metaobs.visitors;

import fr.ufc.metaobs.model.*;
import javafx.util.Pair;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.jdom2.Element;

import java.util.List;

public class ProjectVisitorXml implements ProjectVisitor<Element> {

    private IRI baseUri = null;

    @Override
    public Element visit(Project project) {
        baseUri = IRIFactory.iriImplementation().construct(project.getBaseUri());
        Element projectElement = new Element("Project");
        projectElement.setAttribute("name", project.getName());
        projectElement.setAttribute("ontology", relativize(project.getOntology()));
        visit(projectElement, project.getObservatory());
        Element entitiesElement = new Element("Entities");
        projectElement.addContent(entitiesElement);
        for (Entity entity : project.getEntities()) {
            visit(entitiesElement, entity);
        }
        Element characteristicsElement = new Element("Characteristics");
        projectElement.addContent(characteristicsElement);
        for (Characteristic characteristic : project.getCharacteristics()) {
            visit(characteristicsElement, characteristic);
        }
        return projectElement;
    }

    @Override
    public void visit(Element parent, Observatory observatory) {
        Element observatoryElement = new Element("Observatory");
        for (MetadataInfo info : observatory.getListInfos()) {
            Element infoElement = new Element("info");
            infoElement.setAttribute("name", info.getName());
            infoElement.setAttribute("IRI", relativize(info.getIri()));
            infoElement.setText(info.getValue());
            observatoryElement.addContent(infoElement);
        }
        parent.addContent(observatoryElement);
    }

    @Override
    public void visit(Element parent, Entity entity) {
        Element entityElement = new Element("Entity");
        entityElement.setAttribute("id", String.valueOf(entity.getId()));
        entityElement.setAttribute("name", entity.getName());
        entityElement.setAttribute("type", entity.getType());
        entityElement.setAttribute("IRI", entity.getIri());
        if (entity.getRefEntity() > 0) {
            entityElement.setAttribute("refEntity", String.valueOf(entity.getRefEntity()));
        }
        if (entity.getReferential() != null) {
            visit(entityElement, entity.getReferential());
        }
        if (entity.getExternalId() != null) {
            entityElement.setAttribute("idExternalType", entity.getExternalId().toString());
        }
        visit(entityElement, entity.getContext());
        for (Observation observation : entity.getObservations()) {
            visit(entityElement, observation);
        }
        parent.addContent(entityElement);
    }

    @Override
    public void visit(Element parent, Referential referential) {
        if (!referential.getRepositoryName().isBlank() || !referential.getRepositoryIri().isBlank()
                || !referential.getNameInRepository().isBlank() || !referential.getIriInRepository().isBlank()) {
            Element referentialElement = new Element("Referential");
            referentialElement.setAttribute("NameRef", referential.getRepositoryName());
            referentialElement.setAttribute("IRIRef", referential.getRepositoryIri());
            referentialElement.setAttribute("NameInRef", referential.getNameInRepository());
            referentialElement.setAttribute("IRIInRef", referential.getIriInRepository());
            parent.addContent(referentialElement);
        }
    }

    @Override
    public void visit(Element parent, Characteristic characteristic) {
        Element characteristicElement = new Element("Characteristic");
        characteristicElement.setAttribute("name", characteristic.getName());
        characteristicElement.setAttribute("refEntity", String.valueOf(characteristic.getRefEntity()));
        characteristicElement.setAttribute("refObs", String.valueOf(characteristic.getRefObs()));
        characteristicElement.setAttribute("IRI", relativize(characteristic.getIri()));
        characteristicElement.setAttribute("nullable", String.valueOf(characteristic.isNullable()));
        String fieldType = characteristic.getFieldType();
        if (characteristic.getFieldSize() != null) {
            fieldType = fieldType + "(" + characteristic.getFieldSize() + ")";
        }
        characteristicElement.setAttribute("fieldType", fieldType);
        if (characteristic.getUnit() != null) {
            addCharacteristicChild(characteristicElement, "Unit", characteristic.getUnit());
        }
        if (characteristic.getClassification() != null) {
            addCharacteristicChild(characteristicElement, "Classification", characteristic.getClassification());
        }
        Element protocolElement = new Element("Protocol");
        protocolElement.setText(characteristic.getProtocol());
        characteristicElement.addContent(protocolElement);
        if (characteristic.getTool() != null) {
            addCharacteristicChild(characteristicElement, "Tool", characteristic.getTool());
        }
        parent.addContent(characteristicElement);
    }

    private void addCharacteristicChild(Element parent, String childName, Pair<String, String> pair) {
        Element childElement = new Element(childName);
        childElement.setAttribute("IRI", relativize(pair.getKey()));
        childElement.setText(pair.getValue());
        parent.addContent(childElement);
    }

    @Override
    public void visit(Element parent, Observation observation) {
        Element observationElement = new Element("Observation");
        observationElement.setAttribute("id", String.valueOf(observation.getId()));
        observationElement.setAttribute("name", observation.getName());
        observationElement.setAttribute("multiplicity", observation.getMultiplicity());
        if (observation.getExternalId() != null) {
            observationElement.setAttribute("idExternal", observation.getExternalId().toString());
        }
        if (observation.getContext() != null) {
            visit(observationElement, observation.getContext());
        }
        parent.addContent(observationElement);
    }

    @Override
    public void visit(Element parent, Context context) {
        Element contextElement = new Element("Context");
        if (context.getContextActor() != null && !context.getContextActor().getName().isEmpty()) {
            visit(contextElement, context.getContextActor());
        }
        if (context.getContextLocation() != null && !context.getContextLocation().getName().isEmpty()) {
            visit(contextElement, context.getContextLocation());
        }
        if (context.getContextDate() != null && !context.getContextDate().getName().isEmpty()) {
            visit(contextElement, context.getContextDate());
        }
        parent.addContent(contextElement);
    }

    @Override
    public void visit(Element parent, ContextActor contextActor) {
        Element actorElement = new Element("Actor");
        if (contextActor.getRefContextActor() != null) {
            visit(actorElement, contextActor.getRefContextActor());
        }
        actorElement.setAttribute("name", contextActor.getName());
        actorElement.setAttribute("type", contextActor.getTypeActor());
        addPropertiesFromFieldList(actorElement, contextActor.getPropertiesList());
        parent.addContent(actorElement);
    }

    @Override
    public void visit(Element parent, ContextLocation contextLocation) {
        Element locationElement = new Element("Location");
        if (contextLocation.getRefLocation() != null) {
            visit(locationElement, contextLocation.getRefLocation());
        }
        locationElement.setAttribute("name", contextLocation.getName());
        addPropertiesFromFieldList(locationElement, contextLocation.getPropertiesList());
        parent.addContent(locationElement);
    }

    private void addPropertiesFromFieldList(Element parent, List<Field> fields) {
        for (Field field : fields) {
            Element propertyElement = new Element("Properties");
            propertyElement.setAttribute("originalName", field.getOriginalName());
            propertyElement.setAttribute("name", field.getNewName());
            propertyElement.setAttribute("type", field.getTypeSize().toString());
            parent.addContent(propertyElement);
        }
    }

    @Override
    public void visit(Element parent, ContextDate contextDate) {
        Element dateElement = new Element("Date");
        dateElement.setAttribute("name", contextDate.getName());
        if (contextDate.getDateFormat() != null && !contextDate.getDateFormat().isEmpty()) {
            Element formatElement = new Element("Format");
            formatElement.setAttribute("value", contextDate.getDateFormat());
            dateElement.addContent(formatElement);
        }
        if (contextDate.getDateStandard() != null && !contextDate.getDateStandard().isEmpty()) {
            Element standardElement = new Element("Standard");
            standardElement.setAttribute("value", contextDate.getDateStandard());
            dateElement.addContent(standardElement);
        }
        addPropertiesFromFieldList(dateElement, contextDate.getPropertiesList());
        parent.addContent(dateElement);
    }

    private String relativize(String uri) {
        return baseUri.relativize(uri).toString();
    }

}
