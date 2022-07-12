package fr.ufc.metaobs.controllers.services;

import fr.ufc.metaobs.controllers.services.tasks.CreateProjectTask;
import fr.ufc.metaobs.controllers.services.tasks.OpenProjectTask;
import fr.ufc.metaobs.handlers.ProjectXmlReader;
import fr.ufc.metaobs.model.Project;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * Classe Service qui permet d'ouvrir ou créer un projet en arrière-plan.
 *
 * @see CreateProjectTask
 * @see OpenProjectTask
 */
public class OpenCreateProjectService extends Service<Project> {

    //utiles pour ouvrir ou créer un projet
    private final Class<? extends Task<Project>> taskClass;
    private final ObjectProperty<File> fileProject;
    private final StringProperty baseUri;
    //utile pour ouvrir un projet
    private final ProjectXmlReader projectXmlReader;
    //utile pour créer un projet
    private final StringProperty ontMetaobs;
    //Consumer à récupérer et exécuter à la fin de la tâche
    private Consumer<Project> callback;

    public OpenCreateProjectService(ProjectXmlReader projectXmlReader, Class<? extends Task<Project>> taskClass) {
        this.taskClass = taskClass;
        this.fileProject = new SimpleObjectProperty<>();
        this.baseUri = new SimpleStringProperty();
        this.ontMetaobs = new SimpleStringProperty();
        this.projectXmlReader = projectXmlReader;
    }

    public File getFileProject() {
        return fileProject.get();
    }

    public void setFileProject(File fileProject) {
        this.fileProject.set(fileProject);
    }

    public String getBaseUri() {
        return baseUri.get();
    }

    public void setBaseUri(String baseUri) {
        this.baseUri.set(baseUri);
    }

    public String getOntMetaobs() {
        return ontMetaobs.get();
    }

    public void setOntMetaobs(String ontMetaobs) {
        this.ontMetaobs.set(ontMetaobs);
    }

    public Consumer<Project> getCallback() {
        return callback;
    }

    public void setCallback(Consumer<Project> callback) {
        this.callback = callback;
    }

    @Override
    protected Task<Project> createTask() {
        Task<Project> task = null;
        try {
            //on crée la Task grâce à la réflection
            //il faut donc que les classes à instancier aient un constructeur avec la même signature
            task = taskClass
                    .getConstructor(File.class, ProjectXmlReader.class, String.class, String.class)
                    .newInstance(getFileProject(), projectXmlReader, getOntMetaobs(), getBaseUri());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return task;
    }
}
