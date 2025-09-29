package com.example.management;

import com.example.management.controller.LoginController;
import com.example.management.data.DataStore;
import com.example.management.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManagementApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        LoginView loginView = new LoginView();
        LoginController loginController = new LoginController(loginView);

        Scene scene = new Scene(loginView, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Task and Item Management System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        DataStore.getInstance().saveData();
    }
}
