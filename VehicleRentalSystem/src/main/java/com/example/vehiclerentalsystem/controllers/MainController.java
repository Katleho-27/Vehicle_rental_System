package com.example.vehiclerentalsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentPane;

    public void setView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/vehiclerentalsystem/" + fxmlFile));
            Node node = loader.load();
            contentPane.getChildren().setAll(node);  // replace everything in the contentPane with new node
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
