package fr.ufc.metaobs.view;

import fr.ufc.metaobs.model.Characteristic;
import fr.ufc.metaobs.model.Observation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.stream.Collectors;

import static fr.ufc.metaobs.utils.SubContextUtils.*;

public class ListObservationsViewController {

    private final ObservableList<Observation> observations;

    @FXML
    private TableView<Observation> observationsTableView;

    @FXML
    private TableColumn<Observation, String> entityNameTableColumn;

    @FXML
    private TableColumn<Observation, String> nameTableColumn;

    @FXML
    private TableColumn<Observation, String> multiplicityTableColumn;

    @FXML
    private TableColumn<Observation, String> actorTableColumn;

    @FXML
    private TableColumn<Observation, String> actorPropertiesTableColumn;

    @FXML
    private TableColumn<Observation, String> locationTableColumn;

    @FXML
    private TableColumn<Observation, String> locationPropertiesTableColumn;

    @FXML
    private TableColumn<Observation, String> dateTableColumn;

    @FXML
    private TableColumn<Observation, String> dateFormatTableColumn;

    @FXML
    private TableColumn<Observation, String> dateStandardTableColumn;

    @FXML
    private TableColumn<Observation, String> characteristicsTableColumn;

    public ListObservationsViewController(ObservableList<Observation> observations) {
        this.observations = observations;
    }

    @FXML
    public void initialize() {
        String separatorName = System.lineSeparator() + ";" + System.lineSeparator();
        String separatorProperties = ";" + System.lineSeparator();

        observationsTableView.setItems(observations);

        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        multiplicityTableColumn.setCellValueFactory(new PropertyValueFactory<>("multiplicity"));

        entityNameTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            return observation.getEntity().nameProperty();
        });
        actorTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getContext() != null ? getActorName(observation.getContext().getContextActor(), separatorName) : "";
            return new SimpleStringProperty(value);
        });
        locationTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getContext() != null ? getLocationName(observation.getContext().getContextLocation(), separatorName) : "";
            return new SimpleStringProperty(value);
        });
        dateTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getContext() != null ? observation.getContext().getContextDate().getName() : "";
            return new SimpleStringProperty(value);
        });
        dateFormatTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getContext() != null ? observation.getContext().getContextDate().getDateFormat() : "";
            return new SimpleStringProperty(value);
        });
        dateStandardTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getContext() != null ? observation.getContext().getContextDate().getDateStandard() : "";
            return new SimpleStringProperty(value);
        });

        characteristicsTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getCharacteristics().stream().map(Characteristic::getName).collect(Collectors.joining(","));
            return new SimpleStringProperty(value);
        });

        actorPropertiesTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getContext() != null ? getActorPropeties(observation.getContext().getContextActor(), separatorProperties) : "";
            return new SimpleStringProperty(value);
        });
        locationPropertiesTableColumn.setCellValueFactory(observationStringCellDataFeatures -> {
            Observation observation = observationStringCellDataFeatures.getValue();
            String value = observation.getContext() != null ? getLocationProperties(observation.getContext().getContextLocation(), separatorProperties) : "";
            return new SimpleStringProperty(value);
        });

    }

}
