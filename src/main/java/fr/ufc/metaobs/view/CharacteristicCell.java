package fr.ufc.metaobs.view;

import fr.ufc.metaobs.model.Characteristic;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class CharacteristicCell extends ListCell<Characteristic> {

    private FXMLLoader fxmlLoader;

    @FXML
    private Label characteristicNameLabel;

    @FXML
    private Label characteristicMeasureLabel;

    @Override
    protected void updateItem(Characteristic characteristic, boolean empty) {
        super.updateItem(characteristic, empty);
        if (empty || characteristic == null) {
            setText(null);
            setContentDisplay(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(CharacteristicCell.class.getResource("/fr/ufc/metaobs/view/characteristic.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setText(null);
            setGraphic(fxmlLoader.getRoot());

            characteristicNameLabel.textProperty().bind(characteristic.nameProperty());
            characteristicMeasureLabel.textProperty().bind(Bindings.concat("(", characteristic.measureProperty(), ")"));
        }
    }
}
