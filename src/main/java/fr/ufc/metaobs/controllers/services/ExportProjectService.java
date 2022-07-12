package fr.ufc.metaobs.controllers.services;

import fr.ufc.metaobs.controllers.services.tasks.GenerateDatabaseTask;
import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.model.export.Database;
import fr.ufc.metaobs.visitors.ProjectVisitor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * Classe Service qui permet de commencer l'exportation un projet.
 *
 * @see GenerateDatabaseTask
 */
public class ExportProjectService extends Service<Database> {

    private final Class<? extends Task<Database>> taskClass;
    private final ProjectVisitor<?> projectVisitor;
    private final ObjectProperty<Project> project;
    //Consumer à récupérer et exécuter à la fin de la tâche
    private Consumer<Database> callback;

    public ExportProjectService(ProjectVisitor<?> projectVisitor, Class<? extends Task<Database>> taskClass) {
        this.taskClass = taskClass;
        this.projectVisitor = projectVisitor;
        this.project = new SimpleObjectProperty<>();
    }

    public Project getProject() {
        return project.get();
    }

    public void setProject(Project project) {
        this.project.set(project);
    }

    public Consumer<Database> getCallback() {
        return callback;
    }

    public void setCallback(Consumer<Database> callback) {
        this.callback = callback;
    }

    @Override
    protected Task<Database> createTask() {
        Task<Database> task = null;
        try {
            task = taskClass.getConstructor(Project.class, ProjectVisitor.class)
                    .newInstance(getProject(), projectVisitor);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return task;
    }
}
