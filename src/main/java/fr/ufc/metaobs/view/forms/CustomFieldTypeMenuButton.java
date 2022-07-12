package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.exceptions.NoOntClassException;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.FieldTypeSize;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.apache.jena.ontology.OntClass;

import java.util.List;

public class CustomFieldTypeMenuButton extends MenuButton {

    private final ToggleGroup toggleGroup;


    public CustomFieldTypeMenuButton() throws NoOntClassException {
        super("Field type");
        toggleGroup = new ToggleGroup();
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();
        List<OntClass> fieldsTypesBDList = owlHandler.getClassesWithTag(Tags.FieldsTypes.toString());
        if (fieldsTypesBDList.isEmpty()) {
            throw new NoOntClassException();
        }
        OntClass fieldsTypesBD = fieldsTypesBDList.get(0);
        List<String> types = owlHandler.getSubclassesNames(fieldsTypesBD);
        for (String typeStr : types) {
            boolean hasFieldSize = false;
            if ("VARCHAR".equals(typeStr)) {
                hasFieldSize = true;
            }
            CustomRadioMenuItem radioMenuItem = new CustomRadioMenuItem(toggleGroup, typeStr, hasFieldSize);
            if (hasFieldSize) {
                radioMenuItem.getOptionalFieldSize().textProperty().addListener((observableValue, oldValue, newValue) -> {
                    //on sélectionne d'abord le RadioButton approprié
                    radioMenuItem.getRadioButton().setSelected(true);
                    //on met à jour le texte du MenuButton en fonction du RadioButton sélectionné, c'est pourquoi il
                    //était nécessaire de le sélectionner avant
                    setText(toggleGroup.getSelectedToggle().getUserData().toString());
                });
            }
            getItems().add(radioMenuItem);
        }
        toggleGroup.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                setText("Field type");
            } else {
                setText(newValue.getUserData().toString());
            }
        });
    }

    public void selectTypeSize(String typeSize) {
        String type = typeSize;
        String size = null;
        if (typeSize.contains("(")) {
            int indexBracket = typeSize.indexOf('(');
            type = typeSize.substring(0, indexBracket);
            size = typeSize.substring(indexBracket + 1, typeSize.indexOf(')'));
        }
        selectTypeSize(type, size);
    }

    public void selectTypeSize(String type, String size) {
        for (Toggle toggle : toggleGroup.getToggles()) {
            if (type.equals(((FieldTypeSize) toggle.getUserData()).getType())) {
                toggle.setSelected(true);
                if (size != null) {
                    ((FieldTypeSize) toggle.getUserData()).setSize(size);
                }
            }
        }
    }

    public ToggleGroup getToggleGroup() {
        return toggleGroup;
    }
}
