package fr.ufc.metaobs.controllers.services.tasks;

import fr.ufc.metaobs.handlers.ProjectXmlReader;
import fr.ufc.metaobs.model.Project;
import fr.ufc.metaobs.utils.FileSystemUtils;
import javafx.concurrent.Task;

import java.io.File;

/**
 * Classe héritant de Task pour être exécutée en arrière-plan via un Service afin de créer un nouveau projet.
 *
 * @see Task
 * @see javafx.concurrent.Service
 */
public class CreateProjectTask extends Task<Project> {

    private final File fileProject;
    private final String ontMetaobs;
    private final String baseUri;

    /**
     * Construit une tâche pour créer un nouveau projet
     *
     * @param fileProject le fichier du projet à créer
     * @param useless     inutile ici mais nécessaire pour pouvoir utiliser de façon similaire une OpenProjectTask
     * @param ontMetaobs  le chemin vers l'ontologie Metaobs
     * @param baseUri     la baseUri pour résoudre les uri relatives
     * @see OpenProjectTask
     */
    public CreateProjectTask(File fileProject, ProjectXmlReader useless, String ontMetaobs, String baseUri) {
        this.fileProject = fileProject;
        this.ontMetaobs = ontMetaobs;
        this.baseUri = baseUri;
    }

    @Override
    protected Project call() {
        File repProject = fileProject.getParentFile();
        if (!repProject.exists()) {
            repProject.mkdir();
        }
        Project project = new Project(fileProject.getName(), FileSystemUtils.addFileUriScheme(ontMetaobs), baseUri);
        project.setRepProject(repProject.getAbsolutePath());
        return project;
    }
}
