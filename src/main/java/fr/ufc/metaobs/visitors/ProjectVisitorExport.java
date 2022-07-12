package fr.ufc.metaobs.visitors;

import fr.ufc.metaobs.model.*;
import fr.ufc.metaobs.model.export.*;

import static fr.ufc.metaobs.utils.SubContextUtils.*;

public class ProjectVisitorExport implements ProjectVisitor<ExportElement<?, ?>> {

    public static final String PRIMARY_KEY_TYPE_SIZE = "INT(11)";

    @Override
    public ExportElement<?, ?> visit(Project project) {
        Database database = new Database();
        visit(database, project.getObservatory());

        Table mdEntities = new Table("MD_Entities");
        mdEntities.setTag("Metadata");
        mdEntities.putColumn(new Column("IdEntity", "TEXT", false));
        mdEntities.putColumn(new Column("NameEntity", "TEXT", false));
        mdEntities.putColumn(new Column("TypeEntity", "TEXT", false));
        mdEntities.putColumn(new Column("OfEntity", "TEXT", true));
        mdEntities.putColumn(new Column("NameRef", "TEXT", false));
        mdEntities.putColumn(new Column("IRIRef", "TEXT", false));
        mdEntities.putColumn(new Column("NameInRef", "TEXT", false));
        mdEntities.putColumn(new Column("IRIInRef", "TEXT", false));
        database.addTable(mdEntities);

        Table mdCharacteristics = new Table("MD_Characteristics");
        mdCharacteristics.setTag("Metadata");
        mdCharacteristics.putColumn(new Column("NameCharacteristic", "TEXT", false));
        mdCharacteristics.putColumn(new Column("IRI", "TEXT", false));
        mdCharacteristics.putColumn(new Column("IdObservation", "TEXT", false));
        mdCharacteristics.putColumn(new Column("IdEntity", "TEXT", false));
        mdCharacteristics.putColumn(new Column("Unit", "TEXT", true));
        mdCharacteristics.putColumn(new Column("Classification", "TEXT", true));
        mdCharacteristics.putColumn(new Column("Protocol", "TEXT", true));
        mdCharacteristics.putColumn(new Column("Tool", "TEXT", true));
        database.addTable(mdCharacteristics);

        Table mdContexts = new Table("MD_Contexts");
        mdContexts.setTag("Metadata");
        mdContexts.putColumn(new Column("IdObservation", "TEXT", true));
        mdContexts.putColumn(new Column("IdEntity", "TEXT", true));
        mdContexts.putColumn(new Column("Actor", "TEXT", false));
        mdContexts.putColumn(new Column("ActorFormat", "TEXT", false));
        mdContexts.putColumn(new Column("Date", "TEXT", false));
        mdContexts.putColumn(new Column("DateFormat", "TEXT", false));
        mdContexts.putColumn(new Column("DateStandard", "TEXT", true));
        mdContexts.putColumn(new Column("Location", "TEXT", true));
        mdContexts.putColumn(new Column("LocationFormat", "TEXT", true));
        database.addTable(mdContexts);

        int idCharacteristic = 0;
        int idContext = 0;
        for (Entity entity : project.getEntities()) {
            int id = entity.getId();
            mdEntities.putValue(id, "IdEntity", id);
            mdEntities.putValue(id, "NameEntity", entity.getName());
            mdEntities.putValue(id, "TypeEntity", entity.getType());
            String ofEntity = entity.getRefEntity() > 0 ? "" + entity.getRefEntity() : "";
            mdEntities.putValue(id, "OfEntity", ofEntity);
            String repoName = entity.getReferential() != null ? entity.getReferential().getRepositoryName() : "";
            String repoIri = entity.getReferential() != null ? entity.getReferential().getRepositoryIri() : "";
            String nameInRepo = entity.getReferential() != null ? entity.getReferential().getNameInRepository() : "";
            String iriInRepo = entity.getReferential() != null ? entity.getReferential().getIriInRepository() : "";
            mdEntities.putValue(id, "NameRef", repoName);
            mdEntities.putValue(id, "IRIRef", repoIri);
            mdEntities.putValue(id, "NameInRef", nameInRepo);
            mdEntities.putValue(id, "IRIInRef", iriInRepo);

            visit(database, entity);
            for (Characteristic characteristic : entity.getCharacteristics()) {
                String unitValue = characteristic.getUnit() != null ? characteristic.getUnit().getValue() : "";
                String classificationValue = characteristic.getClassification() != null ? characteristic.getClassification().getValue() : "";
                String toolValue = characteristic.getTool() != null ? characteristic.getTool().getValue() : "";
                String protocol = characteristic.getProtocol() != null ? characteristic.getProtocol() : "";
                mdCharacteristics.putValue(idCharacteristic, "NameCharacteristic", characteristic.getName());
                mdCharacteristics.putValue(idCharacteristic, "IRI", characteristic.getIri());
                mdCharacteristics.putValue(idCharacteristic, "IdObservation", characteristic.getRefObs());
                mdCharacteristics.putValue(idCharacteristic, "IdEntity", characteristic.getRefEntity());
                mdCharacteristics.putValue(idCharacteristic, "Unit", unitValue);
                mdCharacteristics.putValue(idCharacteristic, "Classification", classificationValue);
                mdCharacteristics.putValue(idCharacteristic, "Protocol", protocol);
                mdCharacteristics.putValue(idCharacteristic, "Tool", toolValue);
                idCharacteristic++;
            }
            if (entity.getContext() != null) {
                mdContexts.putValue(idContext, "IdObservation", "");
                mdContexts.putValue(idContext, "IdEntity", entity.getId());
                String actorName = "";
                String actorFormat = "";
                String dateName = "";
                String dateFormat = "";
                String dateStandard = "";
                String locationName = "";
                String locationFormat = "";
                if (entity.getContext().getContextActor() != null && !entity.getContext().getContextActor().getName().isBlank()) {
                    actorName = getActorName(entity.getContext().getContextActor(), ";");
                    actorFormat = getActorPropertiesNames(entity.getContext().getContextActor(), ";");
                }
                if (entity.getContext().getContextDate() != null) {
                    dateName = entity.getContext().getContextDate().getName();
                    dateFormat = entity.getContext().getContextDate().getDateFormat();
                    dateStandard = entity.getContext().getContextDate().getDateStandard();
                }
                if (entity.getContext().getContextLocation() != null && !entity.getContext().getContextLocation().getName().isBlank()) {
                    locationName = getLocationName(entity.getContext().getContextLocation(), ";");
                    locationFormat = getLocationPropertiesNames(entity.getContext().getContextLocation(), ";");
                }
                mdContexts.putValue(idContext, "Actor", actorName);
                mdContexts.putValue(idContext, "ActorFormat", actorFormat);
                mdContexts.putValue(idContext, "Date", dateName);
                mdContexts.putValue(idContext, "DateFormat", dateFormat);
                mdContexts.putValue(idContext, "DateStandard", dateStandard);
                mdContexts.putValue(idContext, "Location", locationName);
                mdContexts.putValue(idContext, "LocationFormat", locationFormat);
                idContext++;
            }
            for (Observation observation : entity.getObservations()) {
                if (observation.getContext() != null) {
                    mdContexts.putValue(idContext, "IdObservation", observation.getId());
                    mdContexts.putValue(idContext, "IdEntity", entity.getId());
                    String actorName = "";
                    String actorFormat = "";
                    String dateName = "";
                    String dateFormat = "";
                    String dateStandard = "";
                    String locationName = "";
                    String locationFormat = "";
                    if (observation.getContext().getContextActor() != null && !observation.getContext().getContextActor().getName().isBlank()) {
                        actorName = getActorName(observation.getContext().getContextActor(), ";");
                        actorFormat = getActorPropertiesNames(observation.getContext().getContextActor(), ";");
                    }
                    if (observation.getContext().getContextDate() != null) {
                        dateName = observation.getContext().getContextDate().getName();
                        dateFormat = observation.getContext().getContextDate().getDateFormat();
                        dateStandard = observation.getContext().getContextDate().getDateStandard();
                    }
                    if (observation.getContext().getContextLocation() != null && !observation.getContext().getContextLocation().getName().isBlank()) {
                        locationName = getLocationName(observation.getContext().getContextLocation(), ";");
                        locationFormat = getLocationPropertiesNames(observation.getContext().getContextLocation(), ";");
                    }
                    mdContexts.putValue(idContext, "Actor", actorName);
                    mdContexts.putValue(idContext, "ActorFormat", actorFormat);
                    mdContexts.putValue(idContext, "Date", dateName);
                    mdContexts.putValue(idContext, "DateFormat", dateFormat);
                    mdContexts.putValue(idContext, "DateStandard", dateStandard);
                    mdContexts.putValue(idContext, "Location", locationName);
                    mdContexts.putValue(idContext, "LocationFormat", locationFormat);
                    idContext++;
                }
            }
        }

        return database;
    }

    @Override
    public void visit(ExportElement<?, ?> parent, Observatory observatory) {
        Table table = new Table("MD_General");
        table.setTag("Metadata");
        for (MetadataInfo info : observatory.getListInfos()) {
            Column column = new Column(info.getName(), "TEXT", false);
            table.putColumn(column);
            table.putValue(0, info.getName(), info.getValue());
        }
        ((Database) parent).addTable(table);
    }

    @Override
    public void visit(ExportElement<?, ?> parent, Entity entity) {
        visit(parent, entity.getContext());

        Table tableEntity = new Table(entity.getName());
        tableEntity.setTag("Entity");
        PrimaryKey primaryKey = new PrimaryKey(entity.getName(), "id" + entity.getName(), PRIMARY_KEY_TYPE_SIZE, false, true);
        tableEntity.putColumn(primaryKey);

        if (entity.getRefEntity() >= 0 && entity.getRefEntityObject() != null) {
            String name = entity.getRefEntityObject().getName();
            PrimaryKey refPrimaryKey = visitRefEntity(entity.getRefEntityObject());
            ForeignKey foreignKey = new ForeignKey(tableEntity, "id" + name, PRIMARY_KEY_TYPE_SIZE, false, refPrimaryKey);
            tableEntity.putColumn(foreignKey);
        }

        if (entity.getExternalId() != null) {
            tableEntity.putColumn(new Column("idExternal", entity.getExternalId().toString(), false));
        }

        if (entity.getContext() != null) {
            if (entity.getContext().getContextActor() != null && !entity.getContext().getContextActor().getName().isBlank()) {
                tableEntity.putColumn(visitContextActor(tableEntity, entity.getContext().getContextActor()));
            }
            if (entity.getContext().getContextLocation() != null && !entity.getContext().getContextLocation().getName().isBlank()) {
                tableEntity.putColumn(visitContextLocation(tableEntity, entity.getContext().getContextLocation()));
            }
            if (entity.getContext().getContextDate() != null) {
                visit(tableEntity, entity.getContext().getContextDate());
            }
        }

        for (Observation observation : entity.getObservations()) {
            if ("1".equals(observation.getMultiplicity())) {
                if (observation.getContext() != null) {
                    visit(parent, observation);
                    visitObservationEntity(tableEntity, observation);
                } else {
                    visit(tableEntity, observation);
                }
            } else {
                visit(parent, observation);
            }
        }

        ((Database) parent).addTable(tableEntity);
    }

    private PrimaryKey visitRefEntity(Entity entity) {
        return new PrimaryKey(entity.getName(), "id" + entity.getName(), PRIMARY_KEY_TYPE_SIZE, false, true);
    }

    @Override
    public void visit(ExportElement<?, ?> parent, Referential referential) {

    }

    @Override
    public void visit(ExportElement<?, ?> parent, Characteristic characteristic) {
        String typeSize = characteristic.getFieldSize() == null ?
                characteristic.getFieldType() :
                characteristic.getFieldType() + "(" + characteristic.getFieldSize() + ")";
        Column column = new Column(characteristic.getName(), typeSize, characteristic.isNullable());
        ((Table) parent).putColumn(column);
    }

    @Override
    public void visit(ExportElement<?, ?> parent, Observation observation) {
        if (observation.getContext() != null) {
            if ("1".equals(observation.getMultiplicity())) {
                visit(parent, observation.getContext());
            } else {
                Table tableObs = new Table(observation.getName());
                tableObs.setTag("Observation");
                PrimaryKey primaryKey = new PrimaryKey(observation.getName(), "id" + observation.getName(), PRIMARY_KEY_TYPE_SIZE, false, true);
                tableObs.putColumn(primaryKey);
                ForeignKey foreignKey = new ForeignKey(tableObs, "id" + observation.getEntity().getName(), PRIMARY_KEY_TYPE_SIZE, false,
                        visitRefEntity(observation.getEntity()));
                tableObs.putColumn(foreignKey);
                for (Characteristic characteristic : observation.getCharacteristics()) {
                    visit(tableObs, characteristic);
                }
                if (observation.getExternalId() != null) {
                    tableObs.putColumn(new Column("idExternal", observation.getExternalId().toString(), false));
                }
                visit(parent, observation.getContext());
                if (observation.getContext().getContextActor() != null && !observation.getContext().getContextActor().getName().isEmpty()) {
                    tableObs.putColumn(visitContextActor(tableObs, observation.getContext().getContextActor()));
                }
                if (observation.getContext().getContextLocation() != null && !observation.getContext().getContextLocation().getName().isEmpty()) {
                    tableObs.putColumn(visitContextLocation(tableObs, observation.getContext().getContextLocation()));
                }
                if (observation.getContext().getContextDate() != null) {
                    visit(tableObs, observation.getContext().getContextDate());
                }
                ((Database) parent).addTable(tableObs);
            }
        } else {
            for (Characteristic characteristic : observation.getCharacteristics()) {
                visit(parent, characteristic);
            }
        }
    }

    private void visitObservationEntity(Table tableEntity, Observation observation) {
        for (Characteristic characteristic : observation.getCharacteristics()) {
            visit(tableEntity, characteristic);
        }
        if (observation.getContext().getContextActor() != null && !observation.getContext().getContextActor().getName().isEmpty()) {
            tableEntity.putColumn(visitContextActor(tableEntity, observation.getContext().getContextActor()));
        }
        if (observation.getContext().getContextLocation() != null && !observation.getContext().getContextLocation().getName().isEmpty()) {
            tableEntity.putColumn(visitContextLocation(tableEntity, observation.getContext().getContextLocation()));
        }
        visit(tableEntity, observation.getContext().getContextDate());

    }

    @Override
    public void visit(ExportElement<?, ?> parent, Context context) {
        if (context != null) {
            if (context.getContextActor() != null && !context.getContextActor().getName().isEmpty()) {
                visit(parent, context.getContextActor());
            }
            if (context.getContextLocation() != null && !context.getContextLocation().getName().isEmpty()) {
                visit(parent, context.getContextLocation());
            }
        }
    }

    private ForeignKey visitContextActor(Table table, ContextActor contextActor) {
        return new ForeignKey(table, "id" + contextActor.getName(), PRIMARY_KEY_TYPE_SIZE, false,
                new PrimaryKey(contextActor.getName(), "id" + contextActor.getName(), PRIMARY_KEY_TYPE_SIZE, false, true));
    }

    @Override
    public void visit(ExportElement<?, ?> parent, ContextActor contextActor) {
        Table table = new Table(contextActor.getName());
        table.setTag("ContextActor");
        PrimaryKey primaryKey = new PrimaryKey(contextActor.getName(), "id" + contextActor.getName(), PRIMARY_KEY_TYPE_SIZE, false, true);
        table.putColumn(primaryKey);
        if (contextActor.getRefContextActor() != null) {
            visit(parent, contextActor.getRefContextActor());
            table.putColumn(visitContextActor(table, contextActor.getRefContextActor()));
        }
        for (Field field : contextActor.getPropertiesList()) {
            Column column = new Column(field.getName(), field.getTypeSize().toString(), false);
            table.putColumn(column);
        }
        ((Database) parent).addTable(table);
    }

    private ForeignKey visitContextLocation(Table table, ContextLocation contextLocation) {
        return new ForeignKey(table, "id" + contextLocation.getName(), PRIMARY_KEY_TYPE_SIZE, false,
                new PrimaryKey(contextLocation.getName(), "id" + contextLocation.getName(), PRIMARY_KEY_TYPE_SIZE, false, true));
    }

    @Override
    public void visit(ExportElement<?, ?> parent, ContextLocation contextLocation) {
        Table table = new Table(contextLocation.getName());
        table.setTag("ContextLocation");
        PrimaryKey primaryKey = new PrimaryKey(contextLocation.getName(), "id" + contextLocation.getName(), PRIMARY_KEY_TYPE_SIZE, false, true);
        table.putColumn(primaryKey);
        if (contextLocation.getRefLocation() != null) {
            visit(parent, contextLocation.getRefLocation());
            table.putColumn(visitContextLocation(table, contextLocation.getRefLocation()));
        }
        for (Field field : contextLocation.getPropertiesList()) {
            Column column = new Column(field.getName(), field.getTypeSize().toString(), false);
            table.putColumn(column);
        }
        ((Database) parent).addTable(table);
    }

    @Override
    public void visit(ExportElement<?, ?> parent, ContextDate contextDate) {
        for (Field field : contextDate.getPropertiesList()) {
            Column column = new Column(field.getName(), field.getTypeSize().toString(), false);
            ((Table) parent).putColumn(column);
        }
    }
}
