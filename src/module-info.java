module ProyectoClient {
	requires javafx.controls;
	requires javafx.fxml;
    requires java.desktop;

    exports domain;
    opens application to javafx.graphics, javafx.fxml;
}
