package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.ContextActor;
import fr.ufc.metaobs.model.Field;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.apache.jena.ontology.OntClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubContextActorFormViewController extends AbstractFormViewController {

    private final ObjectProperty<ContextActor> refContextActor;
    private ContextActor contextActor;
    private SubContextActorFormViewController refSubContextActorFormViewController;

    @FXML
    private ComboBox<String> typeActorComboBox;

    @FXML
    private MenuButton characteristicsActorMenuButton;

    @FXML
    private TextField tableActorNameTextField;

    @FXML
    private ListView<Field> fieldsActorListView;

    @FXML
    private Button addActorButton;

    @FXML
    private Button removeActorButton;

    @FXML
    private BorderPane nestedContextActorPane;

    public SubContextActorFormViewController(ContextActor contextActor) {
        this.contextActor = contextActor;
        this.refContextActor = new SimpleObjectProperty<>();
    }

    @FXML
    public void initialize() {
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();
        List<OntClass> typesActor = owlHandler.getClassesWithTag(Tags.TypeActor.toString());
        FormViewControllerUtils.populateComboBox(typeActorComboBox, typesActor.get(0));

        List<OntClass> characteristicsActor = owlHandler.getClassesWithTag(Tags.CharacteristicActor.toString());
        List<Field> actorPropertiesList = new ArrayList<>();
        if (contextActor != null) {
            actorPropertiesList.addAll(contextActor.getPropertiesList());
        }
        FormViewControllerUtils.populateMenuButton(characteristicsActorMenuButton, fieldsActorListView, actorPropertiesList, characteristicsActor.get(0));

        removeActorButton.visibleProperty().bind(refContextActor.isNotNull());

        initValues();
    }

    private void initValues() {
        if (contextActor != null) {
            typeActorComboBox.setValue(contextActor.getTypeActor());
            typeActorComboBox.getSelectionModel().select(contextActor.getTypeActor());

            if (contextActor.getRefContextActor() != null) {
                addActor(contextActor.getRefContextActor());
            }

            tableActorNameTextField.setText(contextActor.getName());
            fieldsActorListView.getItems().addAll(contextActor.getPropertiesList());
        }
    }

    private void addActor(ContextActor actor) {
        FXMLLoader loader = new FXMLLoader(SubContextActorFormViewController.class.getResource("/fr/ufc/metaobs/edit/sub_context_actor.fxml"));
        refContextActor.set(actor);
        refSubContextActorFormViewController = new SubContextActorFormViewController(refContextActor.get());
        loader.setController(refSubContextActorFormViewController);

        try {
            nestedContextActorPane.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void addActor() {
        addActor(new ContextActor(""));
    }

    @FXML
    public void removeActor() {
        refContextActor.set(null);
        nestedContextActorPane.setCenter(null);
    }

    public ContextActor getContextActor() {
        return contextActor;
    }

    @Override
    protected boolean checkRequirements() {
        boolean res = typeActorComboBox.getValue() != null || tableActorNameTextField.getText().isBlank();
        res = res && ((!tableActorNameTextField.getText().isBlank() && fieldsActorListView.getItems().size() > 0)
                || (tableActorNameTextField.getText().isBlank() && fieldsActorListView.getItems().size() == 0));
        res = res && FormViewControllerUtils.checkFields(fieldsActorListView.getItems());
        if (refContextActor.isNotNull().get() && refSubContextActorFormViewController != null) {
            res = res && refSubContextActorFormViewController.checkRequirements();
        }
        return res;
    }

    @Override
    protected void submitImpl() {
        if (contextActor == null) {
            contextActor = new ContextActor("");
        }
        contextActor.setTypeActor(typeActorComboBox.getValue());
        contextActor.setName(tableActorNameTextField.getText());
        contextActor.clearProperties();
        for (Field field : fieldsActorListView.getItems()) {
            contextActor.putProperty(field.getOriginalName(), field.getName(), field.getTypeSize().toString());
        }
        if (refContextActor.isNotNull().get() && refSubContextActorFormViewController != null) {
            refSubContextActorFormViewController.submitImpl();
            contextActor.setRefContextActor(refSubContextActorFormViewController.getContextActor());
        }
    }

    @Override
    protected void cancelImpl() {

    }

}
