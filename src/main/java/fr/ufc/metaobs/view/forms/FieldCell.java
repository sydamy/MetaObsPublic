package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.Field;
import fr.ufc.metaobs.model.FieldTypeSize;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.apache.jena.ontology.OntClass;

import java.io.IOException;
import java.util.List;

/**
 * Classe héritant de ListCell pour une cellule de ListView, contenant un Field (type + taille).
 *
 * @see javafx.scene.control.ListCell
 */
public class FieldCell extends ListCell<Field> {

    private Field field;
    private ToggleGroup toggleGroup;
    private FXMLLoader fxmlLoader;

    @FXML
    private Label fieldNameLabel;

    @FXML
    private TextField renameTextField;

    @FXML
    private MenuButton fieldTypeMenuButton;

    @FXML
    private Button removeFieldButton;

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();
        //on ajoute un Listener au groupe de sélection pour mettre à jour le texte du MenuButton et
        toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            //si aucun RadioButton/Toggle n'est sélectionné
            if (t1 == null) {
                fieldTypeMenuButton.setText("Field type");
            }
            //si un RadioButton est sélectionné et que le champ field n'est pas null, on peut mettre à jour
            if (field != null && t1 != null) {
                field.setTypeSize((FieldTypeSize) t1.getUserData());
                fieldTypeMenuButton.setText(t1.getUserData().toString());
            }
        });
        //on récupère les différentes OntClass des types de champs de base de données
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();
        List<OntClass> fieldsTypesBD = owlHandler.getClassesWithTag(Tags.FieldsTypes.toString());
        List<OntClass> types = owlHandler.getSubclasses(fieldsTypesBD.get(0));
        for (OntClass type : types) {
            String typeStr = owlHandler.getLocalName(type);
            boolean hasFieldSize = false;
            if ("VARCHAR".equals(typeStr)) {
                hasFieldSize = true;
            }
            CustomRadioMenuItem radioMenuItem = new CustomRadioMenuItem(toggleGroup, typeStr, hasFieldSize);
            //on ajoute un Listener au champ de saisie pour la taille afin de mettre à jour la taille et le texte du MenuButton
            if (hasFieldSize) {
                radioMenuItem.getOptionalFieldSize().textProperty().addListener((observableValue, oldValue, newValue) -> {
                    //on sélectionne d'abord le RadioButton approprié
                    radioMenuItem.getRadioButton().setSelected(true);
                    //on met à jour la taille
                    field.getTypeSize().setSize(newValue);
                    //on met à jour le texte du MenuButton en fonction du RadioButton sélectionné, c'est pourquoi il
                    //était nécessaire de le sélectionner avant
                    fieldTypeMenuButton.setText(toggleGroup.getSelectedToggle().getUserData().toString());
                });
            }
            fieldTypeMenuButton.getItems().add(radioMenuItem);
        }
    }

    @FXML
    public void removeFieldFromListView() {
        if (getItem() != null) {
            getListView().getItems().remove(getItem());
        }
    }

    @Override
    protected void updateItem(Field field, boolean empty) {
        Field oldField = getItem();
        //si la cellule contenait déjà un item, il faut unbind les prorpiétés bidirectionnelles et vider la sélection
        //pour éviter des problèmes d'actualisation à cause des mécanismes de la ListView
        if (oldField != null) {
            renameTextField.textProperty().unbindBidirectional(oldField.newNameProperty());
            toggleGroup.selectToggle(null);
        }
        super.updateItem(field, empty);
        if (empty || field == null) {
            setText(null);
            setContentDisplay(null);
            setGraphic(null);
        } else {
            this.field = field;
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(FieldCell.class.getResource("/fr/ufc/metaobs/edit/field.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setText(null);
            setGraphic(fxmlLoader.getRoot());

            fieldNameLabel.textProperty().bind(this.field.originalNameProperty());
            renameTextField.textProperty().bindBidirectional(this.field.newNameProperty());
            removeFieldButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> "Other".equals(this.field.getOriginalName())));
            //on sélectionne le radiobutton s'il correspond déjà au type de field
            for (Toggle toggle : toggleGroup.getToggles()) {
                if (this.field.getTypeSize() != null &&
                        ((FieldTypeSize) toggle.getUserData()).getType().equals(this.field.getTypeSize().getType())) {
                    ((FieldTypeSize) toggle.getUserData()).setSize(this.field.getTypeSize().getSize());
                    toggle.setSelected(true);
                }
            }
        }
    }
}
