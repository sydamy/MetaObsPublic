package fr.ufc.metaobs.model.export;

import com.github.jferard.fastods.AnonymousOdsFileWriter;
import com.github.jferard.fastods.OdsDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Database implements ExportElement<OdsDocument, AnonymousOdsFileWriter> {

    private Map<Integer, Table> tables;

    public Database() {
        this.tables = new HashMap<>();
    }

    public Map<Integer, Table> getTables() {
        return tables;
    }

    public void setTables(Map<Integer, Table> tables) {
        this.tables = tables;
    }

    public List<Column> getForeignKeys() {
        return tables.values().stream()
                .flatMap(table -> table.getColumns().values().stream())
                .filter(column -> column instanceof ForeignKey)
                .collect(Collectors.toList());
    }

    public Table getTable(String name) {
        for (Table table : tables.values()) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

    public List<Table> getTablesWithTag(String tag) {
        return tables.values().stream()
                .filter(table -> tag.equalsIgnoreCase(table.getTag()))
                .collect(Collectors.toList());
    }

    public void addTable(Table table) {
        this.tables.put(tables.size(), table);
    }

    @Override
    public String toSqlString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SET FOREIGN_KEY_CHECKS = 0;");
        sb.append(NEW_LINE);
        for (Table table : tables.values()) {
            sb.append(table.toSqlString());
            sb.append(NEW_LINE).append(NEW_LINE);
        }
        List<Column> foreignKeys = getForeignKeys();
        for (Column column : foreignKeys) {
            ForeignKey foreignKey = (ForeignKey) column;
            sb.append("ALTER TABLE `").append(foreignKey.getTableName()).append("` ADD FOREIGN KEY (`");
            sb.append(foreignKey.getName()).append("`) REFERENCES `").append(foreignKey.getPrimaryKey().getTableName()).append("`");
            sb.append("(`").append(foreignKey.getPrimaryKey().getName()).append("`);");
            sb.append(NEW_LINE).append(NEW_LINE);
        }
        return sb.toString();
    }

    @Override
    public OdsDocument toOds(AnonymousOdsFileWriter parent) {
        OdsDocument document = parent.document();
        for (Table table : tables.values()) {
            table.toOds(document);
        }
        return document;
    }
}
