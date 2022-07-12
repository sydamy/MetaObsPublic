package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.FieldsTypes;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.exceptions.NoOntClassException;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.MetadataInfo;
import fr.ufc.metaobs.model.Observatory;
import fr.ufc.metaobs.utils.DateUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Classe pour l'édition des métadonnées de projet, hérite de AbstractFormViewController puisque c'est un formulaire.
 *
 * @see AbstractFormViewController
 */
public class MetadataFormViewController extends AbstractFormViewController {

    private final OwlHandler owlHandler;
    private final Observatory metadata;
    private final Map<MetadataInfo, Node> nodeValuesMap;
    private final Map<MetadataInfo, Function<Node, String>> getValuesComputeMap;


    @FXML
    private GridPane metadataGridPane;

    public MetadataFormViewController(Observatory metadata) {
        super();
        this.metadata = metadata;
        owlHandler = Controller.getInstance().getOwlHandler();
        nodeValuesMap = new TreeMap<>(Comparator.comparingInt(MetadataInfo::getOrder));
        getValuesComputeMap = new TreeMap<>(Comparator.comparingInt(MetadataInfo::getOrder));
    }

    @FXML
    public void initialize() throws NoOntClassException {
        metadataGridPane.setVgap(10.0);

        List<OntClass> metadataOntClasses = owlHandler.getClassesWithTag(Tags.Metadata_general.toString());
        if (metadataOntClasses.isEmpty()) {
            throw new NoOntClassException();
        }
        Map<OntClass, String> fieldTypes = new HashMap<>();
        SortedMap<Integer, OntClass> orders = new TreeMap<>();
        Map<OntClass, String> labels = new HashMap<>();
        owlHandler.getObsInfos(metadataOntClasses, fieldTypes, orders, labels);
        List<OntClass> observatoryList = owlHandler.getClassesWithTag(Tags.Observatory.toString());
        if (observatoryList.isEmpty()) {
            throw new NoOntClassException();
        }
        OntClass observatory = observatoryList.get(0);
        List<OntResource> simpleRelation = owlHandler.getRelationSimple(observatory);

        for (Map.Entry<Integer, OntClass> entry : orders.entrySet()) {
            OntClass field = entry.getValue();
            String fieldType = fieldTypes.get(field);
            FieldsTypes fieldTypeEnum = FieldsTypes.valueOf(fieldType);
            String labelText = labels.get(field);
            MetadataInfo metadataInfo = metadata.getInfo(labelText);
            String value = "";
            boolean simpleChoice;
            //si metadataInfo != null alors il a déjà été rempli pour le projet, on récupère la valeur actuelle
            if (metadataInfo != null) {
                value = metadataInfo.getValue();
            } else { //si metadataInfo est null, alors il n'a pas encore été rempli pour le projet
                metadataInfo = new MetadataInfo(labelText, field.getURI(), "");
                metadataInfo.setOrder(entry.getKey());
            }
            Node nodeValue = null;

            switch (fieldTypeEnum) {
                case TextField:
                    nodeValue = new TextField(value);
                    getValuesComputeMap.put(metadataInfo, node -> ((TextField) node).getText());
                    break;
                case TextArea:
                    nodeValue = new TextArea(value);
                    getValuesComputeMap.put(metadataInfo, node -> ((TextArea) node).getText());
                    break;
                case SpatialCoverage:
                    FXMLLoader loader = new FXMLLoader(MetadataFormViewController.class.getResource("/fr/ufc/metaobs/edit/spatial_coverage.fxml"));
                    try {
                        nodeValue = loader.load();
                        fillNodeValuesSpatialCoverage(nodeValue, value);
                        getValuesComputeMap.put(metadataInfo, this::getValuesSpatialCoverage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CreateDate:
                    String defaultValue = value.equals("") ? DateUtils.formatDate("-") : value;
                    nodeValue = new TextField(defaultValue);
                    ((TextField) nodeValue).setEditable(false);
                    getValuesComputeMap.put(metadataInfo, node -> ((TextField) node).getText());
                    break;
                case Enumeration:
                    List<String> list = owlHandler.getSubclassesNames(field);
                    simpleChoice = simpleRelation.contains(field);
                    FlowPane flowPane = new FlowPane(5.0, 10.0);
                    flowPane.setPadding(new Insets(10, 5, 10, 5));
                    flowPane.setRowValignment(VPos.CENTER);
                    //si le choix est simple, alors on utilise des RadioButton avec un seul ToggleGroup
                    if (simpleChoice) {
                        ToggleGroup toggleGroup = new ToggleGroup();
                        RadioButton[] radioButtons = new RadioButton[list.size()];
                        int cnt = 0;
                        for (String str : list) {
                            radioButtons[cnt] = new RadioButton(str);
                            radioButtons[cnt].setUserData(str);
                            if (value != null && value.equals(str)) {
                                radioButtons[cnt].setSelected(true);
                            }
                            cnt++;
                        }
                        toggleGroup.getToggles().setAll(radioButtons);
                        flowPane.getChildren().setAll(radioButtons);
                        getValuesComputeMap.put(metadataInfo, node ->
                                toggleGroup.getSelectedToggle() != null ?
                                        toggleGroup.getSelectedToggle().getUserData().toString()
                                        : null);
                    } else { //si le choix est multiple, on utilise des CheckBox
                        CheckBox[] checkBoxes = new CheckBox[list.size()];
                        String[] valueSplitted = null;
                        if (value != null && value.contains(";")) {
                            valueSplitted = value.split(";");
                        }
                        int cnt = 0;
                        for (String str : list) {
                            checkBoxes[cnt] = new CheckBox(str);
                            checkBoxes[cnt].setMnemonicParsing(false);
                            checkBoxes[cnt].setUserData(str);
                            if (valueSplitted != null) {
                                for (String check : valueSplitted) {
                                    if (str.equals(check.trim())) {
                                        checkBoxes[cnt].setSelected(true);
                                    }
                                }
                            }
                            cnt++;
                        }
                        flowPane.getChildren().setAll(checkBoxes);
                        getValuesComputeMap.put(metadataInfo, this::getValuesCheckBoxes);
                    }
                    nodeValue = flowPane;
                    break;
                case List:
                    simpleChoice = simpleRelation.contains(field);
                    nodeValue = new MultipleLevelsChoice(simpleChoice, owlHandler, field);
                    ((MultipleLevelsChoice) nodeValue).fillValue(value);
                    getValuesComputeMap.put(metadataInfo, ((MultipleLevelsChoice) nodeValue)::getValue);
                    break;
            }

            nodeValuesMap.put(metadataInfo, nodeValue);
            Label label = new Label(labelText);
            label.setLabelFor(nodeValue);
            label.setFont(Font.font("System", FontWeight.BOLD, 13.0));
            metadataGridPane.addRow(metadataGridPane.getRowCount(), label, nodeValue);
        }
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMinWidth(160.0);
        metadataGridPane.getColumnConstraints().add(columnConstraints);
    }

    /**
     * Retourne la concaténation des valeurs pour les CheckBoxes sélectionnées.
     *
     * @param parentCheckBoxes le noeud parent direct des CheckBoxes
     * @return une chaîne de caractéres représentant la concaténation des valeurs sélectionnées séparées par ';'
     */
    private String getValuesCheckBoxes(Node parentCheckBoxes) {
        StringBuilder res = new StringBuilder();
        FlowPane flowPane = ((FlowPane) parentCheckBoxes);
        List<Node> children = flowPane.getChildren();
        for (Node child : children) {
            CheckBox checkBox = (CheckBox) child;
            if (checkBox.isSelected()) {
                res.append(checkBox.getUserData().toString()).append("; ");
            }
        }
        return res.toString();
    }

    /**
     * Retourne la concaténation des valeurs pour la couverture spatiale.
     *
     * @param spatialCoverageNode le noeud de couverture spatiale, obtenu en chargeant le fichier spatial_coverage.fxml
     * @return la concaténation des valeurs pour la couverture spatiale
     */
    private String getValuesSpatialCoverage(Node spatialCoverageNode) {
        StringBuilder res = new StringBuilder();
        String[] valueSplitted = new String[4];
        //les enfants du GridPane sont 4 HBox
        for (Node child : ((GridPane) spatialCoverageNode).getChildren()) {
            HBox hBox = (HBox) child;
            //les enfants de hBox sont un Label et un TextField
            for (Node childChild : hBox.getChildren()) {
                if (childChild.getId() != null) {
                    switch (childChild.getId()) {
                        case "northTextField":
                            valueSplitted[0] = ((TextField) childChild).getText().trim();
                            break;
                        case "westTextField":
                            valueSplitted[1] = ((TextField) childChild).getText().trim();
                            break;
                        case "eastTextField":
                            valueSplitted[2] = ((TextField) childChild).getText().trim();
                            break;
                        case "southTextField":
                            valueSplitted[3] = ((TextField) childChild).getText().trim();
                            break;
                    }
                }
            }
        }
        for (String s : valueSplitted) {
            res.append(s).append(";");
        }
        //si les champs ne sont pas remplis, on retourne une chaîne vide
        String toString = res.toString();
        return toString.equals(";;;;") ? "" : toString;
    }

    /**
     * Remplit les valeurs pour la couverture spatiale.
     *
     * @param spatialCoverageNode le noeud de couverture spatiale, obtenu en chargeant le fichier spatial_coverage.fxml
     * @param value               la valeur concaténée de couverture spatiale
     */
    private void fillNodeValuesSpatialCoverage(Node spatialCoverageNode, String value) {
        //on ne remplit les champs de la couverture spatiale seulement si nécessaire
        if (!value.isEmpty() && value.contains(";")) {
            String[] valueSplitted = value.split(";");
            //les enfants du GridPane sont 4 HBox
            for (Node child : ((GridPane) spatialCoverageNode).getChildren()) {
                HBox hBox = (HBox) child;
                //les enfants de hBox sont un Label et un TextField
                for (Node childChild : hBox.getChildren()) {
                    if (childChild.getId() != null) {
                        switch (childChild.getId()) {
                            case "northTextField":
                                ((TextField) childChild).setText(valueSplitted[0]);
                                break;
                            case "westTextField":
                                ((TextField) childChild).setText(valueSplitted[1]);
                                break;
                            case "eastTextField":
                                ((TextField) childChild).setText(valueSplitted[2]);
                                break;
                            case "southTextField":
                                ((TextField) childChild).setText(valueSplitted[3]);
                                break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean checkRequirements() {
        return true;
    }

    /**
     * Sauvegarde les informations entrées dans le modèle.
     * Cette méthode sera appelée lors d'un appui sur le bouton Submit.
     *
     * @see AbstractFormViewController#submit()
     */
    @Override
    protected void submitImpl() {
        for (Map.Entry<MetadataInfo, Node> entry : nodeValuesMap.entrySet()) {
            MetadataInfo field = entry.getKey();
            Node node = entry.getValue();
            //on récupère la valeur grâce à la fonction enregistrée dans getValuesComputeMap
            if (getValuesComputeMap.get(field) != null) {
                field.setValue(getValuesComputeMap.get(field).apply(node));
            }
            //on ajoute le champ aux métadonnées s'il n'est pas déjà présent
            if (metadata.getInfo(field.getName()) == null && field.getValue() != null && !field.getValue().isBlank()) {
                metadata.addInfo(field);
            }
        }
    }

    /**
     * Ne fait rien
     * Cette méthode est appelée lors d'un appui sur le bouton Cancel.
     *
     * @see AbstractFormViewController#cancel()
     */
    @Override
    public void cancelImpl() {

    }

}
