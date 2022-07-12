package fr.ufc.metaobs.controllers.services.tasks;

import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.model.export.Database;
import javafx.concurrent.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe héritant de Task pour être exécutée en arrière-plan via un Service afin de générer un script SQL pour le projet.
 *
 * @see Task
 * @see javafx.concurrent.Service
 */
public class ExportToSqlTask extends Task<Void> {

    private final Project project;

    private final Database database;

    public ExportToSqlTask(Project project, Database database) {
        this.project = project;
        this.database = database;
    }

    @Override
    protected Void call() {
        File sqlFile = new File(project.getRepProject(), project.getName() + "Bd.sql");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(sqlFile));
            writer.append(database.toSqlString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
