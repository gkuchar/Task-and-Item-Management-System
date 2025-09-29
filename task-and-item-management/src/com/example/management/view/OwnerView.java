package com.example.management.view;

import com.example.management.controller.OwnerController;
import com.example.management.data.DataStore;
import com.example.management.model.Item;
import com.example.management.model.Owner;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class OwnerView extends VBox{
    private final OwnerController controller;
    private final TableView<Owner> ownerTable;
    private final ObservableList<Owner> ownerData;
    private final ItemView itemView;

    public OwnerView(ItemView itemView) {
        this.controller = new OwnerController();
        this.ownerTable = new TableView<>();
        this.ownerData = FXCollections.observableArrayList(controller.findAllOwners());
        this.itemView = itemView;

        setSpacing(10);
        setPadding(new Insets(10));
        getChildren().addAll(createTable(), createButtons());
    }

    private TableView<Owner> createTable() {
        TableColumn<Owner, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));

        TableColumn<Owner, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));

        TableColumn<Owner, Void> actionCol = new TableColumn<>("Actions");

        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button assignButton = new Button("Assign");
            private final HBox buttons = new HBox(5);

            {
                viewButton.setOnAction(e -> {
                    Owner owner = getTableView().getItems().get(getIndex());
                    showViewOwnerDialog(owner);
                });

                editButton.setOnAction(e -> {
                    Owner owner = getTableView().getItems().get(getIndex());
                    showEditOwnerDialog(owner);
                });

                deleteButton.setOnAction(e -> {
                    Owner owner = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setHeaderText("Delete Owner");
                    confirm.setContentText("Are you sure you want to delete \"" + owner.getName() + "\" and unassign their items?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            controller.deleteOwner(owner.getId());
                            ownerData.setAll(controller.findAllOwners());
                            itemView.refreshItems();
                        }
                    });
                });

                assignButton.setOnAction(e -> {
                    Owner owner = getTableView().getItems().get(getIndex());
                    showAssignItemDialogFor(owner);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= ownerData.size()) {
                    setGraphic(null);
                } else {
                    buttons.getChildren().clear();
                    buttons.getChildren().add(viewButton);
                    if (DataStore.getInstance().getCurrentUser().isAdmin()) {
                        buttons.getChildren().addAll(editButton, deleteButton, assignButton);
                    }
                    setGraphic(buttons);
                }
            }
        });

        ownerTable.getColumns().setAll(idCol, nameCol, actionCol);
        ownerTable.setItems(ownerData);
        ownerTable.setPrefHeight(300);
        return ownerTable;
    }

    private HBox createButtons() {
        Button addBtn = new Button("Add");
        HBox buttonBox = new HBox(10);
        if (DataStore.getInstance().getCurrentUser().isAdmin()) {
            addBtn.setOnAction(e -> showAddOwnerDialog());
            buttonBox.getChildren().add(addBtn);
        }
        return buttonBox;
    }

    private void showAddOwnerDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Owner");
        dialog.setHeaderText("Enter owner name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                Owner owner = controller.addOwner(name);
                ownerData.setAll(controller.findAllOwners());
                ownerTable.getSelectionModel().select(owner);
                itemView.refreshItems();
            }
        });
    }

    private void showEditOwnerDialog(Owner owner) {
        if (owner == null) return;

        TextInputDialog dialog = new TextInputDialog(owner.getName());
        dialog.setTitle("Edit Owner");
        dialog.setHeaderText("Edit owner name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                controller.updateOwner(owner.getId(), name);
                ownerData.setAll(controller.findAllOwners());
                itemView.refreshItems();
            }
        });
    }

    private void showAssignItemDialogFor(Owner owner) {
        var unowned = FXCollections.observableArrayList(controller.getUnassignedItems());

        if (unowned.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No unassigned items available.");
            alert.setHeaderText("Nothing to assign");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<Item> dialog = new ChoiceDialog<>(unowned.get(0), unowned);
        dialog.setTitle("Assign Item");
        dialog.setHeaderText("Assign to " + owner.getName());

        dialog.showAndWait().ifPresent(item -> {
            boolean success = controller.assignItemToOwner(owner, item);
            if (success) {
                ownerData.setAll(controller.findAllOwners());
                ownerTable.getSelectionModel().select(owner);
                itemView.refreshItems();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Error: Cannot Assign " + item.getName()
                        + " to a Owner. " + item.getName() + "'s condition is < 10");
                alert.setHeaderText("Poor Condition");
                alert.showAndWait();
                return;
            }
        });
    }

    private void showViewOwnerDialog(Owner owner) {
        if (owner == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Owner Details");
        dialog.setHeaderText("Viewing: " + owner.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(owner.getId()).append("\n");
        sb.append("Name: ").append(owner.getName()).append("\n");
        sb.append("Items:\n");
        for (Item a : owner.getItems()) {
            sb.append("  - ").append(a.getName()).append(" (ID: ").append(a.getId()).append(")\n");
        }

        TextArea details = new TextArea(sb.toString());
        details.setEditable(false);
        details.setWrapText(true);

        VBox content = new VBox(details);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}

