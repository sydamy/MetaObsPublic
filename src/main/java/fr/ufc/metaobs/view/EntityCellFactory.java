package fr.ufc.metaobs.view;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.model.Entity;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.Optional;

public class EntityCellFactory implements Callback<ListView<Entity>, ListCell<Entity>> {

    private final MainViewController mainViewController;

    public EntityCellFactory(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    @Override
    public ListCell<Entity> call(ListView<Entity> entityListView) {
        ListCell<Entity> entityCell = new EntityCell(mainViewController);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addCharacteristicItem = new MenuItem("Add characteristic");
        addCharacteristicItem.setOnAction(actionEvent -> {
            Entity item = entityCell.getItem();
            mainViewController.addCharacteristicForm(item);
        });
        MenuItem addObservationItem = new MenuItem("Add observation");
        addObservationItem.setOnAction(actionEvent -> {
            Entity item = entityCell.getItem();
            mainViewController.addObservationForm(item);
        });
        MenuItem editItem = new MenuItem();
        editItem.setMnemonicParsing(false);
        editItem.textProperty().bind(Bindings.format("Edit \"%s\"", Bindings.selectString(entityCell.itemProperty(), "name")));
        editItem.setOnAction(event -> {
            Entity item = entityCell.getItem();
            mainViewController.editEntityForm(item);
        });
        MenuItem deleteItem = new MenuItem();
        deleteItem.setMnemonicParsing(false);
        deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", Bindings.selectString(entityCell.itemProperty(), "name")));
        deleteItem.setOnAction(event -> {
            Entity item = entityCell.getItem();
            String contentText = "Are you sure to delete this entity ?";
            if (Controller.getInstance().getProject().entityIsReferenced(item)) {
                contentText = "This entity is referenced by other entities, those entities will also be deleted. " + contentText;
            }
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setResizable(true);
            dialog.setHeaderText(deleteItem.getText());
            dialog.setContentText(contentText);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (result.get().getButtonData().isDefaultButton()) {
                    Controller.getInstance().getProject().removeEntity(item);
                    Controller.getInstance().saveProject();
                }
            }
        });
        contextMenu.getItems().add(addCharacteristicItem);
        contextMenu.getItems().add(addObservationItem);
        contextMenu.getItems().add(editItem);
        contextMenu.getItems().add(deleteItem);
        entityCell.emptyProperty().addListener(((observableValue, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                entityCell.setContextMenu(null);
            } else {
                entityCell.setContextMenu(contextMenu);
            }
        }));
        return entityCell;
    }

}
