module com.armendtahiraga.fileupload {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.armendtahiraga.fileupload to javafx.fxml;
    exports com.armendtahiraga.fileupload;
}