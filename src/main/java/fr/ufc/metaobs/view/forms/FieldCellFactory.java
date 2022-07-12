package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.model.Field;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class FieldCellFactory implements Callback<ListView<Field>, ListCell<Field>> {
    @Override
    public ListCell<Field> call(ListView<Field> listView) {
        return new FieldCell();
    }
}
