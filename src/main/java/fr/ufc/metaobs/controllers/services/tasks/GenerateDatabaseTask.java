package fr.ufc.metaobs.controllers.services.tasks;

import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.model.export.Database;
import fr.ufc.metaobs.visitors.ProjectVisitor;
import javafx.concurrent.Task;

/**
 * Classe héritant de Task pour être exécutée en arrière-plan via un Service afin de générer le modèle d'exportation.
 *
 * @see Task
 * @see javafx.concurrent.Service
 */
public class GenerateDatabaseTask extends Task<Database> {

    private final Project project;
    private final ProjectVisitor<Database> visitorSql;

    public GenerateDatabaseTask(Project project, ProjectVisitor<Database> visitorSql) {
        this.project = project;
        this.visitorSql = visitorSql;
    }

    @Override
    protected Database call() {
        return visitorSql.visit(project);
    }
}
