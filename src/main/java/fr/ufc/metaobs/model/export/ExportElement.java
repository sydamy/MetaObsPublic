package fr.ufc.metaobs.model.export;

public interface ExportElement<T,P> {

    String NEW_LINE = System.lineSeparator();

    String toSqlString();

    T toOds(P parent);

}
