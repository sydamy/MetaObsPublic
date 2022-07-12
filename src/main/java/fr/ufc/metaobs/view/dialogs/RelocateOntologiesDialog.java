package fr.ufc.metaobs.view.dialogs;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class RelocateOntologiesDialog extends Dialog<File> {

    private File ontoDirectory;

    public RelocateOntologiesDialog() {
        super();
        setResizable(true);
        setTitle("Relocate ontologies");
        setHeaderText("Select 'onto' directory");

        HBox root = new HBox(5.0);
        TextField directoryTextField = new TextField();
        directoryTextField.setPromptText("Directory path");
        directoryTextField.setEditable(false);
        Button chooseDirectoryButton = new Button("Choose directory");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        chooseDirectoryButton.setOnAction(actionEvent -> {
            File directory = directoryChooser.showDialog(null);
            if (directory != null) {
                directoryTextField.setText(directory.getAbsolutePath());
                ontoDirectory = directory;
            }
        });
        root.getChildren().add(directoryTextField);
        root.getChildren().add(chooseDirectoryButton);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(root);

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return ontoDirectory;
            }
            return null;
        });
    }
}
