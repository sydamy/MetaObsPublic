package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.model.export.Column;
import fr.ufc.metaobs.model.export.Database;
import fr.ufc.metaobs.model.export.MergeTables;
import fr.ufc.metaobs.model.export.Table;
import fr.ufc.metaobs.view.dialogs.MergeTablesDialog;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MergeTablesFormViewController extends AbstractFormViewController {

    private Database database;

    @FXML
    private CheckBox mergeCheckBox;

    @FXML
    private ListView<Table> contextActorListView;

    @FXML
    private ListView<Table> contextLocationListView;

    @FXML
    private ListView<MergeTables> mergeTablesListView;

    public MergeTablesFormViewController(Database database) {
        this.database = database;
    }

    @FXML
    public void initialize() {
        contextActorListView.setCellFactory(tableListView -> new TableCell());
        contextActorListView.getItems().addAll(database.getTablesWithTag("ContextActor"));
        contextLocationListView.setCellFactory(tableListView -> new TableCell());
        contextLocationListView.getItems().addAll(database.getTablesWithTag("ContextLocation"));
        mergeTablesListView.setCellFactory(mergeTablesListView1 -> new MergeTablesCell());
    }


    @FXML
    public void addSelectionToMerge() {
        MergeTables mergeTablesActor = new MergeTables(contextActorListView.getItems().filtered(Table::isSelected));
        if (mergeTablesActor.getTables().size() > 0) {
            MergeTablesDialog dialogActor = new MergeTablesDialog(mergeTablesActor);
            Optional<MergeTables> result = dialogActor.showAndWait();
            if (result.isPresent()) {
                mergeTablesListView.getItems().add(result.get());
                for (Table table : contextActorListView.getItems().filtered(Table::isSelected)) {
                    table.setSelected(false);
                    table.setDisabled(true);
                }
            }
        }
        MergeTables mergeTablesLocation = new MergeTables(contextLocationListView.getItems().filtered(Table::isSelected));
        if (mergeTablesLocation.getTables().size() > 0) {
            MergeTablesDialog dialogLocation = new MergeTablesDialog(mergeTablesLocation);
            Optional<MergeTables> resultLocation = dialogLocation.showAndWait();
            if (resultLocation.isPresent()) {
                mergeTablesListView.getItems().add(resultLocation.get());
                for (Table table : contextLocationListView.getItems().filtered(Table::isSelected)) {
                    table.setSelected(false);
                    table.setDisabled(true);
                }
            }
        }
    }

    @FXML
    public void removeSelectionToMerge() {
        List<MergeTables> mergeTables = mergeTablesListView.getItems().filtered(MergeTables::isSelected);
        for (MergeTables mergeTable : mergeTables) {
            for (Table table : mergeTable.getTables()) {
                table.setDisabled(false);
            }
        }
        mergeTablesListView.getItems().removeIf(MergeTables::isSelected);
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    protected boolean checkRequirements() {
        return true;
    }

    @Override
    protected void submitImpl() {
        if (mergeCheckBox.isSelected()) {
            //on créé une nouvelle Database plutôt que de modifier l'existante
            Database newDatabase = new Database();
            //on ajoute toutes les tables qui ne sont pas modifiées, donc celles de Metadata,
            List<Table> nonMergeableTables = new ArrayList<>(database.getTablesWithTag("Metadata"));
            nonMergeableTables.addAll(database.getTablesWithTag("Entity"));
            nonMergeableTables.addAll(database.getTablesWithTag("Observation"));
            for (Table table : nonMergeableTables) {
                newDatabase.addTable(table);
            }
            List<Column> foreignKeys = database.getForeignKeys();
            for (MergeTables mergeTables : mergeTablesListView.getItems()) {
                newDatabase.addTable(mergeTables.toTable(foreignKeys));
            }
            List<Table> notDisabledActor = contextActorListView.getItems().filtered(table -> !table.isDisabled());
            for (Table table : notDisabledActor) {
                newDatabase.addTable(table);
            }
            List<Table> notDisabledLocation = contextLocationListView.getItems().filtered(table -> !table.isDisabled());
            for (Table table : notDisabledLocation) {
                newDatabase.addTable(table);
            }
            database = newDatabase;
        }
    }

    @Override
    protected void cancelImpl() {

    }

}
