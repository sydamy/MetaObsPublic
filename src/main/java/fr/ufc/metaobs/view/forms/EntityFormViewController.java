package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.exceptions.NoOntClassException;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.Entity;
import fr.ufc.metaobs.model.FieldTypeSize;
import fr.ufc.metaobs.model.Referential;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.jena.ontology.OntClass;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class EntityFormViewController extends AbstractFormViewController {

    private final ContextFormViewController contextFormViewController;
    private final ToggleGroup typeEntityToggleGroup;
    private final List<Entity> projectEntities;
    private StringBinding nameBinding;
    private Entity entity;


    @FXML
    private Text textForm;

    @FXML
    private HBox typesEntityHBox;

    @FXML
    private TextField renameEntityTextField;

    @FXML
    private HBox refEntityHBox;

    @FXML
    private ComboBox<Entity> refEntityComboBox;

    @FXML
    private CheckBox externalIdCheckBox;

    @FXML
    private CustomFieldTypeMenuButton externalIdMenuButton;

    @FXML
    private GridPane referentialGridPane;

    @FXML
    private TextField repositoryNameTextField;

    @FXML
    private TextField repositoryIriTextField;

    @FXML
    private TextField nameInRepositoryTextField;

    @FXML
    private TextField iriInRepositoryTextField;

    @FXML
    private BorderPane contextInclude;


    public EntityFormViewController(List<Entity> projectEntities, Entity entity) {
        super();
        //on ne garde que les entités différentes de 'entity' pour ne pas pouvoir mettre une entité se référençant elle-même !
        this.projectEntities = projectEntities.stream().filter(entity1 -> entity1 != entity).collect(Collectors.toList());
        this.entity = entity;
        contextFormViewController = new ContextFormViewController(entity != null ? entity.getContext() : null);
        typeEntityToggleGroup = new ToggleGroup();
    }

    public Entity getEntity() {
        return entity;
    }

    @FXML
    public void initialize() throws NoOntClassException {
        String text = "Add entity";
        if (entity != null) {
            text = "Edit entity";
        }
        textForm.setText(text);
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();
        List<OntClass> entityOntClassList = owlHandler.getClassesWithTag(Tags.Entity.toString());
        if (entityOntClassList.isEmpty()) {
            throw new NoOntClassException();
        }
        OntClass entityOntClass = entityOntClassList.get(0);
        List<String> typesEntity = owlHandler.getSubclassesNames(entityOntClass);
        for (String typeEntity : typesEntity) {
            RadioButton radioButton = new RadioButton(typeEntity);
            radioButton.setToggleGroup(typeEntityToggleGroup);
            radioButton.setUserData(typeEntity);
            if ("Sample".equals(typeEntity)) {
                refEntityHBox.visibleProperty().bind(radioButton.selectedProperty());
            }
            typesEntityHBox.getChildren().add(radioButton);
        }

        refEntityComboBox.getItems().addAll(projectEntities);

        nameBinding = Bindings.createStringBinding(() -> {
            if (renameEntityTextField.textProperty().isNotEmpty().get()) {
                return renameEntityTextField.textProperty().get();
            }
            return nameInRepositoryTextField.textProperty().get();
        }, renameEntityTextField.textProperty(), nameInRepositoryTextField.textProperty());
        externalIdMenuButton.visibleProperty().bind(externalIdCheckBox.selectedProperty());

        contextFormViewController.setEntityOfContext(nameBinding);
        //charge dynamiquement le contenu du fichier edit/context.fxml pour pouvoir lui mettre comme contrôleur celui
        //instancié avant (dans le constructeur)
        FXMLLoader fxmlLoader = new FXMLLoader(EntityFormViewController.class.getResource("/fr/ufc/metaobs/edit/context.fxml"));
        fxmlLoader.setController(contextFormViewController);
        try {
            AnchorPane content = fxmlLoader.load();
            contextInclude.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initValues();
    }

    private void initValues() {
        if (entity != null) {
            //on sélectionne le type d'entité
            for (Toggle toggle : typeEntityToggleGroup.getToggles()) {
                if (entity.getType().equals(toggle.getUserData())) {
                    toggle.setSelected(true);
                }
            }
            renameEntityTextField.setText(entity.getName());

            if (entity.getRefEntity() > -1) {
                refEntityComboBox.setValue(entity.getRefEntityObject());
            }

            //on remplit le référentiel si besoin
            if (entity.getReferential() != null) {
                Referential referential = entity.getReferential();
                repositoryNameTextField.setText(referential.getRepositoryName());
                repositoryIriTextField.setText(referential.getRepositoryIri());
                nameInRepositoryTextField.setText(referential.getNameInRepository());
                iriInRepositoryTextField.setText(referential.getIriInRepository());
            }
        }
    }

    @Override
    protected boolean checkRequirements() {
        //on vérifie qu'un type est au moins sélectionné
        boolean res = typeEntityToggleGroup.getSelectedToggle() != null;
        //on vérifie qu'il y a au moins un nom d'entité entré
        res = res && (!nameInRepositoryTextField.getText().isBlank() || !renameEntityTextField.getText().isBlank());
        if (externalIdCheckBox.isSelected()) {
            //on vérifie qu'il y a au moins un fieldType
            res = res && externalIdMenuButton.getToggleGroup().getSelectedToggle() != null;
            //on vérifie que si c'est de type VARCHAR, la taille est renseignée
            FieldTypeSize selectedUserData = (FieldTypeSize) externalIdMenuButton.getToggleGroup().getSelectedToggle().getUserData();
            if ("VARCHAR".equals(selectedUserData.getType())) {
                res = res && !selectedUserData.getSize().isBlank();
            }
        }
        //si on sélectionne Sample, on vérifie qu'il y a bien une refEntity de sélectionnée
        if (typeEntityToggleGroup.getSelectedToggle() != null && "Sample".equals(typeEntityToggleGroup.getSelectedToggle().getUserData())) {
            res = res && !refEntityComboBox.getSelectionModel().isEmpty();
        }
        res = res && contextFormViewController.checkRequirements();
        return res;
    }

    @Override
    protected void submitImpl() {
        if (entity == null) {
            entity = new Entity();
        }
        String iri = iriInRepositoryTextField.getText();
        String type = typeEntityToggleGroup.getSelectedToggle().getUserData().toString();
        String name = renameEntityTextField.getText().isBlank() ? nameInRepositoryTextField.getText() : renameEntityTextField.getText();
        entity.setType(type);
        entity.setIri(iri);
        entity.setName(name);

        Entity refEntity = refEntityComboBox.getValue();
        entity.setRefEntityObject(refEntity);
        if (refEntity == null) {
            entity.setRefEntity(-1);
        } else {
            entity.setRefEntity(refEntity.getId());
        }

        if (externalIdCheckBox.isSelected()) {
            entity.setExternalId((FieldTypeSize) externalIdMenuButton.getToggleGroup().getSelectedToggle().getUserData());
        }
        String repositoryName = repositoryNameTextField.getText();
        String repositoryIri = repositoryIriTextField.getText();
        String nameInRepository = nameInRepositoryTextField.getText();
        String iriInRepository = iriInRepositoryTextField.getText();
        Referential newReferential = new Referential(repositoryName, repositoryIri, nameInRepository, iriInRepository);

        entity.setReferential(newReferential);
        contextFormViewController.submitImpl();
        entity.setContext(contextFormViewController.getContext());
    }

    @Override
    protected void cancelImpl() {
        contextFormViewController.cancelImpl();
    }
}
