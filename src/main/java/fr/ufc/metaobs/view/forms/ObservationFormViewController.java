package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.model.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;

public class ObservationFormViewController extends AbstractFormViewController {

    private final ContextFormViewController contextFormViewController;
    private final Entity entity;
    private Observation observation;
    private StringBinding nameBinding;

    @FXML
    private Text textForm;

    @FXML
    private TextField entityTextField;

    @FXML
    private ListView<SelectableCharacteristic> characteristicsListView;

    @FXML
    private TextField nameTextField;

    @FXML
    private ToggleGroup multiplicityToggleGroup;

    @FXML
    private RadioButton multiplicity1RadioButton;

    @FXML
    private RadioButton multiplicityNRadioButton;

    @FXML
    private CheckBox differentContextCheckBox;

    @FXML
    private CheckBox externalIdCheckBox;

    @FXML
    private CustomFieldTypeMenuButton externalIdMenuButton;

    @FXML
    private BorderPane contextInclude;

    public ObservationFormViewController(Entity entity, Observation observation) {
        this.entity = entity;
        this.observation = observation;
        contextFormViewController = new ContextFormViewController(observation != null && observation.getContext() != null ? observation.getContext() : null);
    }

    @FXML
    public void initialize() {
        String text = "Add observation";
        if (observation != null) {
            text = "Edit observation";
        }
        textForm.setText(text);
        characteristicsListView.setCellFactory(characteristicListView -> new ListCell<>() {
            @Override
            protected void updateItem(SelectableCharacteristic selectableCharacteristic, boolean empty) {
                super.updateItem(selectableCharacteristic, empty);
                if (selectableCharacteristic == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    CheckBox checkBox = new CheckBox(selectableCharacteristic.getCharacteristic().getName());
                    checkBox.setMnemonicParsing(false);
                    //si elle est d??j?? s??lectionn??e, donc la caract??ristique est d??j?? li??e ?? cette observation
                    checkBox.selectedProperty().bindBidirectional(selectableCharacteristic.selectedProperty());
                    //on d??sactive si la caract??ristique est d??j?? li??e ?? une observation autre que celle-ci
                    checkBox.setDisable(selectableCharacteristic.getCharacteristic().getRefObs() > 0 && !selectableCharacteristic.isSelected());
                    setText(null);
                    setGraphic(checkBox);
                }
            }
        });

        externalIdCheckBox.setVisible(false);
        externalIdMenuButton.visibleProperty().bind(externalIdCheckBox.selectedProperty());
        //il est possible d'avoir un identifier externe si la multiplicit?? est de n, alors on affiche cette possibilit?? dans le formulaire
        //et il faut un context de collecte si on s??lectione une multiplicit?? de n
        multiplicityNRadioButton.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                externalIdCheckBox.setVisible(true);
                differentContextCheckBox.setSelected(true);
                differentContextCheckBox.setDisable(true);
            } else {
                externalIdCheckBox.setVisible(false);
                externalIdCheckBox.setSelected(false);
                differentContextCheckBox.setDisable(false);
            }
        });

        nameBinding = Bindings.selectString(nameTextField.textProperty());

        ChangeListener<? super Boolean> listener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    contextFormViewController.setEntityOfContext(nameBinding);
                    //charge dynamiquement le contenu du fichier edit/context.fxml pour pouvoir lui mettre comme contr??leur celui
                    //instanci?? avant (dans le constructeur)
                    FXMLLoader fxmlLoader = new FXMLLoader(EntityFormViewController.class.getResource("/fr/ufc/metaobs/edit/context.fxml"));
                    fxmlLoader.setController(contextFormViewController);
                    try {
                        Pane pane = fxmlLoader.load();
                        contextInclude.setCenter(pane);
                        contextInclude.visibleProperty().bind(differentContextCheckBox.selectedProperty());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    contextInclude.setVisible(false);
                }
                //on supprime le listener ?? la fin pour ne l'ex??cuter qu'une fois et donc charger qu'une fois le fxml
                differentContextCheckBox.selectedProperty().removeListener(this);
            }
        };
        differentContextCheckBox.selectedProperty().addListener(listener);

        initValues();
    }

    private void initValues() {
        entityTextField.setText(entity.getName());
        ObservableList<SelectableCharacteristic> characteristicsItems = FXCollections.observableArrayList();
        for (Characteristic characteristic : entity.getCharacteristics()) {
            boolean selected = observation != null && characteristic.getRefObs() == observation.getId();
            characteristicsItems.add(new SelectableCharacteristic(selected, characteristic));
        }
        characteristicsListView.setItems(characteristicsItems);
        if (observation != null) {
            nameTextField.setText(observation.getName());
            String multiplicity = observation.getMultiplicity();
            if ("1".equals(multiplicity)) {
                multiplicity1RadioButton.setSelected(true);
            } else {
                multiplicityNRadioButton.setSelected(true);
            }
            FieldTypeSize externalId = observation.getExternalId();
            if (externalId != null) {
                externalIdCheckBox.setSelected(true);
                externalIdMenuButton.selectTypeSize(externalId.toString());
            }
            differentContextCheckBox.setSelected(observation.getContext() != null);
        }
    }

    public Observation getObservation() {
        return observation;
    }

    @Override
    protected boolean checkRequirements() {
        //on v??rifie qu'il y a un nom
        boolean res = !nameTextField.getText().isBlank();
        //on v??rifie qu'il y a au moins une caract??ristique s??lectionn??e
        res = res && characteristicsListView.getItems()
                .filtered(SelectableCharacteristic::isSelected)
                .size() > 0;
        //on v??rifie la pr??sence de s??lection de multiplicit??
        res = res && multiplicityToggleGroup.getSelectedToggle() != null;
        if (differentContextCheckBox.isSelected()) {
            res = res && contextFormViewController.checkRequirements();
        }
        if (externalIdCheckBox.isSelected()) {
            res = res && externalIdMenuButton.getToggleGroup().getSelectedToggle() != null;
        }
        return res;
    }

    @Override
    protected void submitImpl() {
        if (observation == null) {
            observation = new Observation();
        }
        observation.setName(nameTextField.getText());
        observation.setCharacteristics(FXCollections.observableArrayList());
        characteristicsListView.getItems()
                //on garde les caract??ristiques s??lectionn??es
                .filtered(SelectableCharacteristic::isSelected)
                .stream()
                //on r??cup??re les caract??ristiques
                .map(SelectableCharacteristic::getCharacteristic)
                //on les lies ?? l'observation
                .forEach(characteristic -> characteristic.setObservation(observation));
        //pour les caract??ristiques d??s??lectionn??es, on leur enl??ve l'observation modifi??e
        characteristicsListView.getItems()
                .filtered(selectableCharacteristic -> !selectableCharacteristic.isSelected())
                .stream()
                .map(SelectableCharacteristic::getCharacteristic)
                .filter(characteristic -> characteristic.getRefObs() == observation.getId())
                .forEach(characteristic -> {
                    characteristic.setRefObs(0);
                    characteristic.setObservation(null);
                });
        if (externalIdCheckBox.isSelected()) {
            observation.setExternalId((FieldTypeSize) externalIdMenuButton.getToggleGroup().getSelectedToggle().getUserData());
        } else {
            observation.setExternalId(null);
        }

        if (differentContextCheckBox.isSelected()) {
            contextFormViewController.submitImpl();
            observation.setContext(contextFormViewController.getContext());
        } else {
            observation.setContext(null);
        }

        observation.setMultiplicity(multiplicityToggleGroup.getSelectedToggle().getUserData().toString());
    }

    @Override
    protected void cancelImpl() {
        contextFormViewController.cancelImpl();
    }
}
