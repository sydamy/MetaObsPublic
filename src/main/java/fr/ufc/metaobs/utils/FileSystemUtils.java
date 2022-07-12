package fr.ufc.metaobs.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemUtils {

    public static final String BASE_URI_PROPERTIES_FILE = "baseURI.properties";
    public static final String ONTO_PATH = "./data/onto/";
    public static final String PROJECTS_PATH = "./data/projects/";

    /**
     * Transforme un chemin de fichier en URI
     *
     * @param filePath le chemin du fichier
     * @return l'uri en String
     */
    public static String addFileUriScheme(String filePath) {
        String os = System.getProperty("os.name");
        //si c'est déjà une uri
        if (filePath.startsWith("file://")) {
            return filePath;
        }
        String res;
        if (os.contains("Windows")) {
            res = "file:///" + filePath.replace('\\', '/');
        } else {
            res = "file://" + filePath;
        }
        return res;
    }

    /**
     * Relocalise les ontologies en considérant le dossier 'onto' comme étant le fichier donné en paramètre.
     * Cette méthode relocalise aussi les ontologies des projets.
     *
     * @param ontoDirectory le dossier onto/ normalement
     * @return la nouvelle URI en chaîne de caractères du dossier onto
     */
    public static String relocateOntologies(File ontoDirectory) {
        //regex pour trouver l'ancienne URI utilisée, normalement un chemin absolu donc commençant
        //par file: et finissant par /data/
        String oldUri = "file:\\/.*\\/data\\/";
        //étant donné qu'on relocalise aussi les ontologies des projets, on remplacera le chemin
        //par le chemin du dossier data/ et pas celui du dossier onto/
        String newUri = addFileUriScheme(ontoDirectory.getParentFile().getAbsolutePath()) + "/";
        //pour le fichier properties, on prend le chemin du dossier onto/
        String newUriProperties = addFileUriScheme(ontoDirectory.getAbsolutePath()) + "/";
        List<String> filePaths = getAllFilePaths(ontoDirectory.getParentFile());
        for (String path : filePaths) {
            File file = new File(path);
            replaceStrInFile(file, oldUri, newUri);
        }
        File propertiesFile = new File(ontoDirectory, BASE_URI_PROPERTIES_FILE);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(propertiesFile);
            Properties properties = new Properties();
            properties.setProperty("BASE", newUriProperties);
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newUriProperties;
    }

    /**
     * Remplace les occurences d'une regex dans un fichier texte par une autre chaîne de caractères.
     *
     * @param file      le fichier texte dans lequel remplacer le texte
     * @param regex     la regex qui sert à trouver les occurences de texte à remplacer
     * @param newString la chaîne de caractères à placer au lieu des occurences de la regex
     */
    public static void replaceStrInFile(File file, String regex, String newString) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            Stream<String> lines = reader.lines();
            String newLines = lines.map(str -> str.replaceAll(regex, newString)).collect(Collectors.joining(System.lineSeparator()));
            fileWriter = new FileWriter(file);
            writer = new BufferedWriter(fileWriter);
            writer.write(newLines);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retourne la liste des chemins des fichiers owl présents dans le dossier directory et ses sous-dossiers.
     *
     * @param directory le dossier pour lequel lister les fichiers owls
     * @return la liste des chemins des fichiers owl
     */
    private static List<String> getAllFilePaths(File directory) {
        List<String> res = new ArrayList<>();
        File[] files = directory.listFiles(file -> file.isDirectory() || file.getName().endsWith("owl"));
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    res.addAll(getAllFilePaths(file));
                } else {
                    res.add(file.getAbsolutePath());
                }
            }
        }
        return res;
    }

    /**
     * Retourne la propriété BASE du fichier baseURI.properties qui définit l'uri de base des ontologies.
     *
     * @param ontoDirectory le dossier onto/ normalement
     * @return la propriété BASE définissant l'uri de base des ontologies
     */
    public static String getBaseURI(File ontoDirectory) {
        File[] files = ontoDirectory.listFiles((file, s) -> s.equals(BASE_URI_PROPERTIES_FILE));
        String res = null;
        if (files != null && files.length > 0) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(files[0]);
                Properties properties = new Properties();
                properties.load(fileInputStream);
                res = properties.getProperty("BASE");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

}
