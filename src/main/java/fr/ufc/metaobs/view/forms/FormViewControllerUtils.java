package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.Field;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.apache.jena.ontology.OntClass;

import java.util.List;

public class FormViewControllerUtils {

    /**
     * Vérifie que tous les Field d'une liste sont correctement remplis.
     *
     * @param list une list de Field à vérifier
     * @return un booléen, vrai si tout est correct, faux sinon
     */
    public static boolean checkFields(ObservableList<Field> list) {
        boolean res = true;
        for (Field field : list) {
            res = res && field.getTypeSize() != null;
            if (field.getTypeSize() != null && "VARCHAR".equals(field.getTypeSize().getType())) {
                res = res && !field.getTypeSize().getSize().isBlank();
            }
        }
        return res;
    }

    public static void populateComboBox(ComboBox<String> comboBox, OntClass superClass) {
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();
        comboBox.getItems().add(null);
        for (OntClass subClass : owlHandler.getSubclasses(superClass)) {
            comboBox.getItems().add(owlHandler.getLocalName(subClass));
        }
    }

    /**
     * Peuple un MenuButton
     *
     * @param menuButton le MenuButton à peupler
     * @param listView   la ListView qui sera peuplée lors d'un clic sur un CheckMenuItem
     * @param superClass la super classe de l'ontologie
     */
    public static void populateMenuButton(MenuButton menuButton, ListView<Field> listView, List<Field> fieldList, OntClass superClass) {
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();
        //on peuple le MenuButton qu'avec les sous-classes de superClass pour éviter de peupler avec un menu "Actor" ou "Location"
        for (OntClass subClass : owlHandler.getSubclasses(superClass)) {
            populateMenuButtonRecursive(menuButton, null, listView, fieldList, owlHandler, subClass);
        }
        MenuItem otherMenuItem = new MenuItem("Other");
        otherMenuItem.setOnAction(actionEvent -> {
            Field otherField = new Field("Other");
            listView.getItems().add(otherField);
        });
        menuButton.getItems().add(otherMenuItem);
    }

    /**
     * Peuple un MenuButton de façon récursive
     *
     * @param menuButton le MenuButton à peupler
     * @param menu       le menu (donc sous-menu) actuel, null pour ajouter directement au MenuButton
     * @param listView   la ListView qui sera peuplée lors d'un clic sur un CheckMenuItem
     * @param owlHandler le gestionnaire d'owl pour récupérer les sous-classes
     * @param superClass la super classe de l'ontologie
     */
    public static void populateMenuButtonRecursive(MenuButton menuButton, Menu menu, ListView<Field> listView,
                                                   List<Field> fieldList, OwlHandler owlHandler, OntClass superClass) {
        List<OntClass> subClasses = owlHandler.getSubclasses(superClass);
        String menuName = owlHandler.getLocalName(superClass);
        //si la liste subClasses n'est pas vide, alors on ajoute un Menu
        if (!subClasses.isEmpty()) {
            Menu newMenu = new Menu(menuName);
            if (menu == null) {
                menuButton.getItems().add(newMenu);
            } else {
                menu.getItems().add(newMenu);
            }
            for (OntClass subClass : subClasses) {
                populateMenuButtonRecursive(menuButton, newMenu, listView, fieldList, owlHandler, subClass);
            }
            //si la liste subClasses est vide, alors on ajoute un CheckMenuItem
        } else {
            CheckMenuItem menuItem = new CheckMenuItem(menuName);
            Field field = new Field(menuName);
            for (Field field1 : fieldList) {
                if (field1.getOriginalName().equals(menuName)) {
                    menuItem.setSelected(true);
                }
            }
            menuItem.setOnAction(actionEvent -> {
                boolean isSelected = menuItem.isSelected();
                if (isSelected) {
                    listView.getItems().add(field);
                } else {
                    listView.getItems().removeIf(fieldl -> fieldl.getOriginalName().equals(field.getOriginalName()));
                }
            });
            if (menu == null) {
                menuButton.getItems().add(menuItem);
            } else {
                menu.getItems().add(menuItem);
            }
        }
    }

}
