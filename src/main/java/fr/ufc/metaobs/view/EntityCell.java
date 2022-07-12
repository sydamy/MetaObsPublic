package fr.ufc.metaobs.view;

import fr.ufc.metaobs.model.Characteristic;
import fr.ufc.metaobs.model.Entity;
import fr.ufc.metaobs.model.Observation;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;

import java.io.IOException;

/**
 * Classe héritant de ListCell pour une cellule de ListView, contenant une Entity.
 *
 * @see javafx.scene.control.ListCell
 */
public class EntityCell extends ListCell<Entity> {

    public static final int PREF_HEIGHT_CHARACTERISTIC = 24;

    private final MainViewController mainViewController;

    private FXMLLoader fxmlLoader;

    @FXML
    private Label entityNameLabel;

    @FXML
    private Label entityTypeLabel;

    @FXML
    private TitledPane characteristicsTitledPane;

    @FXML
    private ListView<Characteristic> characteristicsListView;

    @FXML
    private TitledPane observationsTitledPane;

    @FXML
    private ListView<Observation> observationsListView;

    public EntityCell(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    @FXML
    public void initialize() {
        setOnMouseClicked(mouseEvent -> {
            characteristicsTitledPane.setExpanded(true);
            observationsTitledPane.setExpanded(true);
        });
    }

    @Override
    protected void updateItem(Entity entity, boolean empty) {
        super.updateItem(entity, empty);
        if (empty || entity == null) {
            setText(null);
            setContentDisplay(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(EntityCell.class.getResource("/fr/ufc/metaobs/view/entity.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setText(null);
            setGraphic(fxmlLoader.getRoot());

            //on bind les propriétés textuelles pour le nom et le type
            entityNameLabel.textProperty().bind(entity.nameProperty());
            entityTypeLabel.textProperty().bind(Bindings.concat("(", entity.typeProperty(), ")"));
            characteristicsListView.setCellFactory(new CharacteristicCellFactory(mainViewController, entity));
            characteristicsListView.itemsProperty().bind(entity.characteristicsProperty());
            //on optimise la hauteur préférée de la ListView par le (nombre d'éléments + 1) * leur taille + 2 (pour les contours de la ListView)
            //ceci est possible car l'élément graphique CharacteristicCell est de taille fixe et connue
            characteristicsListView.prefHeightProperty().bind(
                    Bindings.size(entity.getCharacteristics())
                            .add(1)
                            .multiply(PREF_HEIGHT_CHARACTERISTIC)
                            .add(2));

            observationsListView.setCellFactory(new ObservationCellFactory(mainViewController, entity));
            observationsListView.itemsProperty().bind(entity.observationsProperty());
        }
    }

}
