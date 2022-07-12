package fr.ufc.metaobs.view;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.model.MetadataInfo;
import fr.ufc.metaobs.model.Observatory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MetadataViewController {

    private final Observatory metadata;

    @FXML
    private GridPane metadataGridPane;

    public MetadataViewController(Observatory metadata) {
        this.metadata = metadata;
        this.metadata.setInfoOrders(Controller.getInstance().getOwlHandler());
    }

    @FXML
    public void initialize() {
        for (MetadataInfo info : metadata.getListInfos().sorted()) {
            //on affiche seulement les métadonnées non vides
            if (info.getValue() != null && !info.getValue().isBlank()) {
                Label label = new Label(info.getName());
                label.setFont(Font.font("System", FontWeight.BOLD, 13.0));
                TextArea valueTextArea = new TextArea(info.getValue());
                valueTextArea.setEditable(false);
                valueTextArea.setPrefRowCount(((int) info.getValue().lines().count()));
                metadataGridPane.addRow(metadataGridPane.getRowCount(), label, valueTextArea);
                metadataGridPane.getRowConstraints().add(new RowConstraints(10, valueTextArea.getPrefHeight(), 128));
            }
        }
    }

}
