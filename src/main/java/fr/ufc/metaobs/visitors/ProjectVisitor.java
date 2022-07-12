package fr.ufc.metaobs.visitors;

import fr.ufc.metaobs.model.*;

public interface ProjectVisitor<T> {

    T visit(Project project);

    void visit(T parent, Observatory observatory);

    void visit(T parent, Entity entity);

    void visit(T parent, Referential referential);

    void visit(T parent, Characteristic characteristic);

    void visit(T parent, Observation observation);

    void visit(T parent, Context context);

    void visit(T parent, ContextActor contextActor);

    void visit(T parent, ContextLocation contextLocation);

    void visit(T parent, ContextDate contextDate);

}
