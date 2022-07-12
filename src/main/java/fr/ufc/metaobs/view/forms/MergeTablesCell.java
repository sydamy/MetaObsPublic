package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.model.export.Column;
import fr.ufc.metaobs.model.export.MergeTables;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;

public class MergeTablesCell extends ListCell<MergeTables> {

    public static final int PREF_HEIGHT_COLUMN = 24;

    private FXMLLoader fxmlLoader;

    @FXML
    private CheckBox nameCheckBox;

    @FXML
    private ListView<Column> columnsListView;

    @Override
    protected void updateItem(MergeTables mergeTables, boolean empty) {
        MergeTables oldMergeTables = getItem();
        if (oldMergeTables != null) {
            nameCheckBox.selectedProperty().unbindBidirectional(oldMergeTables.selectedProperty());
        }
        super.updateItem(mergeTables, empty);
        if (empty || mergeTables == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(MergeTablesCell.class.getResource("/fr/ufc/metaobs/edit/merge_tables.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setText(null);
            setGraphic(fxmlLoader.getRoot());

            nameCheckBox.textProperty().bind(mergeTables.newTableNameProperty());
            nameCheckBox.selectedProperty().bindBidirectional(mergeTables.selectedProperty());
            columnsListView.itemsProperty().bind(mergeTables.newColumnsProperty());
            columnsListView.prefHeightProperty().bind(
                    Bindings.size(mergeTables.newColumnsProperty())
                            .multiply(PREF_HEIGHT_COLUMN)
                            .add(2)
            );
        }
    }
}
