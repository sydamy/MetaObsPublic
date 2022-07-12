package fr.ufc.metaobs.controllers.services;

import fr.ufc.metaobs.controllers.services.tasks.ExportToOdsTask;
import fr.ufc.metaobs.controllers.services.tasks.ExportToSqlTask;
import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.model.export.Database;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.lang.reflect.InvocationTargetException;

/**
 * Classe Service qui permet de finir l'exportation (SQL, Ods) d'un projet.
 *
 * @see ExportToSqlTask
 * @see ExportToOdsTask
 */
public class FinishExportDatabaseService extends Service<Void> {

    private final Class<? extends Task<Void>> taskClass;

    private final ObjectProperty<Database> database;
    private final ObjectProperty<Project> project;

    public FinishExportDatabaseService(Class<? extends Task<Void>> taskClass) {
        this.taskClass = taskClass;
        this.database = new SimpleObjectProperty<>();
        this.project = new SimpleObjectProperty<>();
    }

    public Database getDatabase() {
        return database.get();
    }

    public void setDatabase(Database database) {
        this.database.set(database);
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
            task = taskClass.getConstructor(Project.class, Database.class)
                    .newInstance(getProject(), getDatabase());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return task;
    }

}
