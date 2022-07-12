package fr.ufc.metaobs.model.export;

public class ForeignKey extends Column {

    private Table table;
    private PrimaryKey primaryKey;

    public ForeignKey(Table table, String name, String type, boolean nullable, PrimaryKey primaryKey) {
        super(name, type, nullable);
        this.table = table;
        this.primaryKey = primaryKey;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTableName() {
        return table.getName();
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

}
