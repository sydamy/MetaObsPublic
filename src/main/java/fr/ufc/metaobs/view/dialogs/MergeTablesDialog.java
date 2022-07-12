package fr.ufc.metaobs.view.dialogs;

import fr.ufc.metaobs.model.export.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MergeTablesDialog extends Dialog<MergeTables> {

    private final MergeTables tables;

    private final Map<Column, List<Column>> mapMerge;

    @FXML
    private TextField newTableNameTextField;

    @FXML
    private ListView<Table> originalTablesListView;

    @FXML
    private ListView<Column> originalColumnsListView;

    @FXML
    private ListView<Column> mergedColumnsListView;


    public MergeTablesDialog(MergeTables tables) {
        super();
        setResizable(true);
        this.tables = tables;
        mapMerge = new HashMap<>();
        setTitle("Merge tables");
        setResizable(true);
        FXMLLoader fxmlLoader = new FXMLLoader(MergeTablesDialog.class.getResource("/fr/ufc/metaobs/edit/merge_tables_dialog.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            getDialogPane().setContent(fxmlLoader.getRoot());

            setResultConverter(buttonType -> {
                if (buttonType.getButtonData().isDefaultButton()) {
                    //on ajoute les colonnes originales qui restent malgré la fusion
                    this.tables.getNewColumns().addAll(originalColumnsListView.getItems());
                    //on ajoute les colonnes fusionnées
                    this.tables.getNewColumns().addAll(mergedColumnsListView.getItems());
                    return this.tables;
                }
                return null;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        originalTablesListView.setCellFactory(tableListView -> new ListCell<>() {
            @Override
            protected void updateItem(Table table, boolean empty) {
                super.updateItem(table, empty);
                if (empty || table == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(null);
                    setText(table.getName());
                }
            }
        });
        originalColumnsListView.setCellFactory(columnListView -> new ListCell<>() {
            private CheckBox checkBox;

            @Override
            protected void updateItem(Column column, boolean empty) {
                Column oldColumn = getItem();
                if (oldColumn != null) {
                    checkBox.selectedProperty().unbindBidirectional(oldColumn.selectedProperty());
                }
                super.updateItem(column, empty);
                if (empty || column == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    checkBox = new CheckBox(column.getName());
                    checkBox.selectedProperty().bindBidirectional(column.selectedProperty());
                    setGraphic(checkBox);
                }
            }
        });
        mergedColumnsListView.setCellFactory(columnListView -> new ListCell<>() {
            private CheckBox checkBox;
            private TextField textField;

            @Override
            protected void updateItem(Column column, boolean empty) {
                Column oldColumn = getItem();
                if (oldColumn != null) {
                    checkBox.selectedProperty().unbindBidirectional(oldColumn.selectedProperty());
                    textField.textProperty().unbindBidirectional(oldColumn.nameProperty());
                }
                super.updateItem(column, empty);
                if (empty || column == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    checkBox = new CheckBox();
                    textField = new TextField();
                    textField.setPromptText("New column name");
                    checkBox.selectedProperty().bindBidirectional(column.selectedProperty());
                    textField.textProperty().bindBidirectional(column.nameProperty());
                    HBox root = new HBox(5, checkBox, textField);
                    root.setFillHeight(true);
                    root.setAlignment(Pos.CENTER_LEFT);
                    setText(null);
                    setGraphic(root);
                }
            }
        });

        newTableNameTextField.textProperty().bindBidirectional(tables.newTableNameProperty());
        originalTablesListView.itemsProperty().bind(tables.tablesProperty());
        originalColumnsListView.getItems().addAll(tables.getTables().stream()
                .map(table -> table.getColumns().values())
                .reduce(new ArrayList<>(), ((columns, columns2) -> {
                    columns.addAll(columns2);
                    // on supprime les colonnes qui sont des clés primaires ou étrangères car elles ne sont pas fusionnables
                    columns.removeIf(column -> column instanceof PrimaryKey || column instanceof ForeignKey);
                    return columns;
                }))
        );
    }

    @FXML
    private void unmergeColumns() {
        List<Column> temp = mergedColumnsListView.getItems().stream()
                .filter(Column::isSelected)
                .collect(Collectors.toList());
        for (Column columnTemp : temp) {
            mergedColumnsListView.getItems().remove(columnTemp);
            originalColumnsListView.getItems().addAll(mapMerge.get(columnTemp));
            mapMerge.remove(columnTemp);
        }
    }

    @FXML
    private void mergeColumns() {
        List<Column> temp = originalColumnsListView.getItems().stream()
                .filter(Column::isSelected)
                .collect(Collectors.toList());
        if (temp.size() > 0) {
            String name = temp.get(0).getName();
            String type = temp.get(0).getType();
            if (temp.stream().allMatch(column -> column.getType().equals(type))) {
                originalColumnsListView.getItems().removeAll(temp);
                Column newColumn = new Column(name, type, false);
                mapMerge.put(newColumn, temp);
                mergedColumnsListView.getItems().add(newColumn);
            }
        }
    }
}
