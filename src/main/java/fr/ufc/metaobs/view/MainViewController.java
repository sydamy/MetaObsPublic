package fr.ufc.metaobs.view;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.exceptions.NoOntClassException;
import fr.ufc.metaobs.model.Characteristic;
import fr.ufc.metaobs.model.Entity;
import fr.ufc.metaobs.model.Observation;
import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.model.export.Database;
import fr.ufc.metaobs.utils.DateUtils;
import fr.ufc.metaobs.utils.FileSystemUtils;
import fr.ufc.metaobs.view.dialogs.NewProjectDialog;
import fr.ufc.metaobs.view.dialogs.RelocateOntologiesDialog;
import fr.ufc.metaobs.view.dialogs.SaveAsDialog;
import fr.ufc.metaobs.view.forms.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

public class MainViewController {


    private Consumer<Database> afterMerge;

    @FXML
    private Tab tabProject;

    @FXML
    private ListView<Entity> entitiesListView;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private Label statusLabel;


    @FXML
    public void initialize() {
        tabProject.textProperty().bind(Controller.getInstance().projectNameProperty());
        entitiesListView.setCellFactory(new EntityCellFactory(this));
        entitiesListView.itemsProperty().bind(Controller.getInstance().projectEntitiesProperty());
        statusLabel.textProperty().bind(Controller.getInstance().statusProperty());

        File ontoDir = new File(FileSystemUtils.ONTO_PATH);
        if (!ontoDir.exists()) {
            ontoDir = new File("." + FileSystemUtils.ONTO_PATH);
        }
        if (ontoDir.exists() && ontoDir.isDirectory()) {
            if (FileSystemUtils.getBaseURI(ontoDir) == null) {
                File baseProperties = new File(ontoDir, FileSystemUtils.BASE_URI_PROPERTIES_FILE);
                try {
                    boolean created = baseProperties.createNewFile();
                    if (created) {
                        FileOutputStream fileOutputStream = new FileOutputStream(baseProperties);
                        Properties properties = new Properties();
                        properties.setProperty("BASE", "");
                        properties.store(fileOutputStream, null);
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(this::relocateOntologiesDialog);
            }
        }
    }

    @FXML
    public void exit() {
        Platform.exit();
    }

    @FXML
    public void closeProject() {
        Controller.getInstance().closeProject();
        clearMainScrollPaneContent();
    }

    @FXML
    public void newProjectDialog() {
        NewProjectDialog dialog = new NewProjectDialog();
        boolean projectAlreadyExists = false;
        File metaobsFile;
        File dataDir;
        String projectName;
        String projectDir;
        do {
            if (projectAlreadyExists) {
                dialog.setHeaderText("A project with this name already exists. Please enter another name.");
            }
            Optional<Pair<File, String>> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }
            Pair<File, String> resultPair = result.get();
            metaobsFile = resultPair.getKey();
            projectName = resultPair.getValue();
            if (metaobsFile == null || projectName == null || projectName.isEmpty()) {
                return;
            }
            dataDir = metaobsFile.getParentFile().getParentFile().getParentFile().getParentFile();
            projectDir = projectName + "_" + DateUtils.formatDate("-");
            Path projectPath = Path.of(dataDir.getAbsolutePath() + File.separator + FileSystemUtils.PROJECTS_PATH, projectDir);
            projectAlreadyExists = result.map(name -> name.getValue().isEmpty() || Files.exists(projectPath)).orElse(true);
        } while (projectAlreadyExists);
        Controller.getInstance().newProject(metaobsFile.getAbsolutePath(), dataDir.getAbsolutePath() + File.separator + FileSystemUtils.PROJECTS_PATH + projectDir, projectName, this::editMetadataForm);
    }

    @FXML
    public void openProjectDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open project");
        //on met comme répertoire initial le répertoire des projets qui se trouve
        //dans "./data/"
        fileChooser.setInitialDirectory(new File(FileSystemUtils.PROJECTS_PATH));
        fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("XML", "*.xml"));
        File fileProject;
        try {
            fileProject = fileChooser.showOpenDialog(null);
        } catch (IllegalArgumentException ignored) {
            //au cas où une exception est levée, on met comme répertoire initial le répertoire projets
            //qui se trouve dans "../data/"
            fileChooser.setInitialDirectory(new File("." + FileSystemUtils.PROJECTS_PATH));
            try {
                fileProject = fileChooser.showOpenDialog(null);
            } catch (IllegalArgumentException ignored2) {
                //au cas où une exception est encore levée, alors on met comme répertoire initial
                //le répertoire courant, aucune erreur ne devrait arriver
                fileChooser.setInitialDirectory(new File("."));
                fileProject = fileChooser.showOpenDialog(null);
            }
        }
        if (fileProject != null) {
            Controller.getInstance().openProject(fileProject, this::showMetadataInfo);
        }
    }

    @FXML
    public void saveAsDialog() {
        if (Controller.getInstance().projectIsOpened()) {
            SaveAsDialog saveAsDialog = new SaveAsDialog();
            Optional<Pair<String, String>> result = saveAsDialog.showAndWait();
            if (result.isPresent()) {
                Pair<String, String> directoryAndRename = result.get();
                String directory = directoryAndRename.getKey();
                String rename = directoryAndRename.getValue();
                if (!directory.isBlank()) {
                    String name = rename.isBlank() ? Controller.getInstance().getProject().getName() : rename;
                    Path newPath = Paths.get(directory, name + "_" + DateUtils.formatDate("-"));
                    Controller.getInstance().getProject().setRepProject(newPath.toAbsolutePath().toString());
                    Controller.getInstance().getProject().setName(name);
                    Controller.getInstance().saveProject();
                }
            }
        }
    }

    @FXML
    public void relocateOntologiesDialog() {
        RelocateOntologiesDialog dialog = new RelocateOntologiesDialog();
        Optional<File> result = dialog.showAndWait();
        if (result.isPresent()) {
            File ontoDirectory = result.get();
            String newBaseUri = FileSystemUtils.relocateOntologies(ontoDirectory);
            Controller.getInstance().setBaseUri(newBaseUri);
        }
    }

    @FXML
    public void generateDatabase() {
        if (Controller.getInstance().projectIsOpened()) {
            afterMerge = Controller.getInstance()::finishExportProjectToSql;
            Controller.getInstance().generateDatabase(this::mergeTablesForm);
        }
    }

    private void mergeTablesForm(Database database) {
        FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/edit/merge_tables_form.fxml"));
        MergeTablesFormViewController mergeTablesFormViewController = new MergeTablesFormViewController(database);
        loader.setController(mergeTablesFormViewController);
        try {
            Node mergeTablesPane = loader.load();
            mainScrollPane.setContent(mergeTablesPane);
            mergeTablesFormViewController.setConsumer(submitted -> {
                if (submitted) {
                    Database result = mergeTablesFormViewController.getDatabase();
                    if (afterMerge != null) {
                        afterMerge.accept(result);
                    }
                    afterMerge = null;
                }
                showMetadataInfo();
            });
        } catch (LoadException e) {
            if (e.getCause() instanceof NoOntClassException) {
                showErrorOntologiesDialog();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exportToOds() {
        if (Controller.getInstance().projectIsOpened()) {
            afterMerge = Controller.getInstance()::finishExportProjectToOds;
            Controller.getInstance().generateDatabase(this::mergeTablesForm);
        }
    }

    /**
     * Afficher les métadonnées du projet, cette méthode est appelée depuis le menu de l'application.
     * Vérifie d'abord qu'un projet est ouvert.
     */
    @FXML
    public void showMetadataInfo() {
        if (Controller.getInstance().projectIsOpened()) {
            showMetadataInfo(Controller.getInstance().getProject());
        }
    }

    /**
     * Affiche les métadonnées d'un projet.
     *
     * @param project le projet pour lequel afficher les métadonnées, considéré comme ouvert (non null)
     */
    public void showMetadataInfo(Project project) {
        FXMLLoader loaderMetadata = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/view/metadata.fxml"));
        MetadataViewController metadataViewController = new MetadataViewController(project.getObservatory());
        loaderMetadata.setController(metadataViewController);
        try {
            Node metadata = loaderMetadata.load();
            mainScrollPane.setContent(metadata);
        } catch (LoadException e) {
            if (e.getCause() instanceof NoOntClassException) {
                showErrorOntologiesDialog();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void editMetadataForm() {
        if (Controller.getInstance().projectIsOpened()) {
            editMetadataForm(Controller.getInstance().getProject());
        }
    }

    /**
     * Affiche le formulaire pour modifier les métadonnées du projet.
     */
    public void editMetadataForm(Project project) {
        FXMLLoader loaderMetadata = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/edit/metadata.fxml"));
        MetadataFormViewController metadataFormViewController = new MetadataFormViewController(project.getObservatory());
        loaderMetadata.setController(metadataFormViewController);
        try {
            Node metadatapane = loaderMetadata.load();
            mainScrollPane.setContent(metadatapane);
            mainScrollPane.setVvalue(0.0);
            //on exécutera ce Consumer lorsqu'on validera ou annulera le formulaire
            metadataFormViewController.setConsumer(submitted -> {
                //s'il est validé, on sauvegarde le projet
                if (submitted) {
                    Controller.getInstance().saveProject();
                }
                //puis on revient à l'affichage d'accueil
                showMetadataInfo();
            });
        } catch (LoadException e) {
            if (e.getCause() instanceof NoOntClassException) {
                showErrorOntologiesDialog();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Affiche le formulaire pour ajouter une entité.
     */
    @FXML
    public void addEntityForm() {
        editEntityForm(null);
    }

    /**
     * Affiche le formulaire pour ajouter ou modifier une entité.
     *
     * @param entity l'entité à modifier, 'null' pour un ajout
     */
    public void editEntityForm(Entity entity) {
        if (Controller.getInstance().projectIsOpened()) {
            FXMLLoader loaderEntity = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/edit/entity.fxml"));
            List<Entity> projectEntities = Controller.getInstance().getProject().getEntities();
            EntityFormViewController entityFormViewController = new EntityFormViewController(projectEntities, entity);
            loaderEntity.setController(entityFormViewController);
            try {
                Node content = loaderEntity.load();
                mainScrollPane.setContent(content);
                mainScrollPane.setVvalue(0.0);
                entityFormViewController.setConsumer(submitted -> {
                    if (submitted) {
                        if (entity == null) {
                            Entity newEntity = entityFormViewController.getEntity();
                            Controller.getInstance().getProject().addEntity(newEntity);
                        }
                        Controller.getInstance().saveProject();
                    }
                    //TODO: afficher l'entité au lieu des métadonnées
                    showMetadataInfo();
                });
            } catch (LoadException e) {
                if (e.getCause() instanceof NoOntClassException) {
                    showErrorOntologiesDialog();
                } else {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Affiche le formulaire pour ajouter une caractéristique à une entité
     *
     * @param entity l'entité à laquelle ajouter une cractéristique
     */
    public void addCharacteristicForm(Entity entity) {
        editCharacteristicForm(entity, null);
    }

    /**
     * Affiche le formulaire pour ajouter ou modifier une caractéristique à une entité
     *
     * @param entity         l'entité à laquelle ajouter/modifier la caractéristique
     * @param characteristic la caractéristique à modifier, 'null' pour un ajout
     */
    public void editCharacteristicForm(Entity entity, Characteristic characteristic) {
        if (Controller.getInstance().projectIsOpened()) {
            FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/edit/characteristic.fxml"));
            CharacteristicFormViewController characteristicFormViewController = new CharacteristicFormViewController(entity, characteristic);
            loader.setController(characteristicFormViewController);
            try {
                Node content = loader.load();
                mainScrollPane.setContent(content);
                mainScrollPane.setVvalue(0.0);
                characteristicFormViewController.setConsumer(submitted -> {
                    if (submitted) {
                        if (characteristic == null) {
                            Characteristic newCharacteristic = characteristicFormViewController.getCharacteristic();
                            Controller.getInstance().getProject().addCharacteristic(newCharacteristic);
                        }
                        Controller.getInstance().saveProject();
                    }
                    //TODO: afficher la caractéristique au lieu des métadonnées
                    showMetadataInfo();
                });
            } catch (LoadException e) {
                if (e.getCause() instanceof NoOntClassException) {
                    showErrorOntologiesDialog();
                } else {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Affiche le formulaire pour ajouter une observation à une entité
     *
     * @param entity l'entité à laquelle ajouter une observation
     */
    public void addObservationForm(Entity entity) {
        editObservationForm(entity, null);
    }

    /**
     * Affiche le formulaire pour ajouter ou modifier une observation à une entité
     *
     * @param entity      l'entité à laquelle ajouter/modifier l'observation
     * @param observation l'observation à modifier, 'null' pour un ajout
     */
    public void editObservationForm(Entity entity, Observation observation) {
        if (Controller.getInstance().projectIsOpened()) {
            FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/edit/observation.fxml"));
            ObservationFormViewController observationFormViewController = new ObservationFormViewController(entity, observation);
            loader.setController(observationFormViewController);
            try {
                Node content = loader.load();
                mainScrollPane.setContent(content);
                mainScrollPane.setVvalue(0.0);
                observationFormViewController.setConsumer(submitted -> {
                    if (submitted) {
                        if (observation == null) {
                            Observation newObservation = observationFormViewController.getObservation();
                            Controller.getInstance().getProject().addObservation(entity, newObservation);
                        }
                        Controller.getInstance().saveProject();
                    }
                    //TODO: afficher l'observation au lieu des métadonnées
                    showMetadataInfo();
                });
            } catch (LoadException e) {
                if (e.getCause() instanceof NoOntClassException) {
                    showErrorOntologiesDialog();
                } else {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void displayEntities() {
        if (Controller.getInstance().projectIsOpened()) {
            displayEntities(Controller.getInstance().getProject());
        }
    }

    public void displayEntities(Project project) {
        FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/view/list_entities.fxml"));
        ListEntitiesViewController listEntitiesViewController = new ListEntitiesViewController(project.entitiesProperty());
        loader.setController(listEntitiesViewController);
        try {
            Node content = loader.load();
            mainScrollPane.setContent(content);
            mainScrollPane.setVvalue(0.0);
        } catch (LoadException e) {
            if (e.getCause() instanceof NoOntClassException) {
                showErrorOntologiesDialog();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void displayCharacteristics() {
        if (Controller.getInstance().projectIsOpened()) {
            displayCharacteristics(Controller.getInstance().getProject());
        }
    }

    public void displayCharacteristics(Project project) {
        FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/view/list_characteristics.fxml"));
        ListCharacteristicsViewController listCharacteristicsViewController = new ListCharacteristicsViewController(project.characteristicsProperty());
        loader.setController(listCharacteristicsViewController);
        try {
            Node content = loader.load();
            mainScrollPane.setContent(content);
            mainScrollPane.setVvalue(0.0);
        } catch (LoadException e) {
            if (e.getCause() instanceof NoOntClassException) {
                showErrorOntologiesDialog();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void displayObservations() {
        if (Controller.getInstance().projectIsOpened()) {
            displayObservations(Controller.getInstance().getProject());
        }
    }

    public void displayObservations(Project project) {
        FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/view/list_observations.fxml"));
        ObservableList<Observation> observations = FXCollections.observableArrayList();
        for (Entity entity : project.getEntities()) {
            observations.addAll(entity.getObservations());
        }
        ListObservationsViewController listObservationsViewController = new ListObservationsViewController(observations);
        loader.setController(listObservationsViewController);
        try {
            Node content = loader.load();
            mainScrollPane.setContent(content);
            mainScrollPane.setVvalue(0.0);
        } catch (LoadException e) {
            if (e.getCause() instanceof NoOntClassException) {
                showErrorOntologiesDialog();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void displayEntityContexts() {
        if (Controller.getInstance().projectIsOpened()) {
            displayEntityContexts(Controller.getInstance().getProject());
        }
    }

    public void displayEntityContexts(Project project) {
        FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("/fr/ufc/metaobs/view/list_entity_contexts.fxml"));
        ListEntityContextsViewController listEntityContextsViewController = new ListEntityContextsViewController(project.getEntities());
        loader.setController(listEntityContextsViewController);
        try {
            Node content = loader.load();
            mainScrollPane.setContent(content);
            mainScrollPane.setVvalue(0.0);
        } catch (LoadException e) {
            if (e.getCause() instanceof NoOntClassException) {
                showErrorOntologiesDialog();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearMainScrollPaneContent() {
        mainScrollPane.setContent(null);
    }

    public void showErrorOntologiesDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setResizable(true);
        alert.setHeaderText("An error occured loading ontologies");
        alert.setContentText("Please relocate ontologies using the menu help. " +
                "If this error occurs again, try to open or create another project.");
        alert.showAndWait();
    }

}
