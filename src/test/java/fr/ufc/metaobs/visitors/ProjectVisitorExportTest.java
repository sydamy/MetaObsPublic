package fr.ufc.metaobs.visitors;

import fr.ufc.metaobs.model.*;
import fr.ufc.metaobs.model.export.Database;
import fr.ufc.metaobs.model.export.ExportElement;
import fr.ufc.metaobs.model.export.ForeignKey;
import fr.ufc.metaobs.model.export.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectVisitorExportTest {

    private static Project project;
    private static ProjectVisitorExport visitor;

    @BeforeEach
    void setUp() {
        project = new Project("Test", "test", "");
        visitor = new ProjectVisitorExport();
    }

    @Test
    void testVisitMetadata() {
        MetadataInfo info = new MetadataInfo("Title", "metaobs_core.owl#Title", "TestTitle");
        project.getObservatory().addInfo(info);
        ExportElement<?, ?> res = visitor.visit(project);
        assertEquals(4, ((Database) res).getTables().size());
        assertNotNull(((Database) res).getTable("MD_General"));
        assertEquals(1, ((Database) res).getTable("MD_General").getColumns().size());
        assertEquals("Title", ((Database) res).getTable("MD_General").getColumn("Title").getName());
        assertEquals("TestTitle", ((Database) res).getTable("MD_General").getValues().get(0).get("Title"));

        project.getObservatory().addInfo("Date", "metaobs_core.owl#DateCeation", "2021-03-25");
        res = visitor.visit(project);
        assertEquals(4, ((Database) res).getTables().size());
        assertNotNull(((Database) res).getTable("MD_General"));
        assertEquals(2, ((Database) res).getTable("MD_General").getColumns().size());
        assertEquals("Date", ((Database) res).getTable("MD_General").getColumn("Date").getName());
        assertEquals("2021-03-25", ((Database) res).getTable("MD_General").getValues().get(0).get("Date"));
    }

    @Test
    void testVisitCharacteristic() {
        Entity ours = new Entity(1, "Ours", "SampleUnit", "#Ours");
        Referential referential = new Referential("RepoName", "RepoIRI", "Ours", "#Ours");
        ours.setReferential(referential);
        project.addEntity(ours);
        Characteristic poids = new Characteristic(1, 0, "Poids", "#Weight", false, "FLOAT(10)");
        poids.setFieldTypeSize("FLOAT(10)");
        poids.setEntity(ours);

        project.addEntity(ours);
        project.addCharacteristic(poids);

        Table oursTable = new Table("Ours");
        visitor.visit(oursTable, poids);
        assertEquals(1, oursTable.getColumns().size());
        assertNotNull(oursTable.getColumn("Poids"));
        assertEquals("FLOAT(10)", oursTable.getColumn("Poids").getType());
    }

    @Test
    void testVisitEntity() {
        Entity ours = new Entity(1, "Ours", "SampleUnit", "#Ours");
        Referential referential = new Referential("RepoName", "RepoIRI", "Ours", "#Ours");
        ours.setReferential(referential);
        project.addEntity(ours);
        Database database = new Database();
        visitor.visit(database, ours);
        assertEquals(0, database.getForeignKeys().size());
        assertEquals(1, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertEquals(1, database.getTable("Ours").getColumns().size());
        assertNotNull(database.getTable("Ours").getColumn("idOurs"));

        database = new Database();
        ours.setExternalId(new FieldTypeSize("INT"));
        visitor.visit(database, ours);
        assertEquals(0, database.getForeignKeys().size());
        assertEquals(1, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertEquals(2, database.getTable("Ours").getColumns().size());
        assertNotNull(database.getTable("Ours").getColumn("idOurs"));
        assertNotNull(database.getTable("Ours").getColumn("idExternal"));
        assertEquals("INT", database.getTable("Ours").getColumn("idExternal").getType());

        Entity sang = new Entity(2, "Sang", "Sample", "#Sang");
        Referential sangReferential = new Referential("RepoName", "RepoIRI", "Sang", "#Sang");
        sang.setReferential(sangReferential);
        sang.setRefEntity(1);
        sang.setRefEntityObject(ours);
        project.addEntity(sang);

        database = new Database();
        visitor.visit(database, ours);
        visitor.visit(database, sang);
        assertEquals(1, database.getForeignKeys().size());
        assertEquals(2, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertNotNull(database.getTable("Sang"));
        assertEquals(2, database.getTable("Ours").getColumns().size());
        assertNotNull(database.getTable("Ours").getColumn("idOurs"));
        assertNotNull(database.getTable("Ours").getColumn("idExternal"));
        assertEquals("INT", database.getTable("Ours").getColumn("idExternal").getType());
        assertEquals(2, database.getTable("Sang").getColumns().size());
        assertNotNull(database.getTable("Sang").getColumn("idSang"));
        assertNotNull(database.getTable("Sang").getColumn("idOurs"));
        assertTrue(database.getTable("Sang").getColumn("idOurs") instanceof ForeignKey);
        assertEquals("INT(11)", database.getTable("Sang").getColumn("idOurs").getType());

        Observation testSang = new Observation(1, "TestSang", "1");
        Characteristic ph = new Characteristic(2, 1, "PH", "#PH", false, "FLOAT");
        testSang.setEntity(sang);
        project.addCharacteristic(ph);

        database = new Database();
        visitor.visit(database, ours);
        visitor.visit(database, sang);
        assertEquals(1, database.getForeignKeys().size());
        assertEquals(2, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertNotNull(database.getTable("Sang"));
        assertEquals(2, database.getTable("Ours").getColumns().size());
        assertNotNull(database.getTable("Ours").getColumn("idOurs"));
        assertNotNull(database.getTable("Ours").getColumn("idExternal"));
        assertEquals("INT", database.getTable("Ours").getColumn("idExternal").getType());
        assertEquals(3, database.getTable("Sang").getColumns().size());
        assertNotNull(database.getTable("Sang").getColumn("idSang"));
        assertNotNull(database.getTable("Sang").getColumn("idOurs"));
        assertNotNull(database.getTable("Sang").getColumn("PH"));
        assertTrue(database.getTable("Sang").getColumn("idOurs") instanceof ForeignKey);
        assertEquals("INT(11)", database.getTable("Sang").getColumn("idOurs").getType());
        assertEquals("FLOAT", database.getTable("Sang").getColumn("PH").getType());
    }

    @Test
    void testVisitObservation() {
        Entity ours = new Entity(1, "Ours", "SampleUnit", "#Ours");
        Referential referential = new Referential("RepoName", "RepoIRI", "Ours", "#Ours");
        ours.setReferential(referential);
        project.addEntity(ours);

        Observation peser = new Observation(1, "Peser", "1");
        peser.setEntity(ours);
        ContextActor peserContextActor = new ContextActor("PeserCollectActor");
        ContextLocation peserContextLocation = new ContextLocation("");
        ContextDate peserContextDate = new ContextDate("PeserCollectDate");
        peserContextDate.setDateFormat("DayMonthYear");
        peserContextDate.setDateStandard("ISO");
        peserContextDate.putProperty("PeserCollectDate", "PeserCollectDate", "DATETIME");
        Context peserContext = new Context(peserContextActor, peserContextLocation, peserContextDate);
        peser.setContext(peserContext);
        Characteristic poids = new Characteristic(ours.getId(), peser.getId(), "Poids", "#Weight", true, "FLOAT");
        peser.addCharacteristic(poids);
        poids.setEntity(ours);

        Database database = new Database();
        visitor.visit(database, ours);
        assertEquals(2, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertNotNull(database.getTable("PeserCollectActor"));
        assertEquals(4, database.getTable("Ours").getColumns().size());

        peserContextLocation.setName("PeserCollectLocation");

        database = new Database();
        visitor.visit(database, ours);
        assertEquals(3, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertEquals(5, database.getTable("Ours").getColumns().size());
        assertNotNull(database.getTable("Ours").getColumn("idOurs"));
        assertNotNull(database.getTable("Ours").getColumn("Poids"));
        assertNotNull(database.getTable("Ours").getColumn("idPeserCollectActor"));
        assertNotNull(database.getTable("Ours").getColumn("idPeserCollectLocation"));
        assertNotNull(database.getTable("Ours").getColumn("PeserCollectDate"));
        assertNotNull(database.getTable("PeserCollectActor"));
        assertNotNull(database.getTable("PeserCollectLocation"));
        assertEquals(1, database.getTable("PeserCollectActor").getColumns().size());
        assertEquals(1, database.getTable("PeserCollectLocation").getColumns().size());

        peser.setMultiplicity("1");
        peser.setContext(null);

        database = new Database();
        visitor.visit(database, ours);
        assertEquals(1, database.getTables().size());
        assertNotNull(database.getTable("Ours"));

        peser.setMultiplicity("n");
        peser.setContext(peserContext);
        database = new Database();
        visitor.visit(database, ours);
        assertEquals(4, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertNotNull(database.getTable("Peser"));
        assertNotNull(database.getTable("PeserCollectActor"));
        assertNotNull(database.getTable("PeserCollectLocation"));
        assertEquals(6, database.getTable("Peser").getColumns().size());
        assertNotNull(database.getTable("Peser").getColumn("idPeser"));
        assertNotNull(database.getTable("Peser").getColumn("idOurs"));
        assertNotNull(database.getTable("Peser").getColumn("Poids"));
        assertNotNull(database.getTable("Peser").getColumn("idPeserCollectActor"));
        assertNotNull(database.getTable("Peser").getColumn("idPeserCollectLocation"));
        assertNotNull(database.getTable("Peser").getColumn("PeserCollectDate"));

        peserContextLocation.putProperty("City", "City", "POLYGON");

        database = new Database();
        visitor.visit(database, ours);
        assertEquals(4, database.getTables().size());
        assertNotNull(database.getTable("Ours"));
        assertNotNull(database.getTable("Peser"));
        assertNotNull(database.getTable("PeserCollectActor"));
        assertNotNull(database.getTable("PeserCollectLocation"));
        assertEquals(6, database.getTable("Peser").getColumns().size());
        assertNotNull(database.getTable("Peser").getColumn("idPeser"));
        assertNotNull(database.getTable("Peser").getColumn("idOurs"));
        assertNotNull(database.getTable("Peser").getColumn("Poids"));
        assertNotNull(database.getTable("Peser").getColumn("idPeserCollectActor"));
        assertNotNull(database.getTable("Peser").getColumn("idPeserCollectLocation"));
        assertNotNull(database.getTable("Peser").getColumn("PeserCollectDate"));
        assertEquals(2, database.getTable("PeserCollectLocation").getColumns().size());
        assertNotNull(database.getTable("PeserCollectLocation").getColumn("idPeserCollectLocation"));
        assertNotNull(database.getTable("PeserCollectLocation").getColumn("City"));

        ExportElement<?, ?> res = visitor.visit(project);
        assertTrue(res instanceof Database);
        assertEquals(8, ((Database) res).getTables().size());
        assertNotNull(((Database) res).getTable("MD_General"));
        assertNotNull(((Database) res).getTable("MD_Entities"));
        assertNotNull(((Database) res).getTable("MD_Characteristics"));
        assertNotNull(((Database) res).getTable("MD_Contexts"));
        assertNotNull(((Database) res).getTable("Ours"));
        assertNotNull(((Database) res).getTable("Peser"));
        assertNotNull(((Database) res).getTable("PeserCollectActor"));
        assertNotNull(((Database) res).getTable("PeserCollectLocation"));
        assertEquals(2, ((Database) res).getTable("PeserCollectLocation").getColumns().size());
        assertNotNull(database.getTable("PeserCollectLocation").getColumn("idPeserCollectLocation"));
        assertNotNull(database.getTable("PeserCollectLocation").getColumn("City"));
        assertEquals(1, ((Database) res).getTable("MD_Contexts").getValues().size());
        assertNotNull(((Database) res).getTable("MD_Contexts").getValues().get(0));
        assertEquals(1, ((Database) res).getTable("MD_Contexts").getValues().get(0).get("IdObservation"));
        assertEquals(1, ((Database) res).getTable("MD_Contexts").getValues().get(0).get("IdEntity"));
        assertEquals("PeserCollectActor", ((Database) res).getTable("MD_Contexts").getValues().get(0).get("Actor"));
        assertEquals("", ((Database) res).getTable("MD_Contexts").getValues().get(0).get("ActorFormat"));
        assertEquals("PeserCollectDate", ((Database) res).getTable("MD_Contexts").getValues().get(0).get("Date"));
        assertEquals("DayMonthYear", ((Database) res).getTable("MD_Contexts").getValues().get(0).get("DateFormat"));
        assertEquals("ISO", ((Database) res).getTable("MD_Contexts").getValues().get(0).get("DateStandard"));
        assertEquals("PeserCollectLocation", ((Database) res).getTable("MD_Contexts").getValues().get(0).get("Location"));
        assertEquals("City", ((Database) res).getTable("MD_Contexts").getValues().get(0).get("LocationFormat"));
    }

    @Test
    void testVisit() {
        ExportElement<?, ?> res = visitor.visit(project);
        assertTrue(res instanceof Database);
        assertEquals(4, ((Database) res).getTables().size());
        assertNotNull(((Database) res).getTable("MD_General"));
        assertNotNull(((Database) res).getTable("MD_Entities"));
        assertNotNull(((Database) res).getTable("MD_Characteristics"));
        assertNotNull(((Database) res).getTable("MD_Contexts"));
        assertNull(((Database) res).getTable("Ours"));

        Entity ours = new Entity(1, "Ours", "SampleUnit", "#Ours");
        Referential referential = new Referential("RepoName", "RepoIRI", "Ours", "#Ours");
        ours.setReferential(referential);
        project.addEntity(ours);

        res = visitor.visit(project);
        assertEquals(5, ((Database) res).getTables().size());
        assertEquals(0, ((Database) res).getForeignKeys().size());
        assertEquals("Ours", ((Database) res).getTable("Ours").getName());
        assertEquals("Ours", ((Database) res).getTable("MD_Entities").getValues().get(1).get("NameEntity"));
        assertEquals("", ((Database) res).getTable("MD_Entities").getValues().get(1).get("OfEntity"));
        assertEquals("SampleUnit", ((Database) res).getTable("MD_Entities").getValues().get(1).get("TypeEntity"));
        assertEquals("#Ours", ((Database) res).getTable("MD_Entities").getValues().get(1).get("IRIInRef"));

        ContextActor oursContextActor = new ContextActor("OursCollectActor");
        ContextLocation oursContextLocation = new ContextLocation("OursCollectLocation");
        ContextDate oursContextDate = new ContextDate("OursCollectDate");
        oursContextDate.setDateFormat("DayMonthYear");
        oursContextDate.setDateStandard("ISO");
        oursContextDate.putProperty("Date", "Date", "DATE");
        Context oursContext = new Context(oursContextActor, oursContextLocation, oursContextDate);
        ours.setContext(oursContext);

        res = visitor.visit(project);
        assertEquals(7, ((Database) res).getTables().size());
        assertEquals(2, ((Database) res).getForeignKeys().size());
        assertEquals("OursCollectActor", ((Database) res).getTable("OursCollectActor").getName());
        assertEquals("OursCollectLocation", ((Database) res).getTable("OursCollectLocation").getName());
        Table oursCollectActor = ((Database) res).getTable("OursCollectActor");
        assertEquals(1, oursCollectActor.getColumns().size());

        oursContextActor.putProperty("Name", "Name", "VARCHAR(100)");

        res = visitor.visit(project);
        oursCollectActor = ((Database) res).getTable("OursCollectActor");
        assertEquals(2, oursCollectActor.getColumns().size());

        Observation peser = new Observation(1, "Peser", "1");
        peser.setEntity(ours);
        ContextActor peserContextActor = new ContextActor("PeserCollectActor");
        ContextLocation peserContextLocation = new ContextLocation("");
        ContextDate peserContextDate = new ContextDate("PeserCollectDate");
        peserContextDate.setDateFormat("DayMonthYear");
        peserContextDate.setDateStandard("ISO");
        peserContextDate.putProperty("DateTime", "DateTime", "DATETIME");
        Context peserContext = new Context(peserContextActor, peserContextLocation, peserContextDate);
        peser.setContext(peserContext);
        Characteristic poids = new Characteristic(ours.getId(), peser.getId(), "Poids", "#Weight", true, "FLOAT");
        peser.addCharacteristic(poids);
        poids.setEntity(ours);

        res = visitor.visit(project);
        Table peserTable = ((Database) res).getTable("Peser");
        assertNull(peserTable);
        assertEquals(3, ((Database) res).getForeignKeys().size());
    }

}