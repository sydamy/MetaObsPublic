package fr.ufc.metaobs.view;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.model.Entity;
import fr.ufc.metaobs.model.Observation;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.Optional;

public class ObservationCellFactory implements Callback<ListView<Observation>, ListCell<Observation>> {

    private final Entity entity;
    private final MainViewController mainViewController;

    public ObservationCellFactory(MainViewController mainViewController, Entity entity) {
        this.mainViewController = mainViewController;
        this.entity = entity;
    }

    @Override
    public ListCell<Observation> call(ListView<Observation> observationListView) {
        ListCell<Observation> observationCell = new ObservationCell();
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem();
        editItem.setMnemonicParsing(false);
        editItem.textProperty().bind(Bindings.format("Edit \"%s\" from \"%s\"", Bindings.selectString(observationCell.itemProperty(), "name"), entity.nameProperty()));
        editItem.setOnAction(event -> {
            Observation item = observationCell.getItem();
            mainViewController.editObservationForm(entity, item);
        });
        MenuItem deleteItem = new MenuItem();
        deleteItem.setMnemonicParsing(false);
        deleteItem.textProperty().bind(Bindings.format("Delete \"%s\" from \"%s\"", Bindings.selectString(observationCell.itemProperty(), "name"), entity.nameProperty()));
        deleteItem.setOnAction(event -> {
            Observation item = observationCell.getItem();
            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.setResizable(true);
            dialog.setHeaderText(deleteItem.getText());
            dialog.setContentText("Are you sure to delete this observation ?");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (result.get().getButtonData().isDefaultButton()) {
                    Controller.getInstance().getProject().removeObservation(item);
                    Controller.getInstance().saveProject();
                }
            }
        });
        contextMenu.getItems().add(editItem);
        contextMenu.getItems().add(deleteItem);
        observationCell.emptyProperty().addListener(((observableValue, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                observationCell.setContextMenu(null);
            } else {
                observationCell.setContextMenu(contextMenu);
            }
        }));
        return observationCell;
    }

}
