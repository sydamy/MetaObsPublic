package fr.ufc.metaobs.controllers.services.tasks;

import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.visitors.ProjectVisitor;
import javafx.concurrent.Task;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Classe héritant de Task pour être exécutée en arrière-plan via un Service afin de sauvegarder le projet.
 *
 * @see Task
 * @see javafx.concurrent.Service
 */
public class SaveProjectTask extends Task<Void> {

    private final Project project;
    private final ProjectVisitor<Element> projectVisitor;

    public SaveProjectTask(Project project, ProjectVisitor<Element> projectVisitor) {
        this.project = project;
        this.projectVisitor = projectVisitor;
    }

    @Override
    protected Void call() {
        File directory = new File(project.getRepProject());
        if (!directory.exists()) {
            directory.mkdir();
        }
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            Element root = projectVisitor.visit(project);
            Document document = new Document(root);
            File file = new File(project.getRepProject(), project.getName() + ".xml");
            xmlOutputter.output(document, new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
