package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.model.FieldTypeSize;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.text.DecimalFormat;
import java.text.ParsePosition;

/**
 * Classe héritant de CustomMenuItem pour contenir un RadioButton et éventuellement un TextField pour pouvoir
 * entrer des chiffres.
 *
 * @see javafx.scene.control.CustomMenuItem
 */
public class CustomRadioMenuItem extends CustomMenuItem {

    private final RadioButton radioButton;
    private final FieldTypeSize fieldTypeSize;
    private TextField optionalFieldSize;

    public CustomRadioMenuItem(ToggleGroup toggleGroup, String text, boolean hasFieldSize) {
        super();
        setHideOnClick(false);
        fieldTypeSize = new FieldTypeSize(text);
        radioButton = new RadioButton(text);
        //quand on clique sur le MenuItem, on sélectionne le RadioButton
        //et on demande le focus pour le TextField de la taille si nécessaire
        setOnAction(actionEvent -> {
            radioButton.fire();
            if (getOptionalFieldSize() != null) {
                getOptionalFieldSize().requestFocus();
            }
        });
        radioButton.setToggleGroup(toggleGroup);
        radioButton.setUserData(fieldTypeSize);
        HBox root = new HBox(radioButton);
        root.setSpacing(5.0);
        root.setAlignment(Pos.CENTER);
        root.setFillHeight(true);
        if (hasFieldSize) {
            optionalFieldSize = new TextField();
            optionalFieldSize.setPromptText("size");
            optionalFieldSize.setPrefWidth(50.0);
            //on ne doit pouvoir rentrer que des chiffres
            DecimalFormat format = new DecimalFormat("#");
            optionalFieldSize.setTextFormatter(new TextFormatter<>(c -> {
                if (c.getControlNewText().isEmpty()) {
                    return c;
                }
                ParsePosition parsePosition = new ParsePosition(0);
                Object object = format.parse(c.getControlNewText(), parsePosition);
                if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
                    return null;
                } else {
                    return c;
                }
            }));
            optionalFieldSize.textProperty().bindBidirectional(fieldTypeSize.sizeProperty());
            root.getChildren().add(optionalFieldSize);
        }
        setContent(root);
    }

    public RadioButton getRadioButton() {
        return radioButton;
    }

    public TextField getOptionalFieldSize() {
        return optionalFieldSize;
    }

}
