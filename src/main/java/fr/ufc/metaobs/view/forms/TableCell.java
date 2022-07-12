package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.model.export.Column;
import fr.ufc.metaobs.model.export.ForeignKey;
import fr.ufc.metaobs.model.export.PrimaryKey;
import fr.ufc.metaobs.model.export.Table;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TableCell extends ListCell<Table> {

    public static final int PREF_HEIGHT_COLUMN = 24;

    private FXMLLoader fxmlLoader;

    @FXML
    private CheckBox nameCheckBox;

    @FXML
    private ListView<Column> columnsListView;

    @FXML
    public void initialize() {
        columnsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Column column, boolean empty) {
                super.updateItem(column, empty);
                if (empty || column == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(column.getName() + " " + column.getType());
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        });
    }

    @Override
    protected void updateItem(Table table, boolean empty) {
        Table oldTable = getItem();
        if (oldTable != null) {
            nameCheckBox.selectedProperty().unbindBidirectional(oldTable.selectedProperty());
            nameCheckBox.disableProperty().unbindBidirectional(oldTable.disabledProperty());
        }
        super.updateItem(table, empty);
        if (empty || table == null) {
            setText(null);
            setContentDisplay(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(TableCell.class.getResource("/fr/ufc/metaobs/edit/table.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setText(null);
            setGraphic(fxmlLoader.getRoot());

            nameCheckBox.setText(table.getName());
            nameCheckBox.selectedProperty().bindBidirectional(table.selectedProperty());
            nameCheckBox.disableProperty().bind(table.disabledProperty());
            List<Column> columns = table.getColumns().values().stream()
                    .filter(column -> !(column instanceof PrimaryKey) && !(column instanceof ForeignKey))
                    .collect(Collectors.toList());
            ObservableList<Column> observableList = FXCollections.observableList(columns);
            columnsListView.setItems(observableList);
            columnsListView.prefHeightProperty().bind(
                    Bindings.size(observableList)
                            .multiply(PREF_HEIGHT_COLUMN)
                            .add(2)
            );
        }
    }

}
