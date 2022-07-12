package fr.ufc.metaobs.controllers.services.tasks;

import fr.ufc.metaobs.handlers.ProjectXmlReader;
import fr.ufc.metaobs.model.Project;
import javafx.concurrent.Task;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;

/**
 * Classe héritant de Task pour être exécutée en arrièe-plan via un Service afin d'ouvrir un projet.
 *
 * @see Task
 * @see javafx.concurrent.Service
 */
public class OpenProjectTask extends Task<Project> {

    private final File fileProject;
    private final ProjectXmlReader projectXmlReader;
    private final String baseUri;

    /**
     * Construit une tâche pour ouvrir un projet
     *
     * @param fileProject      le fichier du projet
     * @param projectXmlReader l'instance de la classe permettant de lire le xml et d'en récupérer le projet
     * @param useless          inutile ici mais nécessaire pour pouvoir utiliser de façon similaire une CreateProjectTask
     * @param baseUri          la baseUri pour résoudre les uri relatives
     * @see CreateProjectTask
     */
    public OpenProjectTask(File fileProject, ProjectXmlReader projectXmlReader, String useless, String baseUri) {
        this.fileProject = fileProject;
        this.projectXmlReader = projectXmlReader;
        this.baseUri = baseUri;
    }

    @Override
    protected Project call() {
        Project openedProject = null;
        try {
            openedProject = projectXmlReader.open(fileProject, baseUri);
            openedProject.setRepProject(fileProject.getParent());
            File[] owlFiles = fileProject.getParentFile().listFiles(file -> file.getPath().endsWith(".owl"));
            openedProject.setProjectOwls(owlFiles);
            openedProject.loadProjectOwls();
        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
        return openedProject;
    }

}
