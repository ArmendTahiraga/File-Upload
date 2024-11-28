module com.armendtahiraga.fileupload {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;

    opens com.fileupload to javafx.fxml;
    exports com.fileupload;
}