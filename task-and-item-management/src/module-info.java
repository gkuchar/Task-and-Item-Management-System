module javafxapp {
    requires javafx.controls;
    requires javafx.fxml;

    // Jackson
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Open packages for reflection
    opens com.example.management to javafx.fxml; // if you have FXML controllers here
    opens com.example.management.model to com.fasterxml.jackson.databind; // <-- key line

    // Exports (as needed)
    exports com.example.management;
    exports com.example.management.model;
}