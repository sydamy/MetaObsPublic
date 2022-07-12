package fr.ufc.metaobs.view;

import fr.ufc.metaobs.model.Characteristic;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ListCharacteristicsViewController {

    private final ObservableList<Characteristic> characteristics;

    @FXML
    private TableView<Characteristic> characteristicsTableView;

    @FXML
    private TableColumn<Characteristic, String> entityNameTableColumn;

    @FXML
    private TableColumn<Characteristic, String> nameTableColumn;

    @FXML
    private TableColumn<Characteristic, String> measureTableColumn;

    @FXML
    private TableColumn<Characteristic, String> fieldTypeTableColumn;

    @FXML
    private TableColumn<Characteristic, String> unitTableColumn;

    @FXML
    private TableColumn<Characteristic, String> classificationTableColumn;

    @FXML
    private TableColumn<Characteristic, String> toolTableColumn;

    @FXML
    private TableColumn<Characteristic, String> protocolTableColumn;

    @FXML
    private TableColumn<Characteristic, String> nullableTableColumn;


    public ListCharacteristicsViewController(ObservableList<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    @FXML
    public void initialize() {
        characteristicsTableView.setItems(characteristics);
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        measureTableColumn.setCellValueFactory(new PropertyValueFactory<>("measure"));
        nullableTableColumn.setCellValueFactory(new PropertyValueFactory<>("nullable"));
        protocolTableColumn.setCellValueFactory(new PropertyValueFactory<>("protocol"));

        entityNameTableColumn.setCellValueFactory(characteristicStringCellDataFeatures -> {
            Characteristic characteristic = characteristicStringCellDataFeatures.getValue();
            return characteristic.getEntity().nameProperty();
        });
        fieldTypeTableColumn.setCellValueFactory(characteristicStringCellDataFeatures -> {
            Characteristic characteristic = characteristicStringCellDataFeatures.getValue();
            String value = characteristic.getFieldType();
            if (characteristic.getFieldSize() != null && !characteristic.getFieldSize().isBlank()) {
                value = value + "(" + characteristic.getFieldSize() + ")";
            }
            return new SimpleStringProperty(value);
        });
        unitTableColumn.setCellValueFactory(characteristicStringCellDataFeatures -> {
            Characteristic characteristic = characteristicStringCellDataFeatures.getValue();
            String value = characteristic.getUnit() != null ? characteristic.getUnit().getValue() : "";
            return new SimpleStringProperty(value);
        });
        classificationTableColumn.setCellValueFactory(characteristicStringCellDataFeatures -> {
            Characteristic characteristic = characteristicStringCellDataFeatures.getValue();
            String value = characteristic.getClassification() != null ? characteristic.getClassification().getValue() : "";
            return new SimpleStringProperty(value);
        });
        toolTableColumn.setCellValueFactory(characteristicStringCellDataFeatures -> {
            Characteristic characteristic = characteristicStringCellDataFeatures.getValue();
            String value = characteristic.getTool() != null ? characteristic.getTool().getValue() : "";
            return new SimpleStringProperty(value);
        });
    }


}
