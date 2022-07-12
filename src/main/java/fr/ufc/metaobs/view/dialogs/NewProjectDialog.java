package fr.ufc.metaobs.view.dialogs;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;

public class NewProjectDialog extends Dialog<Pair<File, String>> {

    private static final String METAOBS_ONTO_PATH = "./data/onto/obs/";

    private File metaobsFile;

    public NewProjectDialog() {
        this("", "");
    }

    public NewProjectDialog(String metaobsStr, String nameProject) {
        super();
        setResizable(true);
        setTitle("New project");
        setHeaderText("Open Metaobs ontology then enter a name for the new project.");

        VBox root = new VBox(5.0);
        root.setFillWidth(true);

        HBox metaobsHBox = new HBox(5.0);
        metaobsHBox.setFillHeight(true);
        TextField metaobsTextField = new TextField(metaobsStr);
        metaobsTextField.setPrefColumnCount(25);
        metaobsTextField.setEditable(false);
        FileChooser metaobsChooser = new FileChooser();
        metaobsChooser.setTitle("Open Metaobs ontology");
        metaobsChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("OWL", "*.owl"));
        Button metaobsButton = new Button("Open Metaobs ontology");
        metaobsButton.setOnAction(actionEvent -> {
            File metaobsFile;
            metaobsChooser.setInitialDirectory(new File(METAOBS_ONTO_PATH));
            try {
                metaobsFile = metaobsChooser.showOpenDialog(null);
            } catch (IllegalArgumentException ignored) {
                metaobsChooser.setInitialDirectory(new File("." + METAOBS_ONTO_PATH));
                try {
                    metaobsFile = metaobsChooser.showOpenDialog(null);
                } catch (IllegalArgumentException ignored2) {
                    metaobsChooser.setInitialDirectory(new File("."));
                    metaobsFile = metaobsChooser.showOpenDialog(null);
                }
            }
            if (metaobsFile != null) {
                metaobsTextField.setText(metaobsFile.getAbsolutePath());
                this.metaobsFile = metaobsFile;
            }
        });
        metaobsHBox.getChildren().add(metaobsTextField);
        metaobsHBox.getChildren().add(metaobsButton);

        HBox nameHBox = new HBox(5.0);
        nameHBox.setFillHeight(true);
        TextField nameTextField = new TextField(nameProject);
        nameTextField.setPrefColumnCount(31);
        Label nameLabel = new Label("Project name:");
        nameLabel.setLabelFor(nameTextField);
        nameHBox.getChildren().add(nameLabel);
        nameHBox.getChildren().add(nameTextField);

        root.getChildren().add(metaobsHBox);
        root.getChildren().add(nameHBox);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(root);

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new Pair<>(metaobsFile, nameTextField.getText());
            }
            return null;
        });
    }
}
