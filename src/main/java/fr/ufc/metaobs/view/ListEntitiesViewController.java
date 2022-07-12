package fr.ufc.metaobs.view;

import fr.ufc.metaobs.model.Entity;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ListEntitiesViewController {

    private final ObservableList<Entity> entities;

    @FXML
    private TableView<Entity> entitiesTableView;

    @FXML
    private TableColumn<Entity, String> nameTableColumn;

    @FXML
    private TableColumn<Entity, String> typeTableColumn;

    @FXML
    private TableColumn<Entity, String> repositoryNameTableColumn;

    @FXML
    private TableColumn<Entity, String> repositoryIriTableColumn;

    @FXML
    private TableColumn<Entity, String> nameInRepositoryTableColumn;

    @FXML
    private TableColumn<Entity, String> iriInRepositoryTableColumn;

    public ListEntitiesViewController(ObservableList<Entity> entities) {
        this.entities = entities;
    }

    @FXML
    public void initialize() {
        entitiesTableView.setItems(entities);
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeTableColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        //pour les propriétés du referential, les CellValueFactory sont un peu moins simples car les propriétés
        //ne sont pas directement dans l'entité et il faut vérifier que le referential n'est pas null
        repositoryNameTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getReferential() != null ? entity.getReferential().getRepositoryName() : "";
            return new SimpleStringProperty(value);
        });
        repositoryIriTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getReferential() != null ? entity.getReferential().getRepositoryIri() : "";
            return new SimpleStringProperty(value);
        });
        nameInRepositoryTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getReferential() != null ? entity.getReferential().getNameInRepository() : "";
            return new SimpleStringProperty(value);
        });
        iriInRepositoryTableColumn.setCellValueFactory(entityStringCellDataFeatures -> {
            Entity entity = entityStringCellDataFeatures.getValue();
            String value = entity.getReferential() != null ? entity.getReferential().getIriInRepository() : "";
            return new SimpleStringProperty(value);
        });
    }

}
