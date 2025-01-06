module com.devops.ninjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.swing;

    opens com.devops.ninjava to javafx.fxml;
    exports com.devops.ninjava;
    exports com.devops.ninjava.manager;

    opens com.devops.ninjava.model.hero to javafx.fxml;
    opens com.devops.ninjava.model.projectile to javafx.fxml;
}