package fr.ufc.metaobs.model.export;

import com.github.jferard.fastods.OdsDocument;
import com.github.jferard.fastods.TableCell;
import com.github.jferard.fastods.TableRowImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Table implements ExportElement<com.github.jferard.fastods.Table, OdsDocument> {

    private final String name;
    private final Map<Integer, Column> columns;
    private final Map<Integer, Map<String, Object>> values;
    private final BooleanProperty selected;
    private final BooleanProperty disabled;
    private String tag;

    public Table(String name) {
        this.name = name;
        this.columns = new TreeMap<>();
        this.values = new TreeMap<>();
        this.selected = new SimpleBooleanProperty(false);
        this.disabled = new SimpleBooleanProperty(false);
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Column> getColumns() {
        return columns;
    }

    public Column getColumn(String name) {
        for (Column column : columns.values()) {
            if (column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    public void putColumn(Column column) {
        this.columns.put(columns.size(), column);
    }

    public Map<Integer, Map<String, Object>> getValues() {
        return values;
    }

    public void putValue(Integer idMap, String columnName, Object value) {
        values.putIfAbsent(idMap, new HashMap<>());
        values.computeIfPresent(idMap, (key, var) -> {
            var.put(columnName, value);
            return var;
        });
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public boolean isDisabled() {
        return disabled.get();
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    @Override
    public String toSqlString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/***************** ").append(name).append(" *****************/");
        sb.append(NEW_LINE);
        sb.append("DROP TABLE IF EXISTS `").append(name).append("`;");
        sb.append(NEW_LINE);
        sb.append("CREATE TABLE `").append(name).append("` (");
        sb.append(NEW_LINE);
        sb.append(columns.values().stream().map(Column::toSqlString).collect(Collectors.joining("," + NEW_LINE)));
        String autoIncrementStr = " ";
        if (columns.values().stream().anyMatch(column -> column instanceof PrimaryKey)) {
            sb.append(",").append(NEW_LINE);
            sb.append("PRIMARY KEY (`id").append(name).append("`)");
            sb.append(NEW_LINE);
            autoIncrementStr = " AUTO_INCREMENT=1 ";
        } else {
            sb.append(NEW_LINE);
        }
        sb.append(") ENGINE=InnoDB").append(autoIncrementStr).append("CHARSET=utf8;");
        sb.append(NEW_LINE);
        if (!values.isEmpty()) {
            sb.append("LOCK TABLES `").append(name).append("` WRITE;");
            sb.append(NEW_LINE);
            for (Map<String, Object> map : values.values()) {
                sb.append("INSERT INTO `").append(name).append("` VALUES (");
                int max = columns.keySet().stream().max(Comparator.naturalOrder()).get();
                for (Map.Entry<Integer, Column> entry : columns.entrySet()) {
                    Column column = entry.getValue();
                    sb.append("'").append(map.get(column.getName())).append("'");
                    if (entry.getKey() < max) {
                        sb.append(",");
                    } else {
                        sb.append(");");
                    }
                }
                sb.append(NEW_LINE);
            }
            sb.append(NEW_LINE);
            sb.append("UNLOCK TABLES;");
        }
        return sb.toString();
    }

    @Override
    public com.github.jferard.fastods.Table toOds(OdsDocument parent) {
        com.github.jferard.fastods.Table odsTable = null;
        try {
            odsTable = parent.addTable(name);
            int nbRow = 0;
            TableRowImpl header = odsTable.getRow(nbRow++);
            for (Map.Entry<Integer, Column> entry : columns.entrySet()) {
                entry.getValue().toOds(header);
            }
            for (Map<String, Object> map : values.values()) {
                TableRowImpl row = odsTable.getRow(nbRow++);
                for (Map.Entry<Integer, Column> entry : columns.entrySet()) {
                    TableCell cell = row.getOrCreateCell(entry.getKey());
                    Column column = entry.getValue();
                    Object value = map.get(column.getName());
                    if (value != null) {
                        if (value instanceof Integer) {
                            cell.setFloatValue(((Integer) value));
                        } else {
                            cell.setStringValue(value.toString());
                        }
                    } else {
                        cell.setVoidValue();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return odsTable;
    }
}
