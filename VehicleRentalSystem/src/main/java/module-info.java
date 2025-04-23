module com.example.vehiclerentalsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.example.vehiclerentalsystem;
    exports com.example.vehiclerentalsystem.controllers;
    exports com.example.vehiclerentalsystem.models;
    exports com.example.vehiclerentalsystem.utils;

    opens com.example.vehiclerentalsystem to javafx.fxml;
    opens com.example.vehiclerentalsystem.controllers to javafx.fxml;
}
