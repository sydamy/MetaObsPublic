package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.ContextLocation;
import fr.ufc.metaobs.model.Field;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.apache.jena.ontology.OntClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubContextLocationFormViewController extends AbstractFormViewController {

    private final ObjectProperty<ContextLocation> refContextLocation;
    private ContextLocation contextLocation;
    private SubContextLocationFormViewController refSubContextLocationFormViewController;

    @FXML
    private MenuButton locationMenuButton;

    @FXML
    private Button addLocationButton;

    @FXML
    private Button removeLocationButton;

    @FXML
    private TextField tableLocationNameTextField;

    @FXML
    private ListView<Field> fieldsLocationListView;

    @FXML
    private BorderPane nestedContextLocationPane;

    public SubContextLocationFormViewController(ContextLocation contextLocation) {
        this.contextLocation = contextLocation;
        this.refContextLocation = new SimpleObjectProperty<>();
    }

    @FXML
    public void initialize() {
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();

        List<OntClass> locations = owlHandler.getClassesWithTag(Tags.Location.toString());
        List<Field> locationPropertiesList = new ArrayList<>();
        if (contextLocation != null) {
            locationPropertiesList.addAll(contextLocation.getPropertiesList());
        }

        FormViewControllerUtils.populateMenuButton(locationMenuButton, fieldsLocationListView, locationPropertiesList, locations.get(0));

        removeLocationButton.visibleProperty().bind(refContextLocation.isNotNull());

        initValues();
    }

    private void initValues() {
        if (contextLocation != null) {
            tableLocationNameTextField.setText(contextLocation.getName());
            fieldsLocationListView.getItems().addAll(contextLocation.getPropertiesList());

            if (contextLocation.getRefLocation() != null) {
                addLocation(contextLocation.getRefLocation());
            }
        }
    }

    private void addLocation(ContextLocation location) {
        FXMLLoader loader = new FXMLLoader(SubContextActorFormViewController.class.getResource("/fr/ufc/metaobs/edit/sub_context_location.fxml"));
        refContextLocation.set(location);
        refSubContextLocationFormViewController = new SubContextLocationFormViewController(refContextLocation.get());
        loader.setController(refSubContextLocationFormViewController);

        try {
            nestedContextLocationPane.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void addLocation() {
        addLocation(new ContextLocation(""));
    }

    @FXML
    public void removeLocation() {
        refContextLocation.set(null);
        nestedContextLocationPane.setCenter(null);
    }

    public ContextLocation getContextLocation() {
        return contextLocation;
    }

    @Override
    protected boolean checkRequirements() {
        boolean res = ((!tableLocationNameTextField.getText().isBlank() && fieldsLocationListView.getItems().size() > 0)
                || (tableLocationNameTextField.getText().isBlank() && fieldsLocationListView.getItems().size() == 0));
        res = res && FormViewControllerUtils.checkFields(fieldsLocationListView.getItems());
        if (refContextLocation.isNotNull().get() && refSubContextLocationFormViewController != null) {
            res = res && refSubContextLocationFormViewController.checkRequirements();
        }
        return res;
    }

    @Override
    protected void submitImpl() {
        if (contextLocation == null) {
            contextLocation = new ContextLocation("");
        }
        contextLocation.setName(tableLocationNameTextField.getText());
        contextLocation.clearProperties();
        for (Field field : fieldsLocationListView.getItems()) {
            contextLocation.putProperty(field.getOriginalName(), field.getName(), field.getTypeSize().toString());
        }
        if (refContextLocation.isNotNull().get() && refSubContextLocationFormViewController != null) {
            refSubContextLocationFormViewController.submitImpl();
            contextLocation.setRefLocation(refSubContextLocationFormViewController.getContextLocation());
        }
    }

    @Override
    protected void cancelImpl() {

    }
}
