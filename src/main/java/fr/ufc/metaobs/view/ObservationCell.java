package fr.ufc.metaobs.view;

import fr.ufc.metaobs.model.Characteristic;
import fr.ufc.metaobs.model.Observation;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.io.IOException;

/**
 * Classe héritant de ListCell pour une cellule de ListView, contenant une Observation.
 *
 * @see javafx.scene.control.ListCell
 */
public class ObservationCell extends ListCell<Observation> {

    public static final int PREF_HEIGHT_CHARACTERISTIC = 24;

    private FXMLLoader fxmlLoader;

    @FXML
    private Label observationNameLabel;

    @FXML
    private Label observationMultiplicityLabel;

    @FXML
    private ListView<Characteristic> characteristicsListView;

    @FXML
    public void initialize() {
        //on crée une CellFactory simple pour les caractéristiques, pas besoin de faire une classe pour cela
        characteristicsListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Characteristic> call(ListView<Characteristic> characteristicListView) {
                return new ListCell<>() {
                    final Label nameLabel = new Label();

                    @Override
                    protected void updateItem(Characteristic characteristic, boolean empty) {
                        super.updateItem(characteristic, empty);
                        if (empty || characteristic == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            nameLabel.textProperty().bind(characteristic.nameProperty());
                            setGraphic(nameLabel);
                        }
                    }
                };
            }
        });
    }

    @Override
    protected void updateItem(Observation observation, boolean empty) {
        super.updateItem(observation, empty);
        if (empty || observation == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(ObservationCell.class.getResource("/fr/ufc/metaobs/view/observation.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setText(null);
            setGraphic(fxmlLoader.getRoot());

            //on bind les propriétés textuelles pour le nom et la multiplicité
            observationNameLabel.textProperty().bind(observation.nameProperty());
            observationMultiplicityLabel.textProperty().bind(Bindings.concat("(", observation.multiplicityProperty(), ")"));
            //on bind la propriété de la liste
            characteristicsListView.itemsProperty().bind(observation.characteristicsProperty());
            //on optimise la hauteur préférée de la ListView par le (nombre d'éléments + 1) * leur taille + 2 (pour les contours de la ListView)
            //ceci est possible car l'élément graphique Label est de taille fixe et connue
            characteristicsListView.prefHeightProperty().bind(
                    Bindings.size(observation.characteristicsProperty())
                            .add(1)
                            .multiply(PREF_HEIGHT_CHARACTERISTIC)
                            .add(2));
        }

    }
}
