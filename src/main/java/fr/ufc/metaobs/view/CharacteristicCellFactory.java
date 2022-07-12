package fr.ufc.metaobs.view;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.model.Characteristic;
import fr.ufc.metaobs.model.Entity;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.Optional;

public class CharacteristicCellFactory implements Callback<ListView<Characteristic>, ListCell<Characteristic>> {

    private final MainViewController mainViewController;
    private final Entity entity;

    public CharacteristicCellFactory(MainViewController mainViewController, Entity entity) {
        this.mainViewController = mainViewController;
        this.entity = entity;
    }

    @Override
    public ListCell<Characteristic> call(ListView<Characteristic> characteristicListView) {
        CharacteristicCell characteristicCell = new CharacteristicCell();
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem();
        editItem.setMnemonicParsing(false);
        editItem.textProperty().bind(Bindings.format("Edit \"%s\" from \"%s\"", Bindings.selectString(characteristicCell.itemProperty(), "name"), entity.nameProperty()));
        editItem.setOnAction(event -> {
            Characteristic item = characteristicCell.getItem();
            mainViewController.editCharacteristicForm(entity, item);
        });
        MenuItem deleteItem = new MenuItem();
        deleteItem.setMnemonicParsing(false);
        deleteItem.textProperty().bind(Bindings.format("Delete \"%s\" from \"%s\"", Bindings.selectString(characteristicCell.itemProperty(), "name"), entity.nameProperty()));
        deleteItem.setOnAction(event -> {
            Characteristic item = characteristicCell.getItem();
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setResizable(true);
            dialog.setHeaderText(deleteItem.getText());
            dialog.setContentText("Are you sure to delete this characteristic ?");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (result.get().getButtonData().isDefaultButton()) {
                    Controller.getInstance().getProject().removeCharacteristic(item);
                    Controller.getInstance().saveProject();
                }
            }
        });
        contextMenu.getItems().add(editItem);
        contextMenu.getItems().add(deleteItem);
        characteristicCell.emptyProperty().addListener(((observableValue, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                characteristicCell.setContextMenu(null);
            } else {
                characteristicCell.setContextMenu(contextMenu);
            }
        }));
        return characteristicCell;
    }

}
