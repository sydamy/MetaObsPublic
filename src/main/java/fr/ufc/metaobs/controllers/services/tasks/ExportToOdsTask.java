package fr.ufc.metaobs.controllers.services.tasks;

import com.github.jferard.fastods.AnonymousOdsFileWriter;
import com.github.jferard.fastods.OdsDocument;
import com.github.jferard.fastods.OdsFactory;
import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.model.export.Database;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Classe héritant de Task pour être exécutée en arrière-plan via un Service afin d'export le projet dans le format
 * Ods.
 *
 * @see Task
 * @see javafx.concurrent.Service
 */
public class ExportToOdsTask extends Task<Void> {

    private final Project project;

    private final Database database;

    public ExportToOdsTask(Project project, Database database) {
        this.project = project;
        this.database = database;
    }

    @Override
    protected Void call() {
        File odsFile = new File(project.getRepProject(), project.getName() + ".ods");
        Logger logger = Logger.getLogger("Metaobs");
        OdsFactory odsFactory = OdsFactory.create(logger, Locale.US);
        AnonymousOdsFileWriter writer = odsFactory.createWriter();
        OdsDocument document = database.toOds(writer);
        try {
            writer.saveAs(odsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
