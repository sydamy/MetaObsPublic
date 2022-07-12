package fr.ufc.metaobs.controllers;

import fr.ufc.metaobs.controllers.services.ExportProjectService;
import fr.ufc.metaobs.controllers.services.FinishExportDatabaseService;
import fr.ufc.metaobs.controllers.services.OpenCreateProjectService;
import fr.ufc.metaobs.controllers.services.SaveProjectService;
import fr.ufc.metaobs.controllers.services.tasks.*;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.handlers.ProjectXmlReader;
import fr.ufc.metaobs.model.Entity;
import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.model.export.Database;
import fr.ufc.metaobs.utils.FileSystemUtils;
import fr.ufc.metaobs.visitors.ProjectVisitorExport;
import fr.ufc.metaobs.visitors.ProjectVisitorXml;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.function.Consumer;

/**
 * Classe singleton qui permet à tous les "ViewController" d'agir sur le modèle
 */
public class Controller {

    private static Controller instance;

    private final ObjectProperty<Project> project;
    private final StringProperty status;
    private final ProjectXmlReader projectXmlReader;
    private final OwlHandler owlHandler;
    private final ProjectVisitorXml visitorXml;
    private final ProjectVisitorExport visitorExport;
    private final ExportProjectService generateDatabaseService;
    private final FinishExportDatabaseService exportToSqlService;
    private final FinishExportDatabaseService exportToOdsService;
    private final SaveProjectService saveProjectService;
    private final OpenCreateProjectService createProjectService;
    private final OpenCreateProjectService openProjectService;
    private String baseUri;

    public Controller() {
        project = new SimpleObjectProperty<>();
        projectXmlReader = new ProjectXmlReader();
        File ontoDir = new File(FileSystemUtils.ONTO_PATH);
        if (!ontoDir.exists()) {
            ontoDir = new File("." + FileSystemUtils.ONTO_PATH);
        }
        baseUri = FileSystemUtils.getBaseURI(ontoDir);
        owlHandler = new OwlHandler();
        visitorXml = new ProjectVisitorXml();
        visitorExport = new ProjectVisitorExport();
        status = new SimpleStringProperty();

        //initialisation des ChangeListener pour indiquer l'état actuel des services
        ChangeListener<Worker.State> saveListener = (observableValue, oldState, newState) -> {
            switch (newState) {
                case FAILED:
                case CANCELLED:
                    status.set("Failed to save project.");
                    break;
                case RUNNING:
                    status.set("Saving project...");
                    break;
                case SUCCEEDED:
                    status.set("Project saved.");
                    break;
            }
        };
        ChangeListener<Worker.State> createListener = (observableValue, oldState, newState) -> {
            switch (newState) {
                case FAILED:
                case CANCELLED:
                    status.set("Failed to create project.");
                    break;
                case RUNNING:
                    status.set("Creating project...");
                    break;
                case SUCCEEDED:
                    status.set("Project created successfully.");
                    break;
            }
        };
        ChangeListener<Worker.State> openListener = (observableValue, oldState, newState) -> {
            switch (newState) {
                case FAILED:
                case CANCELLED:
                    status.set("Failed to open project.");
                    break;
                case RUNNING:
                    status.set("Opening project...");
                    break;
                case SUCCEEDED:
                    status.set("Project opened successfully.");
                    break;
            }
        };
        ChangeListener<Worker.State> exportToSqlListener = (observableValue, oldState, newState) -> {
            switch (newState) {
                case FAILED:
                case CANCELLED:
                    status.set("Failed to generate database.");
                    break;
                case SUCCEEDED:
                    status.set("Database generated successfully.");
                    break;
            }
        };
        ChangeListener<Worker.State> exportToOdsListener = (observableValue, oldState, newState) -> {
            switch (newState) {
                case FAILED:
                case CANCELLED:
                    status.set("Failed to export project.");
                    break;
                case SUCCEEDED:
                    status.set("Project exported successfully.");
                    break;
            }
        };
        //EventHandler commun pour créer ou ouvrir un projet
        EventHandler<WorkerStateEvent> serviceSuccessHandler = workerStateEvent -> {
            //on récupère le projet résultat depuis la source de l'évènement
            Project result = (Project) workerStateEvent.getSource().getValue();
            try {
                //on charge le projet
                load(result);
                //ensuite on exécute le 'callback' du service source
                Consumer<Project> consumer = ((OpenCreateProjectService) workerStateEvent.getSource()).getCallback();
                if (consumer != null) {
                    consumer.accept(result);
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setResizable(true);
                    alert.setHeaderText("An error occured loading ontologies");
                    alert.setContentText("Please relocate ontologies using the menu help. " +
                            "If this error occurs again, try to open or create another project.");
                    alert.showAndWait();
                });
            }
        };
        EventHandler<WorkerStateEvent> exportServiceSuccessHandler = workerStateEvent -> {
            Database result = (Database) workerStateEvent.getSource().getValue();
            Consumer<Database> consumer = ((ExportProjectService) workerStateEvent.getSource()).getCallback();
            if (consumer != null) {
                consumer.accept(result);
            }
        };


        saveProjectService = new SaveProjectService(visitorXml, SaveProjectTask.class);
        saveProjectService.stateProperty().addListener(saveListener);

        generateDatabaseService = new ExportProjectService(visitorExport, GenerateDatabaseTask.class);
        generateDatabaseService.setOnSucceeded(exportServiceSuccessHandler);

        exportToOdsService = new FinishExportDatabaseService(ExportToOdsTask.class);
        exportToOdsService.stateProperty().addListener(exportToOdsListener);

        exportToSqlService = new FinishExportDatabaseService(ExportToSqlTask.class);
        exportToSqlService.stateProperty().addListener(exportToSqlListener);

        openProjectService = new OpenCreateProjectService(projectXmlReader, OpenProjectTask.class);
        openProjectService.stateProperty().addListener(openListener);
        openProjectService.setOnSucceeded(serviceSuccessHandler);

        createProjectService = new OpenCreateProjectService(projectXmlReader, CreateProjectTask.class);
        createProjectService.stateProperty().addListener(createListener);
        createProjectService.setOnSucceeded(serviceSuccessHandler);
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public ProjectXmlReader getXmlReader() {
        return projectXmlReader;
    }

    public OwlHandler getOwlHandler() {
        return owlHandler;
    }

    public boolean projectIsOpened() {
        return project.isNotNull().get();
    }

    public Project getProject() {
        return project.get();
    }

    public ObjectProperty<Project> projectProperty() {
        return project;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void closeProject() {
        project.set(null);
        status.set("Project closed");
    }

    public void openProject(File fileProject, Consumer<Project> callback) {
        openProjectService.setFileProject(fileProject);
        openProjectService.setBaseUri(baseUri);
        openProjectService.setCallback(callback);
        openProjectService.restart();
    }

    public void newProject(String ontMetaobs, String projectRep, String projectName, Consumer<Project> callback) {
        createProjectService.setFileProject(new File(projectRep, projectName));
        createProjectService.setOntMetaobs(ontMetaobs);
        createProjectService.setBaseUri(baseUri);
        //on sauvegarde puis on appellera le Consumer callback
        Consumer<Project> newCallback = this::saveProject;
        createProjectService.setCallback(newCallback.andThen(callback));
        createProjectService.restart();
    }

    public void saveProject(Project projectToSave) {
        saveProjectService.setProject(projectToSave);
        saveProjectService.restart();
    }

    public void saveProject() {
        Project projectToSave = getProject();
        if (projectToSave != null) {
            saveProject(projectToSave);
        }
    }

    public void generateDatabase(Consumer<Database> callback) {
        generateDatabaseService.setProject(getProject());
        generateDatabaseService.setCallback(callback);
        generateDatabaseService.restart();
    }

    public void finishExportProjectToSql(Database database) {
        exportToSqlService.setProject(getProject());
        exportToSqlService.setDatabase(database);
        exportToSqlService.restart();
    }

    public void finishExportProjectToOds(Database database) {
        exportToOdsService.setProject(getProject());
        exportToOdsService.setDatabase(database);
        exportToOdsService.restart();
    }

    public Binding<ObservableList<Entity>> projectEntitiesProperty() {
        return Bindings.select(project, "entities");
    }

    public Binding<String> projectNameProperty() {
        return Bindings.select(project, "name");
    }

    /**
     * Change la baseUri, et si le projet est ouvert alors on lui met la nouvelle baseUri et on le recharge.
     *
     * @param baseUri la nouvelle baseUri pour ouvrir les projets
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
        if (projectIsOpened()) {
            getProject().setBaseUri(baseUri);
            load(getProject());
        }
    }

    /**
     * Charge un projet, en lui faisant charger les ontologies, puis initialise l'owlHandler avec
     * l'ontologie du projet et son modèle d'ontologie personnelle
     *
     * @param project le projet à charger
     */
    public void load(Project project) {
        project.loadProjectOwls();
        //on initialise l'owlHandler avec l'ontologie du projet
        this.owlHandler.init(project.getOntology(), project.getProjectModel());
        //et on 'ouvre' ce projet
        this.project.set(project);
    }

    /**
     * Recharge le projet ouvert actuellement.
     */
    public void reload() {
        if (projectIsOpened()) {
            getProject().loadProjectOwls();
            this.owlHandler.init(getProject().getOntology(), getProject().getProjectModel());
        }
    }

}
