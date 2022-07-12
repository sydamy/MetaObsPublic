package fr.ufc.metaobs.view.dialogs;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;

import java.io.File;

public class SaveAsDialog extends Dialog<Pair<String, String>> {

    public SaveAsDialog() {
        super();
        setResizable(true);
        setTitle("Save as...");
        setHeaderText("Choose directory to save project and optionally rename it.");
        VBox root = new VBox(5.0);
        root.setFillWidth(true);

        HBox directoryHBox = new HBox(5.0);
        directoryHBox.setFillHeight(true);
        TextField directoryTextField = new TextField();
        directoryTextField.setPromptText("Directory path");
        Button chooseDirectoryButton = new Button("Choose directory");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        chooseDirectoryButton.setOnAction(actionEvent -> {
            File directory = directoryChooser.showDialog(null);
            if (directory != null) {
                directoryTextField.setText(directory.getAbsolutePath());
            }
        });
        directoryHBox.getChildren().add(directoryTextField);
        directoryHBox.getChildren().add(chooseDirectoryButton);

        HBox renameHBox = new HBox(5.0);
        renameHBox.setFillHeight(true);
        TextField renameTextField = new TextField();
        Label renameLabel = new Label("Rename project (optional):");
        renameLabel.setLabelFor(renameTextField);
        renameHBox.getChildren().add(renameLabel);
        renameHBox.getChildren().add(renameTextField);

        root.getChildren().add(directoryHBox);
        root.getChildren().add(renameHBox);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(root);

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new Pair<>(directoryTextField.getText(), renameTextField.getText());
            }
            return null;
        });
    }
}
