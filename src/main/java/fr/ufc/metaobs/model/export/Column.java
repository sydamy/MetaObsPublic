package fr.ufc.metaobs.model.export;

import com.github.jferard.fastods.TableCell;
import com.github.jferard.fastods.TableRowImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class Column implements ExportElement<TableCell, TableRowImpl> {

    protected final String type;
    private final StringProperty name;
    private final BooleanProperty selected;
    protected boolean nullable;

    public Column(String name, String type, boolean nullable) {
        this.name = new SimpleStringProperty(name);
        this.type = type;
        this.nullable = nullable;
        this.selected = new SimpleBooleanProperty(false);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
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
    public String toString() {
        return getName() + " " + type;
    }

    @Override
    public String toSqlString() {
        String nullableStr = nullable ? "NULL" : "NOT NULL";
        return "`" + getName() + "` " + type + " " + nullableStr + "";
    }

    @Override
    public TableCell toOds(TableRowImpl parent) {
        TableCell cell = parent.getOrCreateCell(parent.getColumnCount());
        cell.setStringValue(getName());
        return cell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return Objects.equals(getName(), column.getName()) && Objects.equals(type, column.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

}
