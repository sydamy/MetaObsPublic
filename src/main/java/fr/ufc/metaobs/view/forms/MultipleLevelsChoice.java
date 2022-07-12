package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.handlers.OwlHandler;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.apache.jena.ontology.OntClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe héritant de VBox pour un choix à multiple niveaux et avec la possibilité d'avoir un choix simple (sélection depuis
 * la dernière ComboBox) ou non (chaque sélection sur la dernière ComboBox ajoute à un TextField le texte de l'entrée sélectionnée).
 */
public class MultipleLevelsChoice extends VBox {

    private final boolean simpleChoice;
    private final OwlHandler owlHandler;
    private final OntClass superClass;
    private final Map<String, OntClass> items;
    private final ComboBox<String>[] comboBoxes;
    private TextField textField;


    public MultipleLevelsChoice(boolean simpleChoice, OwlHandler owlHandler, OntClass ontClass) {
        super(5.0);
        this.simpleChoice = simpleChoice;
        this.owlHandler = owlHandler;
        superClass = ontClass;
        items = new HashMap<>();
        int size = owlHandler.getSubclassLayers(superClass);
        comboBoxes = new ComboBox[size];
        owlHandler.getSubclassesTrans(ontClass, items);
        initComponents();
        initListeners();
    }

    /**
     * On crée les ComboBoxes et le TextField en fonction du type de choix et de la profondeur de la liste de choix.
     */
    private void initComponents() {
        if (comboBoxes.length == 1 && simpleChoice) {
            comboBoxes[0] = new ComboBox<>(FXCollections.observableArrayList(owlHandler.getSubclassesNames(superClass)));
            getChildren().add(comboBoxes[0]);
        } else {
            for (int i = 0; i < comboBoxes.length; i++) {
                if (i == 0) {
                    comboBoxes[i] = new ComboBox<>(FXCollections.observableArrayList(owlHandler.getSubclassesNames(superClass)));
                } else {
                    comboBoxes[i] = new ComboBox<>();
                }
                getChildren().add(comboBoxes[i]);
            }
        }
        if (!simpleChoice) {
            textField = new TextField();
            getChildren().add(textField);
        }
    }

    /**
     * On ajoute à chaque ComboBox un listener pour que le changement de sélection modifie les propositions de la ComboBox
     * suivante.
     */
    private void initListeners() {
        for (int i = 0; i < comboBoxes.length; i++) {
            int finalI = i;
            comboBoxes[i].setOnAction(actionEvent -> {
                String itemSelected = (String) ((ComboBox) actionEvent.getSource()).getSelectionModel().getSelectedItem();
                if (finalI == comboBoxes.length - 1) {
                    if (!simpleChoice && textField != null && itemSelected != null && !textField.getText().contains(itemSelected)) {
                        textField.setText(textField.getText() + itemSelected + "; ");
                    }
                } else {
                    OntClass ontCLass = items.get(itemSelected);
                    List<String> subClassesNames = owlHandler.getSubclassesNames(ontCLass);
                    comboBoxes[finalI + 1].getItems().setAll(subClassesNames);
                }
            });
        }
    }

    public void fillValue(String value) {
        if (value != null && !value.isEmpty()) {
            if (comboBoxes.length == 1 && simpleChoice) {
                comboBoxes[0].getSelectionModel().select(value);
            } else {
                textField.setText(value);
            }
        }
    }

    /**
     * Retourne la valeur en fonction du type de choix.
     *
     * @param node inutile ici, mais nécessaire pour la signature
     * @return une chaîne de caractères représentant la valeur actuelle
     */
    public String getValue(Node node) {
        String res;
        if (simpleChoice) {
            res = comboBoxes[comboBoxes.length - 1].getValue();
        } else {
            res = textField.getText();
        }
        return res;
    }


}
