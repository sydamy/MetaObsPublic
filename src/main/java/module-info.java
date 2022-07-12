module fr.ufc.metaobs {
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.apache.jena.core;
    requires org.apache.jena.iri;
    requires fastods;
    requires jdom2;

    opens fr.ufc.metaobs to javafx.fxml;
    opens fr.ufc.metaobs.model to javafx.base;
    opens fr.ufc.metaobs.view to javafx.fxml;
    opens fr.ufc.metaobs.utils to javafx.fxml;
    opens fr.ufc.metaobs.view.forms to javafx.fxml;
    opens fr.ufc.metaobs.view.dialogs to javafx.fxml;
    exports fr.ufc.metaobs;
}