package com.example.management.controller;

import com.example.management.data.DataStore;
import com.example.management.model.User;
import com.example.management.view.DashboardView;
import com.example.management.view.LoginView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {
    private final LoginView loginView;

    public LoginController(LoginView loginView) {
        this.loginView = loginView;
        this.loginView.setController(this);
    }

    public void handleLogin(String username, String password) {
        User user = DataStore.getInstance().authenticate(username, password);
        if (user != null) { // User authenticated successfully
            // Save the authenticated user
            DataStore.getInstance().setCurrentUser(user);
            DashboardView dashboardView = new DashboardView(); // Create the dashboard view ONLY after successful login
            DashboardController dashboardController = new DashboardController(dashboardView, this.loginView); // Create the controller for the dashboard view

            Scene scene = this.loginView.getScene();
            if (scene != null) {
                // Replace the login view with the dashboard view
                Stage stage = (Stage) scene.getWindow();
                Scene dashScene = new Scene(dashboardView, 1200, 400);
                stage.setScene(dashScene);
                stage.show();

            }
        } else { // Authentication failed
            this.loginView.getMessageLabel().setText("Invalid username or password.");
        }
    }
}
