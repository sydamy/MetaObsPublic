package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.Characteristic;
import fr.ufc.metaobs.model.Entity;
import fr.ufc.metaobs.utils.FileSystemUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CharacteristicFormViewController extends AbstractFormViewController {

    private final Entity entity;
    private final Map<String, TreeItem<OntClass>> treeItemRootMap;
    private final ObjectProperty<OntClass> chosenCharacteristic;
    private final ObjectProperty<OntClass> chosenUnit;
    private final ObjectProperty<OntClass> chosenClassification;
    private final ObjectProperty<OntClass> chosenTool;
    private final StringProperty chosenProtocol;
    private final StringProperty chosenTag;
    private final OwlHandler owlHandler;
    private Characteristic characteristic;

    @FXML
    private Text textForm;

    @FXML
    private TextField entityTextField;

    @FXML
    private TextField characteristicTextField;

    @FXML
    private TextField newNameTextField;

    @FXML
    private TextArea protocolTextArea;

    @FXML
    private TextField toolTextField;

    @FXML
    private TextField unitTextField;

    @FXML
    private TextField classificationTextField;

    @FXML
    private CustomFieldTypeMenuButton fieldTypeMenuButton;

    @FXML
    private CheckBox nullableCheckBox;

    @FXML
    private Label inputTreeLabel;

    @FXML
    private TreeView<OntClass> inputTreeView;

    @FXML
    private Button inputTreeChooseButton;

    @FXML
    private Button inputTreeAddButton;


    public CharacteristicFormViewController(Entity entity, Characteristic characteristic) {
        super();
        this.entity = entity;
        this.characteristic = characteristic;
        chosenTag = new SimpleStringProperty();
        chosenCharacteristic = new SimpleObjectProperty<>();
        chosenUnit = new SimpleObjectProperty<>();
        chosenClassification = new SimpleObjectProperty<>();
        chosenTool = new SimpleObjectProperty<>();
        chosenProtocol = new SimpleStringProperty();
        treeItemRootMap = new HashMap<>();
        owlHandler = Controller.getInstance().getOwlHandler();
        List<Tags> tags = List.of(Tags.Characteristic, Tags.Unit, Tags.Classification, Tags.Tool);
        for (Tags tag : tags) {
            OntClass tagClass = owlHandler.getClassesWithTag(tag.toString()).get(0);
            treeItemRootMap.put(tag.toString(), buildRecursiveTreeItem(owlHandler, tagClass));
        }
    }

    private TreeItem<OntClass> buildRecursiveTreeItem(OwlHandler owlHandler, OntClass ontClass) {
        TreeItem<OntClass> root = new TreeItem<>(ontClass);
        List<OntClass> subClasses = owlHandler.getSubclasses(ontClass);
        for (OntClass subClass : subClasses) {
            root.getChildren().add(buildRecursiveTreeItem(owlHandler, subClass));
        }
        return root;
    }

    @FXML
    public void initialize() {
        String text = "Add characteristic";
        if (characteristic != null) {
            text = "Edit characteristic";
        }
        textForm.setText(text);
        protocolTextArea.textProperty().bindBidirectional(chosenProtocol);
        protocolTextArea.setPrefColumnCount(17);
        inputTreeLabel.textProperty().bind(chosenTag);
        //on spécifie une CellFactory qui affiche le localeName de l'OntClass
        inputTreeView.setCellFactory(ontClassTreeView -> new TreeCell<>() {
            @Override
            protected void updateItem(OntClass ontClass, boolean empty) {
                super.updateItem(ontClass, empty);
                if (empty || ontClass == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    //on affiche que le localName pour pas afficher toute l'URI
                    setText(owlHandler.getLocalName(ontClass));
                    setGraphic(getTreeItem().getGraphic());
                }
            }
        });
        //la racine de la TreeView sera soit celle obtenue en fonction du tag choisi, soit vide
        inputTreeView.rootProperty().bind(
                Bindings.createObjectBinding(
                        () -> chosenTag.isNotNull().get() ? treeItemRootMap.get(chosenTag.get()) : new TreeItem<>()
                        , chosenTag)
        );

        //le texte affiché dans le TextField sera seulement le localName de l'OntClass choisie, ou vide
        characteristicTextField.textProperty().bind(Bindings.createStringBinding(
                () -> chosenCharacteristic.isNotNull().get() ? owlHandler.getLocalName(chosenCharacteristic.getValue()) : ""
                , chosenCharacteristic)
        );
        unitTextField.textProperty().bind(Bindings.createStringBinding(
                () -> chosenUnit.isNotNull().get() ? owlHandler.getLocalName(chosenUnit.getValue()) : ""
                , chosenUnit
        ));
        classificationTextField.textProperty().bind(Bindings.createStringBinding(
                () -> chosenClassification.isNotNull().get() ? owlHandler.getLocalName(chosenClassification.getValue()) : ""
                , chosenClassification
        ));
        toolTextField.textProperty().bind(Bindings.createStringBinding(
                () -> chosenTool.isNotNull().get() ? owlHandler.getLocalName(chosenTool.getValue()) : ""
                , chosenTool
        ));

        //on ajoute un listener pour le focus qui sélectionne le tag à utiliser et change l'action à effectuer
        //quand on appuie sur le bouton Choose
        characteristicTextField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                chosenTag.set(Tags.Characteristic.toString());
                inputTreeChooseButton.setOnAction(actionEvent -> chooseActionButton(chosenCharacteristic));
                inputTreeAddButton.setOnAction(actionEvent -> addActionButton(chosenCharacteristic, "characteristic"));
            }
        });
        unitTextField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                chosenTag.set(Tags.Unit.toString());
                inputTreeChooseButton.setOnAction(actionEvent -> chooseActionButton(chosenUnit));
                inputTreeAddButton.setOnAction(actionEvent -> addActionButton(chosenUnit, "unit"));
            }
        });
        classificationTextField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                chosenTag.set(Tags.Classification.toString());
                inputTreeChooseButton.setOnAction(actionEvent -> chooseActionButton(chosenClassification));
                inputTreeAddButton.setOnAction(actionEvent -> addActionButton(chosenClassification, "classification"));
            }
        });
        toolTextField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                chosenTag.set(Tags.Tool.toString());
                inputTreeChooseButton.setOnAction(actionEvent -> chooseActionButton(chosenTool));
                inputTreeAddButton.setOnAction(actionEvent -> addActionButton(chosenTool, "tool"));
            }
        });
        newNameTextField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                chosenTag.set(null);
            }
        });
        protocolTextArea.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                chosenTag.set(null);
            }
        });
        initValues();
    }

    /**
     * On remplit les champs du formulaire avec les données déjà connues
     */
    private void initValues() {
        entityTextField.setText(entity.getName());
        if (characteristic != null) {
            newNameTextField.setText(characteristic.getName());
            chosenCharacteristic.set(owlHandler.getClassFromURI(characteristic.getIri()));
            if (characteristic.getUnit() != null) {
                chosenUnit.set(owlHandler.getClassFromURI(characteristic.getUnit().getKey()));
            }
            if (characteristic.getClassification() != null) {
                chosenClassification.set(owlHandler.getClassFromURI(characteristic.getClassification().getKey()));
            }
            chosenProtocol.set(characteristic.getProtocol());
            if (characteristic.getTool() != null) {
                chosenTool.set(owlHandler.getClassFromURI(characteristic.getTool().getKey()));
            }
            fieldTypeMenuButton.selectTypeSize(characteristic.getFieldType(), characteristic.getFieldSize());
            nullableCheckBox.setSelected(characteristic.isNullable());
        }
    }

    /**
     * Méthode appelée lorsque l'utilisateur appuie sur le bouton "Choose" pour garder en mémoire l'item sélectionné
     * dans inpurtTreeView.
     *
     * @param chosenProperty l'ObjectProperty qui gardera la valeur choisie
     */
    private void chooseActionButton(ObjectProperty<OntClass> chosenProperty) {
        //on vérifie que la racine n'est pas sélectionnée
        if (inputTreeView.getSelectionModel().getSelectedItem() != null
                && inputTreeView.getSelectionModel().getSelectedItem().getParent() != null) {
            chosenProperty.setValue(inputTreeView.getSelectionModel().getSelectedItem().getValue());
            chosenTag.set(null);
        }
    }

    /**
     * Méthode appelée lorsque l'utilisateur appuie sur le bouton "Add" pour demander à l'utilisateur un nom
     * d'une nouvelle propriété (Characteristic, Unit, Classification, Tool), l'ajouter à l'ontologie du projet et dans
     * inputTreeView et la garder en mémoire.
     *
     * @param chosenProperty l'ObjectProperty qui gardera la valeur ajoutée
     * @param propertyName   le nom du type de la propriété à ajouter
     */
    private void addActionButton(ObjectProperty<OntClass> chosenProperty, String propertyName) {
        //on vérifie que la sélection n'est pas nulle
        if (inputTreeView.getSelectionModel().getSelectedItem() != null) {
            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setHeaderText("Enter the new " + propertyName + "'s name.");
            Optional<String> optionalRes = inputDialog.showAndWait();
            if (optionalRes.isPresent()) {
                OntClass newClass = inputNewOntClass(inputTreeView.getSelectionModel().getSelectedItem().getValue(), optionalRes.get());
                chosenProperty.setValue(newClass);
                TreeItem<OntClass> newItem = new TreeItem<>(newClass);
                inputTreeView.getSelectionModel().getSelectedItem().getChildren().add(newItem);
                inputTreeView.getSelectionModel().select(newItem);
                chosenTag.set(null);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(true);
            alert.setTitle("Information dialog");
            alert.setHeaderText("Please select a parent to create a new " + propertyName + ".");
            alert.showAndWait();
        }
    }

    /**
     * Crée une nouvelle OntClass fille de l'OntClass donnée en paramètre, avec le nom donné en paramètre.
     *
     * @param parentSelected l'OntClass parent
     * @param newNodeName    le nom de la nouvelle OntClass
     * @return la nouvelle OntClass
     */
    private OntClass inputNewOntClass(OntClass parentSelected, String newNodeName) {
        String baseUrl = Controller.getInstance().getProject().getRepProject();
        String suffixUrl = File.separator + Controller.getInstance().getProject().getName() + ".owl";
        File newOwlFile = new File(baseUrl + suffixUrl);
        boolean newOwlFileExists = newOwlFile.exists();
        baseUrl = FileSystemUtils.addFileUriScheme(baseUrl);
        try {
            if (parentSelected != null) {
                OntModel newModel;
                //si le fichier n'existe pas
                if (!newOwlFileExists) {
                    newOwlFile.createNewFile();

                    newModel = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LANG);
                    //si le fichier owl du projet existe déjà
                } else {
                    newModel = Controller.getInstance().getProject().getProjectModel();
                }

                OntClass newClass = newModel.createClass(baseUrl + suffixUrl + "#" + newNodeName);
                newClass.addSuperClass(parentSelected);
                newModel.write(new FileOutputStream(newOwlFile), "RDF/XML-ABBREV");

                Controller.getInstance().getProject().setProjectOwls(new File[]{newOwlFile});
                Controller.getInstance().reload();
                return newClass;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Characteristic getCharacteristic() {
        return characteristic;
    }

    @Override
    protected boolean checkRequirements() {
        boolean res = chosenCharacteristic.isNotNull().get();

        return res;
    }

    @Override
    protected void submitImpl() {
        //si c'est un ajout de caractéristique, on en crée une nouvelle
        if (characteristic == null) {
            characteristic = new Characteristic(entity.getId());
        }
        String name = newNameTextField.getText().isBlank() ? owlHandler.getLocalName(chosenCharacteristic.getValue()) : newNameTextField.getText();
        //on modifie la caractéristique, qu'elle soit nouvelle ou déjà créée
        characteristic.setName(name);
        characteristic.setIri(chosenCharacteristic.getValue().getURI());
        characteristic.setNullable(nullableCheckBox.isSelected());
        characteristic.setFieldTypeSize(fieldTypeMenuButton.getToggleGroup().getSelectedToggle().getUserData().toString());
        if (chosenUnit.get() != null) {
            OntClass ontClass = chosenUnit.get();
            characteristic.setUnit(new Pair<>(ontClass.getURI(), owlHandler.getLocalName(ontClass)));
        }
        if (chosenClassification.get() != null) {
            OntClass ontClass = chosenClassification.get();
            characteristic.setClassification(new Pair<>(ontClass.getURI(), owlHandler.getLocalName(ontClass)));
        }
        if (chosenTool.get() != null) {
            OntClass ontClass = chosenTool.get();
            characteristic.setTool(new Pair<>(ontClass.getURI(), owlHandler.getLocalName(ontClass)));
        }
        characteristic.setProtocol(chosenProtocol.get());
    }

    @Override
    protected void cancelImpl() {

    }
}
