package fr.ufc.metaobs.view.forms;

import fr.ufc.metaobs.controllers.Controller;
import fr.ufc.metaobs.enums.Tags;
import fr.ufc.metaobs.exceptions.NoOntClassException;
import fr.ufc.metaobs.handlers.OwlHandler;
import fr.ufc.metaobs.model.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.jena.ontology.OntClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContextFormViewController extends AbstractFormViewController {

    private final ToggleGroup chosenTimeToggleGroup;
    private final StringProperty chosenTimeFormat;

    private final SubContextActorFormViewController contextActorFormViewController;
    private final SubContextLocationFormViewController contextLocationFormViewController;

    private StringBinding entityOfContext;
    private Context context;

    @FXML
    private BorderPane contextActorPane;

    @FXML
    private BorderPane contextLocationPane;

    @FXML
    private MenuButton locationMenuButton;

    @FXML
    private MenuButton timeFormatMenuButton;

    @FXML
    private TextField tableLocationNameTextField;

    @FXML
    private TextField dateStandardTextField;

    @FXML
    private ListView<Field> fieldsLocationListView;

    @FXML
    private ListView<Field> fieldsDateListView;

    public ContextFormViewController(Context context) {
        this.context = context;
        contextActorFormViewController = new SubContextActorFormViewController(this.context != null ? this.context.getContextActor() : null);
        contextLocationFormViewController = new SubContextLocationFormViewController(this.context != null ? this.context.getContextLocation() : null);
        chosenTimeFormat = new SimpleStringProperty();
        chosenTimeToggleGroup = new ToggleGroup();
    }

    public String getEntityOfContext() {
        return entityOfContext.get();
    }

    public void setEntityOfContext(StringBinding stringBinding) {
        entityOfContext = stringBinding;
    }

    public StringBinding entityOfContextProperty() {
        return entityOfContext;
    }

    @FXML
    public void initialize() throws NoOntClassException {
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();

        FXMLLoader contextActorLoader = new FXMLLoader(ContextFormViewController.class.getResource("/fr/ufc/metaobs/edit/sub_context_actor.fxml"));
        contextActorLoader.setController(contextActorFormViewController);
        try {
            Pane content = contextActorLoader.load();
            contextActorPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FXMLLoader contextLocationLoader = new FXMLLoader(ContextFormViewController.class.getResource("/fr/ufc/metaobs/edit/sub_context_location.fxml"));
        contextLocationLoader.setController(contextLocationFormViewController);

        try {
            Pane content = contextLocationLoader.load();
            contextLocationPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String timeFormat = null;
        List<Field> fieldDate = new ArrayList<>();
        if (context != null && context.getContextDate() != null) {
            timeFormat = context.getContextDate().getDateFormat();
            fieldDate.addAll(context.getContextDate().getPropertiesList());
        }
        List<OntClass> dates = owlHandler.getClassesWithTag(Tags.Dates.toString());
        if (dates.isEmpty()) {
            throw new NoOntClassException();
        }
        populateTimeFormatMenuButton(timeFormatMenuButton, chosenTimeToggleGroup, fieldsDateListView, timeFormat, fieldDate, dates.get(0));

        chosenTimeFormat.bind(Bindings.selectString(chosenTimeToggleGroup.selectedToggleProperty(), "userData"));
        timeFormatMenuButton.textProperty().bind(
                Bindings.when(chosenTimeFormat.isNotNull())
                        .then(chosenTimeFormat)
                        .otherwise("Time format")
        );

        initValues();
    }

    private void initValues() {
        if (context != null) {
            dateStandardTextField.setText(context.getContextDate().getDateStandard());
            fieldsDateListView.getItems().setAll(context.getContextDate().getPropertiesList());
        }
    }

    private void populateTimeFormatMenuButton(MenuButton menuButton, ToggleGroup toggleGroup, ListView<Field> listView,
                                              String timeFormat, List<Field> fieldList, OntClass superClass) {
        OwlHandler owlHandler = Controller.getInstance().getOwlHandler();
        RadioMenuItem noneMenuItem = new RadioMenuItem("None");
        noneMenuItem.setToggleGroup(toggleGroup);
        noneMenuItem.setUserData("None");
        menuButton.getItems().add(noneMenuItem);
        if ("None".equals(timeFormat)) {
            toggleGroup.selectToggle(noneMenuItem);
        }
        for (OntClass subClass : owlHandler.getSubclasses(superClass)) {
            populateTimeFormatMenuButtonRecursive(menuButton, null, toggleGroup, timeFormat, listView, fieldList, owlHandler, subClass);
        }
    }

    private void populateTimeFormatMenuButtonRecursive(MenuButton menuButton, Menu menu, ToggleGroup toggleGroup,
                                                       String timeFormat, ListView<Field> listView, List<Field> fieldList,
                                                       OwlHandler owlHandler, OntClass superClass) {
        List<OntClass> subClasses = owlHandler.getSubclasses(superClass);
        String menuName = owlHandler.getLocalName(superClass);
        //si la liste subClasses n'est pas vide, alors on ajoute un Menu
        if (!subClasses.isEmpty()) {
            Menu newMenu = new Menu(menuName);
            if (menu == null) {
                menuButton.getItems().add(newMenu);
            } else {
                menu.getItems().add(newMenu);
            }
            for (OntClass subClass : subClasses) {
                populateTimeFormatMenuButtonRecursive(menuButton, newMenu, toggleGroup, timeFormat, listView, fieldList, owlHandler, subClass);
            }
            //si la liste subClasses est vide, alors on ajoute un RadioMenuItem
        } else {
            RadioMenuItem menuItem = new RadioMenuItem(menuName);
            menuItem.setToggleGroup(toggleGroup);
            menuItem.setUserData(menuName);

            //on regarde la valeur de la propriété hasNumberFields sur l'OntClass superClass
            int hasNumberFields = superClass.listSuperClasses()
                    .filterKeep(OntClass::isRestriction)
                    .mapWith(OntClass::asRestriction)
                    .filterKeep(restriction -> "hasNumberFields".equals(restriction.getOnProperty().getLocalName()))
                    .next()
                    .asHasValueRestriction().getHasValue().asLiteral().getInt();

            List<Field> fields = new ArrayList<>(hasNumberFields);
            if (hasNumberFields < 2) {
                Field field = new Field("CollectDate");
                field.originalNameProperty().bind(Bindings.concat(entityOfContext, "CollectDate"));
                fields.add(field);
            } else {
                for (int i = 1; i <= hasNumberFields; i++) {
                    Field field = new Field("CollectDate" + i);
                    field.originalNameProperty().bind(Bindings.concat(entityOfContext, "CollectDate" + i));
                    fields.add(field);
                }
            }

            if (menuName != null && menuName.equals(timeFormat)) {
                menuItem.setSelected(true);
            }
            //on ajoute un listener sur la propriété de sélection
            menuItem.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue) {
                    listView.getItems().setAll(fields);
                } else {
                    listView.getItems().removeIf(fields::contains);
                }
            });
            if (menu == null) {
                menuButton.getItems().add(menuItem);
            } else {
                menu.getItems().add(menuItem);
            }
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected boolean checkRequirements() {
        boolean res = contextActorFormViewController.checkRequirements();
        res = res && contextLocationFormViewController.checkRequirements();
        res = res && FormViewControllerUtils.checkFields(fieldsDateListView.getItems());
        res = res && chosenTimeFormat.isNotNull().get();
        return res;
    }

    @Override
    protected void submitImpl() {
        if (context == null) {
            context = new Context(
                    new ContextActor(""),
                    new ContextLocation(""),
                    new ContextDate("")
            );
        }
        if (context.getContextActor() != null) {
            contextActorFormViewController.submitImpl();
            context.setContextActor(contextActorFormViewController.getContextActor());
        }
        if (context.getContextLocation() != null) {
            contextLocationFormViewController.submitImpl();
            context.setContextLocation(contextLocationFormViewController.getContextLocation());
        }
        context.getContextDate().setName(Bindings.concat(entityOfContext, "CollectDate").getValue());
        context.getContextDate().setDateFormat(chosenTimeFormat.get());
        context.getContextDate().setDateStandard(dateStandardTextField.getText());
        context.getContextDate().clearProperties();
        for (Field field : fieldsDateListView.getItems()) {
            context.getContextDate().putProperty(field.getOriginalName(), field.getName(), field.getTypeSize().toString());
        }
    }

    @Override
    protected void cancelImpl() {

    }

}
