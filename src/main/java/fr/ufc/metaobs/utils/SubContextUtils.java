package fr.ufc.metaobs.utils;

import fr.ufc.metaobs.model.ContextActor;
import fr.ufc.metaobs.model.ContextLocation;
import fr.ufc.metaobs.model.Field;

import java.util.stream.Collectors;

public class SubContextUtils {

    public static String getActorName(ContextActor contextActor, String separator) {
        StringBuilder sb = new StringBuilder();
        while (contextActor != null) {
            sb.append(contextActor.getName());
            sb.append(separator);
            contextActor = contextActor.getRefContextActor();
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

    public static String getActorPropeties(ContextActor contextActor, String separator) {
        StringBuilder sb = new StringBuilder();
        while (contextActor != null) {
            for (Field field : contextActor.getPropertiesList()) {
                sb.append(field.getName()).append(":").append(field.getTypeSize().toString()).append(System.lineSeparator());
            }
            sb.append(separator);
            contextActor = contextActor.getRefContextActor();
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

    public static String getActorPropertiesNames(ContextActor contextActor, String separator) {
        StringBuilder sb = new StringBuilder();
        while (contextActor != null) {
            sb.append(contextActor.getPropertiesList()
                    .stream()
                    .map(Field::getName)
                    .collect(Collectors.joining(","))
            );
            sb.append(separator);
            contextActor = contextActor.getRefContextActor();
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

    public static String getLocationName(ContextLocation contextLocation, String separator) {
        StringBuilder sb = new StringBuilder();
        while (contextLocation != null) {
            sb.append(contextLocation.getName());
            sb.append(separator);
            contextLocation = contextLocation.getRefLocation();
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

    public static String getLocationProperties(ContextLocation contextLocation, String separator) {
        StringBuilder sb = new StringBuilder();
        while (contextLocation != null) {
            for (Field field : contextLocation.getPropertiesList()) {
                sb.append(field.getName()).append(":").append(field.getTypeSize().toString()).append(System.lineSeparator());
            }
            sb.append(separator);
            contextLocation = contextLocation.getRefLocation();
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

    public static String getLocationPropertiesNames(ContextLocation contextLocation, String separator) {
        StringBuilder sb = new StringBuilder();
        while (contextLocation != null) {
            sb.append(contextLocation.getPropertiesList()
                    .stream()
                    .map(Field::getName)
                    .collect(Collectors.joining(","))
            );
            sb.append(separator);
            contextLocation = contextLocation.getRefLocation();
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

}
