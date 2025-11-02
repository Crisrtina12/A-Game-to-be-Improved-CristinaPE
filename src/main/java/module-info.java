module com.ieselgrao.gametofork {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.ieselgrao.gametofork to javafx.fxml;
    exports com.ieselgrao.gametofork;
    exports com.ieselgrao.gametofork.controller;
    opens com.ieselgrao.gametofork.controller to javafx.fxml;
    exports com.ieselgrao.gametofork.model;
    opens com.ieselgrao.gametofork.model to javafx.fxml;
}