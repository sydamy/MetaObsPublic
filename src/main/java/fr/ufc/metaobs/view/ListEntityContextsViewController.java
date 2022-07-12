package fr.ufc.metaobs.view;

import fr.ufc.metaobs.model.Entity;
import fr.ufc.metaobs.model.Field;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import static fr.ufc.metaobs.utils.SubContextUtils.*;

public class ListEntityContextsViewController {

    private final ObservableList<Entity> entities;


    @FXML
    private TableView<Entity> entityContextsTableView;

    @FXML
    private TableColumn<Entity, String> entityTableColumn;

    @FXML
    private TableColumn<Entity, String> actorTableColumn;

    @FXML
    private TableColumn<Entity, String> actorPropertiesTableColumn;

    @FXML
    private TableColumn<Entity, String> locationTableColumn;

    @FXML
    private TableColumn<Entity, String> locationPropertiesTableColumn;

    @FXML
    private TableColumn<Entity, String> dateTableColumn;

    @FXML
    private TableColumn<Entity, String> dateFormatTableColumn;

    @FXML
    private TableColumn<Entity, String> datePropertiesTableColumn;

    @FXML
    private TableColumn<Entity, String> dateStandardTableColumn;

    public ListEntityContextsViewController(ObservableList<Entity> entities) {
        this.entities = entities;
    }


    @FXML
    public void initialize() {
        String separatorName = System.lineSeparator() + ";" + System.lineSeparator();
        String separatorProperties = ";" + System.lineSeparator();

        entityContextsTableView.setItems(this.entities);

        entityTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        actorTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getContext() != null ? getActorName(entity.getContext().getContextActor(), separatorName) : "";
            return new SimpleStringProperty(value);
        });
        locationTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getContext() != null ? getLocationName(entity.getContext().getContextLocation(), separatorName) : "";
            return new SimpleStringProperty(value);
        });
        dateTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getContext() != null ? entity.getContext().getContextDate().getName() : "";
            return new SimpleStringProperty(value);
        });
        dateFormatTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getContext() != null ? entity.getContext().getContextDate().getDateFormat() : "";
            return new SimpleStringProperty(value);
        });
        dateStandardTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getContext() != null ? entity.getContext().getContextDate().getDateStandard() : "";
            return new SimpleStringProperty(value);
        });

        actorPropertiesTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getContext() != null ? getActorPropeties(entity.getContext().getContextActor(), separatorProperties) : "";
            return new SimpleStringProperty(value);
        });

        locationPropertiesTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getContext() != null ? getLocationProperties(entity.getContext().getContextLocation(), separatorProperties) : "";
            return new SimpleStringProperty(value);
        });
        datePropertiesTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            if (entity.getContext() != null) {
                for (Field field : entity.getContext().getContextDate().getPropertiesList()) {
                    stringBuilder.append(field.getName()).append(":").append(field.getTypeSize().toString()).append(System.lineSeparator());
                }
            }
            return new SimpleStringProperty(stringBuilder.toString());
        });
    }

}
