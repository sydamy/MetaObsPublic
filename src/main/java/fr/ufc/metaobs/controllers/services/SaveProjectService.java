package fr.ufc.metaobs.controllers.services;

import fr.ufc.metaobs.controllers.services.tasks.GenerateDatabaseTask;
import fr.ufc.metaobs.controllers.services.tasks.SaveProjectTask;
import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.visitors.ProjectVisitor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.lang.reflect.InvocationTargetException;

/**
 * Classe Service qui permet de sauvegarder le projet en arrière-plan.
 *
 * @see SaveProjectTask
 */
public class SaveProjectService extends Service<Void> {

    private final Class<? extends Task<Void>> taskClass;
    private final ProjectVisitor<?> projectVisitor;

    private final ObjectProperty<Project> project;

    public SaveProjectService(ProjectVisitor<?> projectVisitor, Class<? extends Task<Void>> taskClass) {
        this.projectVisitor = projectVisitor;
        this.taskClass = taskClass;
        this.project = new SimpleObjectProperty<>();
    }

    public Project getProject() {
        return project.get();
    }

    public void setProject(Project project) {
        this.project.set(project);
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = null;
        try {
            //on crée la Task grâce à la reflection
            //il faut donc que les classes à instancier aient un constructeur avec la même signature
            task = taskClass
                    .getConstructor(Project.class, ProjectVisitor.class)
                    .newInstance(getProject(), projectVisitor);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return task;
    }
}
