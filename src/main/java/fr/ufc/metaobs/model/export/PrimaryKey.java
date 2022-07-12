package fr.ufc.metaobs.model.export;

import java.util.Objects;

public class PrimaryKey extends Column {

    private final String tableName;
    private final boolean autoIncrement;

    public PrimaryKey(String tableName, String name, String type, boolean nullable, boolean autoIncrement) {
        super(name, type, nullable);
        this.tableName = tableName;
        this.autoIncrement = autoIncrement;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toSqlString() {
        String nullableStr = super.isNullable() ? "NULL" : "NOT NULL";
        String autoIncrementStr = autoIncrement ? "AUTO_INCREMENT" : " ";
        return "`" + super.getName() + "` " + super.getType() + " " + nullableStr + " " + autoIncrementStr + "";
    }
}
