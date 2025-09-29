package com.example.management.view;

import com.example.management.controller.DashboardController;
import com.example.management.data.DataStore;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DashboardView extends BorderPane {
    private DashboardController controller;
    private final ItemView itemView;
    private final OwnerView ownerView;

    public DashboardView() {
        this.itemView = new ItemView();
        this.ownerView = new OwnerView(this.itemView);

        setTop(createHeader());
        setLeft(createSidebar());
        setCenter(this.itemView); // Default center view
    }

    private HBox createHeader() {
        Label title = new Label("Hogwarts Artifacts Management System");
        Label userLabel = new Label("Logged in as: " + DataStore.getInstance().getCurrentUser().getUsername());
        Button logoutButton = new Button("Logout");

        logoutButton.setOnAction(e -> {
            this.controller.handleLogout(); // Assuming handleLogout is defined in the controller
        });

        HBox header = new HBox(20, title, userLabel, logoutButton);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #d0d0d0;");
        return header;
    }

    private VBox createSidebar() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(10));
        menu.setStyle("-fx-background-color: #f0f0f0;");

        Button artifactBtn = new Button("Artifacts");
        Button wizardBtn = new Button("Wizards");
        artifactBtn.setMaxWidth(Double.MAX_VALUE);
        wizardBtn.setMaxWidth(Double.MAX_VALUE);

        artifactBtn.setOnAction(e -> this.setCenter(itemView));
        wizardBtn.setOnAction(e -> this.setCenter(ownerView));

        menu.getChildren().addAll(artifactBtn, wizardBtn);
        return menu;
    }

    public void setController(DashboardController dashboardController) {
        this.controller = dashboardController;
    }
}
