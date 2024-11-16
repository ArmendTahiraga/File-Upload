module com.armendtahiraga.fileupload {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.armendtahiraga.fileupload to javafx.fxml;
    exports com.armendtahiraga.fileupload;
}