package com.example.hogwarts.view;

import com.example.hogwarts.controller.ArtifactController;
import com.example.hogwarts.data.DataStore;
import com.example.hogwarts.model.Artifact;
import com.example.hogwarts.model.Transaction;
import com.example.hogwarts.model.Wizard;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ArtifactView extends VBox{
    private final ArtifactController controller;
    private final TableView<Artifact> artifactTable;
    private final ObservableList<Artifact> artifactData;

    public ArtifactView() {
        this.controller = new ArtifactController();
        this.artifactTable = new TableView<>();
        this.artifactData = FXCollections.observableArrayList(controller.findAllArtifacts());

        setSpacing(10);
        setPadding(new Insets(10));
        getChildren().addAll(createSearchBar(), createTable(), createButtons());
    }

    private TableView<Artifact> createTable() {
        TableColumn<Artifact, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));

        TableColumn<Artifact, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));

        TableColumn<Artifact, Number> conditionCol = new TableColumn<>("Condition");
        conditionCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getCondition()));

        TableColumn<Artifact, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(cell ->  {
            Artifact artifact = cell.getValue();
            String ownerString;
            if (artifact.getOwner() == null) {
                ownerString = "--";
            }
            else {
                ownerString = artifact.getOwner().getName();
            }
            return new ReadOnlyStringWrapper(ownerString);
        });

        TableColumn<Artifact, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button unassignButton = new Button("Unassign");
            private final Button historyButton = new Button("History");
            private final Button repairButton = new Button("Repair");
            private final HBox buttons = new HBox(5);
            {
                viewButton.setOnAction(e -> {
                    Artifact artifact = getTableView().getItems().get(getIndex());
                    showViewArtifactDialog(artifact);
                });

                historyButton.setOnAction(e -> {
                    Artifact artifact = getTableView().getItems().get(getIndex());
                    showHistoryArtifactDialog(artifact);
                });

                editButton.setOnAction(e -> {
                    Artifact artifact = getTableView().getItems().get(getIndex());
                    showEditArtifactDialog(artifact);
                });

                repairButton.setOnAction(e -> {
                    Artifact artifact = getTableView().getItems().get(getIndex());
                    showRepairArtifactDialog(artifact);
                    refreshArtifacts();
                });

                unassignButton.setOnAction( e -> {
                    Artifact artifact = getTableView().getItems().get(getIndex());
                    unassignArtifactFromWizard(artifact);
                    refreshArtifacts();
                });

                deleteButton.setOnAction(e -> {
                    Artifact artifact = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setHeaderText("Delete Artifact");
                    confirm.setContentText("Are you sure you want to delete \"" + artifact.getName() + "\"?");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            controller.deleteArtifact(artifact.getId());
                            refreshArtifacts();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= artifactData.size()) {
                    setGraphic(null);
                } else {
                    Artifact artifact = getTableView().getItems().get(getIndex());
                    buttons.getChildren().clear();
                    buttons.getChildren().add(viewButton);
                    buttons.getChildren().add(historyButton);
                    if (DataStore.getInstance().getCurrentUser().isAdmin()) {
                        buttons.getChildren().addAll(editButton, repairButton, deleteButton);
                        if(artifact.getOwner() != null) {
                            buttons.getChildren().add(unassignButton);
                        }
                    }
                    setGraphic(buttons);
                }
            }
        });

        artifactTable.getColumns().setAll(idCol, nameCol, conditionCol, actionCol, ownerCol);
        artifactTable.setItems(artifactData);
        artifactTable.setPrefHeight(300);
        actionCol.setPrefWidth(360);
        ownerCol.setPrefWidth(150);

        return artifactTable;
    }

    private void unassignArtifactFromWizard(Artifact artifact) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Unassignment");
        confirm.setHeaderText("Unassign Artifact");
        String message = "Are you sure you want to unassign '" + artifact.getName() + "' from '" + artifact.getOwner().getName() + "'?";
        confirm.setContentText(message);
        confirm.getDialogPane().setPrefWidth(message.length() * 7);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                controller.unassignArtifactFromWizard(artifact.getOwner(), artifact);
                refreshArtifacts();
            }
        });


    }

    private HBox createButtons() {
        Button addBtn = new Button("Add");
        HBox box = new HBox(10);
        if (DataStore.getInstance().getCurrentUser().isAdmin()) {
            addBtn.setOnAction(e -> showAddArtifactDialog());
            box.getChildren().add(addBtn);
        }
        return box;
    }

    private HBox createSearchBar() {
        Label label = new Label("Search By Artifact Name: ");
        TextField search = new TextField();
        search.setPrefWidth(250);
        HBox box = new HBox(30);
        box.getChildren().add(label);
        box.getChildren().add(search);
        search.setOnKeyReleased(e -> searchArtifacts(search.getText().toLowerCase()));
        return box;
    }

    private void searchArtifacts(String searchString) {
        if (searchString.isEmpty()) {
            artifactTable.setItems(artifactData);
            refreshArtifacts();
            return;
        }

        FilteredList<Artifact> filteredData = new FilteredList<>(artifactData);

        filteredData.setPredicate(item -> {
            return item.getName().toLowerCase().contains(searchString);
        });

        artifactTable.setItems(filteredData);
        refreshArtifacts();

    }



    private void showAddArtifactDialog() {
        Dialog<Artifact> dialog = new Dialog<>();
        dialog.setTitle("Add Artifact");
        dialog.setHeaderText("Enter artifact details:");

        TextField nameField = new TextField();
        TextArea descField = new TextArea();

        VBox content = new VBox(10, new Label("Name:"), nameField, new Label("Description:"), descField);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Artifact artifact = controller.addArtifact(nameField.getText(), descField.getText());
                refreshArtifacts();
                return artifact;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(artifact -> {
            artifactData.setAll(controller.findAllArtifacts());
            artifactTable.getSelectionModel().select(artifact);
        });
    }

    private void showRepairArtifactDialog(Artifact artifact) {
        if (artifact == null) return;
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Repair Artifact");
        dialog.setHeaderText("Repair the condition of " + artifact.getName() + ": ");


        int condition = artifact.getCondition();
        Label label = new Label("Current condition for " + artifact.getName() + " is: " + condition);
        Label label2 = new Label("Enter the amount to repair (increase) the condition by");
        Label label3 = new Label("(It must be an integer 0 to 100, condition cannot exceed 100)");

        TextField amount = new TextField();

        Button button = new Button("Repair");

        VBox content = new VBox(label, label2, label3, amount, button);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        button.setOnAction(e -> {
            try {
                if (Integer.parseInt(amount.getText()) >= 0 && Integer.parseInt(amount.getText()) <= 100) {
                    boolean hitMax = controller.repairArtifact(artifact, Integer.parseInt(amount.getText()));
                    repairConfirmation(artifact, Integer.parseInt(amount.getText()), dialog, hitMax);
                    refreshArtifacts();
                }
                else {
                    invalidRepairPopup();
                }
            } catch (NumberFormatException f) {
                invalidRepairPopup();
            }
        });
        dialog.showAndWait();




    }

    private void repairConfirmation(Artifact artifact, int amount, Dialog dialog, boolean hitMax) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Artifact Repaired");
        confirm.setHeaderText("Artifact has been repaired");
        String message = artifact.getName() + " was repaired by " + amount;
        if (hitMax) { message = message + ", condition exceeded 100 and was reduced back to 100";}
        confirm.setContentText(message);
        confirm.getDialogPane().setPrefWidth(message.length() * 7);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                confirm.close();
                dialog.close();
            }
        });
    }

    private void invalidRepairPopup() {
        Alert confirm = new Alert(Alert.AlertType.ERROR);
        confirm.setTitle("Repair Error");
        confirm.setHeaderText("Please Enter a valid repair number");
        confirm.setContentText("Value must be an integer from 0 to 100");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.CLOSE) {
            }
        });
    }

    private void showEditArtifactDialog(Artifact artifact) {
        if (artifact == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Artifact");
        dialog.setHeaderText("Edit artifact details:");

        TextField nameField = new TextField(artifact.getName());
        TextArea descField = new TextArea(artifact.getDescription());

        VBox content = new VBox(10, new Label("Name:"), nameField, new Label("Description:"), descField);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                controller.updateArtifact(artifact.getId(), nameField.getText(), descField.getText());
                refreshArtifacts();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showViewArtifactDialog(Artifact artifact) {
        if (artifact == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Artifact Details");
        dialog.setHeaderText("Viewing: " + artifact.getName());

        String ownerName = artifact.getOwner() != null ? artifact.getOwner().getName() : "Unassigned";
        TextArea details = new TextArea(
                "ID: " + artifact.getId() + "\n" +
                        "Name: " + artifact.getName() + "\n" +
                        "Description: " + artifact.getDescription() + "\n" +
                        "Condition: " + artifact.getCondition() + "\n" +
                        "Owner: " + ownerName
        );
        details.setEditable(false);
        details.setWrapText(true);

        VBox content = new VBox(details);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public void showHistoryArtifactDialog(Artifact artifact) {
        if (artifact == null) return;
        List<Transaction> copy = artifact.getHistory().reversed();
        ObservableList<Transaction> historyData = FXCollections.observableArrayList(copy);
        Dialog dialog = new Dialog();
        dialog.setTitle("Artifact History");
        dialog.setHeaderText("Assignment History of: " + artifact.getName());

        TableView<Transaction> table = new TableView();

        TableColumn<Transaction, String> timeStampCol = new TableColumn<>("Time Stamp");
        timeStampCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDateTimeString()));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getType()));

        TableColumn<Transaction, String> toWizardCol = new TableColumn<>("To Wizard");
        toWizardCol.setCellValueFactory(cell ->  {
            Transaction t = cell.getValue();
            String wizString;
            if (t.getToWizard() == null) {
                wizString = "--";
            }
            else {
                wizString = t.getToWizard().getName();
            }
            return new ReadOnlyStringWrapper(wizString);
        });

        TableColumn<Transaction, String> fromWizardCol = new TableColumn<>("From Wizard");
        fromWizardCol.setCellValueFactory(cell ->  {
            Transaction t = cell.getValue();
            String wizString;
            if (t.getFromWizard() == null) {
                wizString = "--";
            }
            else {
                wizString = t.getFromWizard().getName();
            }
            return new ReadOnlyStringWrapper(wizString);
        });

        table.getColumns().setAll(timeStampCol, typeCol, toWizardCol, fromWizardCol);
        table.setItems(historyData);
        table.setPrefHeight(300);
        table.setPrefWidth(600);

        HBox content = new HBox(table);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();

    }

    public void refreshArtifacts() {
        artifactData.setAll(controller.findAllArtifacts());
    }
}
