package fr.ufc.metaobs.model.export;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MergeTables {

    private final StringProperty newTableName;
    private final ListProperty<Table> tables;
    private final List<Column> primaryKeys;
    private final List<Column> foreignKeys;
    private final ListProperty<Column> newColumns;
    private final BooleanProperty selected;

    public MergeTables(List<Table> tables) {
        this.newTableName = new SimpleStringProperty();
        this.tables = new SimpleListProperty<>(FXCollections.observableList(tables));
        this.primaryKeys = tables.stream()
                .flatMap(table -> table.getColumns().values().stream())
                .filter(column -> column instanceof PrimaryKey)
                .collect(Collectors.toList());
        this.foreignKeys = tables.stream()
                .flatMap(table -> table.getColumns().values().stream())
                .filter(column -> column instanceof ForeignKey)
                .collect(Collectors.toList());
        this.newColumns = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.selected = new SimpleBooleanProperty(false);
    }

    public String getNewTableName() {
        return newTableName.get();
    }

    public void setNewTableName(String newTableName) {
        this.newTableName.set(newTableName);
    }

    public StringProperty newTableNameProperty() {
        return newTableName;
    }

    public ObservableList<Table> getTables() {
        return tables.get();
    }

    public void setTables(ObservableList<Table> tables) {
        this.tables.set(tables);
    }

    public ListProperty<Table> tablesProperty() {
        return tables;
    }

    public ObservableList<Column> getNewColumns() {
        return newColumns.get();
    }

    public void setNewColumns(ObservableList<Column> newColumns) {
        this.newColumns.set(newColumns);
    }

    public ListProperty<Column> newColumnsProperty() {
        return newColumns;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MergeTables that = (MergeTables) o;
        return Objects.equals(newTableName, that.newTableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newTableName);
    }

    /**
     * Transforme en Table pour faire la fusion des tables originales.
     *
     * @param foreignKeys les cl??s ??trang??res qu'il faut potentiellement modifier pour les mettre ?? jour avec la
     *                    nouvelle cl?? primaire de cette table
     * @return une Table d'export qui est la fusion des tables originales
     */
    public Table toTable(List<Column> foreignKeys) {
        String tableName = getNewTableName();
        Table tableRes = new Table(tableName);
        PrimaryKey newPrimaryKey = new PrimaryKey(tableName, "id" + tableName, "INT(11)", false, true);
        //on va chercher parmi toutes les cl??s ??trang??res celles qui r??f??rencent une cl?? primaire originale des tables fusionn??es
        for (Column foreignKey : foreignKeys) {
            if (foreignKey instanceof ForeignKey) {
                PrimaryKey primaryForeignKey = ((ForeignKey) foreignKey).getPrimaryKey();
                Table tableForeignKey = ((ForeignKey) foreignKey).getTable();
                //si la liste des cl??s primaires des tables originales contient la cl?? primaire r??f??renc??e par la cl?? ??trang??re
                if (primaryKeys.contains(primaryForeignKey)) {
                    //alors on change la cl?? r??f??renc??e par la cl?? ??trang??re
                    //pour r??f??rencer la nouvelle cl?? primaire de la table fusionn??e
                    ((ForeignKey) foreignKey).setPrimaryKey(newPrimaryKey);
                }
                //si la liste des tables originales contient la table de la cl?? ??trang??re
                if (tables.contains(tableForeignKey)) {
                    //alors on change la table de la cl?? ??trang??re par la table de fusion
                    ((ForeignKey) foreignKey).setTable(tableRes);
                }
            }
        }
        //on ajoute la nouvelle cl?? primaire
        tableRes.putColumn(newPrimaryKey);
        //on ??vite les doublons de cl??s ??trang??res avec la m??me table
        Set<Table> setTablesForeignKeys = new HashSet<>(this.foreignKeys.size());
        this.foreignKeys.removeIf(fk -> fk instanceof ForeignKey && !setTablesForeignKeys.add(((ForeignKey) fk).getTable()));
        //on ajoute les cl??s ??trang??res
        for (Column foreignKey : this.foreignKeys) {
            tableRes.putColumn(foreignKey);
        }
        //on rajoute toutes les nouvelles colonnes (celles qui ont ??t?? fusionn??es ou non)
        for (Column column : newColumns) {
            column.setNullable(true);
            tableRes.putColumn(column);
        }
        return tableRes;
    }

}
