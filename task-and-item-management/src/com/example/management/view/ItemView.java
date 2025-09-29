package com.example.management.view;

import com.example.management.controller.ItemController;
import com.example.management.data.DataStore;
import com.example.management.model.Item;
import com.example.management.model.Transaction;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ItemView extends VBox{
    private final ItemController controller;
    private final TableView<Item> itemTable;
    private final ObservableList<Item> itemData;

    public ItemView() {
        this.controller = new ItemController();
        this.itemTable = new TableView<>();
        this.itemData = FXCollections.observableArrayList(controller.findAllItems());

        setSpacing(10);
        setPadding(new Insets(10));
        getChildren().addAll(createSearchBar(), createTable(), createButtons());
    }

    private TableView<Item> createTable() {
        TableColumn<Item, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));

        TableColumn<Item, Number> conditionCol = new TableColumn<>("Condition");
        conditionCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getCondition()));

        TableColumn<Item, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(cell ->  {
            Item item = cell.getValue();
            String ownerString;
            if (item.getOwner() == null) {
                ownerString = "--";
            }
            else {
                ownerString = item.getOwner().getName();
            }
            return new ReadOnlyStringWrapper(ownerString);
        });

        TableColumn<Item, Void> actionCol = new TableColumn<>("Actions");
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
                    Item item = getTableView().getItems().get(getIndex());
                    showViewItemDialog(item);
                });

                historyButton.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());
                    showHistoryItemDialog(item);
                });

                editButton.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());
                    showEditItemDialog(item);
                });

                repairButton.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());
                    showRepairItemDialog(item);
                    refreshItems();
                });

                unassignButton.setOnAction( e -> {
                    Item item = getTableView().getItems().get(getIndex());
                    unassignItemFromOwner(item);
                    refreshItems();
                });

                deleteButton.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setHeaderText("Delete Item");
                    confirm.setContentText("Are you sure you want to delete \"" + item.getName() + "\"?");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            controller.deleteItem(item.getId());
                            refreshItems();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void old_item, boolean empty) {
                super.updateItem(old_item, empty);
                if (empty || getIndex() >= itemData.size()) {
                    setGraphic(null);
                } else {
                    Item item = getTableView().getItems().get(getIndex());
                    buttons.getChildren().clear();
                    buttons.getChildren().add(viewButton);
                    buttons.getChildren().add(historyButton);
                    if (DataStore.getInstance().getCurrentUser().isAdmin()) {
                        buttons.getChildren().addAll(editButton, repairButton, deleteButton);
                        if(item.getOwner() != null) {
                            buttons.getChildren().add(unassignButton);
                        }
                    }
                    setGraphic(buttons);
                }
            }
        });

        itemTable.getColumns().setAll(idCol, nameCol, conditionCol, actionCol, ownerCol);
        itemTable.setItems(itemData);
        itemTable.setPrefHeight(300);
        actionCol.setPrefWidth(360);
        ownerCol.setPrefWidth(150);

        return itemTable;
    }

    private void unassignItemFromOwner(Item item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Unassignment");
        confirm.setHeaderText("Unassign Item");
        String message = "Are you sure you want to unassign '" + item.getName() + "' from '" + item.getOwner().getName() + "'?";
        confirm.setContentText(message);
        confirm.getDialogPane().setPrefWidth(message.length() * 7);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                controller.unassignItemFromOwner(item.getOwner(), item);
                refreshItems();
            }
        });


    }

    private HBox createButtons() {
        Button addBtn = new Button("Add");
        HBox box = new HBox(10);
        if (DataStore.getInstance().getCurrentUser().isAdmin()) {
            addBtn.setOnAction(e -> showAddItemDialog());
            box.getChildren().add(addBtn);
        }
        return box;
    }

    private HBox createSearchBar() {
        Label label = new Label("Search By Item Name: ");
        TextField search = new TextField();
        search.setPrefWidth(250);
        HBox box = new HBox(30);
        box.getChildren().add(label);
        box.getChildren().add(search);
        search.setOnKeyReleased(e -> searchItems(search.getText().toLowerCase()));
        return box;
    }

    private void searchItems(String searchString) {
        if (searchString.isEmpty()) {
            itemTable.setItems(itemData);
            refreshItems();
            return;
        }

        FilteredList<Item> filteredData = new FilteredList<>(itemData);

        filteredData.setPredicate(item -> {
            return item.getName().toLowerCase().contains(searchString);
        });

        itemTable.setItems(filteredData);
        refreshItems();

    }



    private void showAddItemDialog() {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Add Item");
        dialog.setHeaderText("Enter item details:");

        TextField nameField = new TextField();
        TextArea descField = new TextArea();

        VBox content = new VBox(10, new Label("Name:"), nameField, new Label("Description:"), descField);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Item item = controller.addItem(nameField.getText(), descField.getText());
                refreshItems();
                return item;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            itemData.setAll(controller.findAllItems());
            itemTable.getSelectionModel().select(item);
        });
    }

    private void showRepairItemDialog(Item item) {
        if (item == null) return;
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Repair Item");
        dialog.setHeaderText("Repair the condition of " + item.getName() + ": ");


        int condition = item.getCondition();
        Label label = new Label("Current condition for " + item.getName() + " is: " + condition);
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
                    boolean hitMax = controller.repairItem(item, Integer.parseInt(amount.getText()));
                    repairConfirmation(item, Integer.parseInt(amount.getText()), dialog, hitMax);
                    refreshItems();
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

    private void repairConfirmation(Item item, int amount, Dialog dialog, boolean hitMax) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Item Repaired");
        confirm.setHeaderText("Item has been repaired");
        String message = item.getName() + " was repaired by " + amount;
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

    private void showEditItemDialog(Item item) {
        if (item == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Edit item details:");

        TextField nameField = new TextField(item.getName());
        TextArea descField = new TextArea(item.getDescription());

        VBox content = new VBox(10, new Label("Name:"), nameField, new Label("Description:"), descField);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                controller.updateItem(item.getId(), nameField.getText(), descField.getText());
                refreshItems();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showViewItemDialog(Item item) {
        if (item == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Item Details");
        dialog.setHeaderText("Viewing: " + item.getName());

        String ownerName = item.getOwner() != null ? item.getOwner().getName() : "Unassigned";
        TextArea details = new TextArea(
                "ID: " + item.getId() + "\n" +
                        "Name: " + item.getName() + "\n" +
                        "Description: " + item.getDescription() + "\n" +
                        "Condition: " + item.getCondition() + "\n" +
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

    public void showHistoryItemDialog(Item item) {
        if (item == null) return;
        List<Transaction> copy = item.getHistory().reversed();
        ObservableList<Transaction> historyData = FXCollections.observableArrayList(copy);
        Dialog dialog = new Dialog();
        dialog.setTitle("Item History");
        dialog.setHeaderText("Assignment History of: " + item.getName());

        TableView<Transaction> table = new TableView();

        TableColumn<Transaction, String> timeStampCol = new TableColumn<>("Time Stamp");
        timeStampCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDateTimeString()));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getType()));

        TableColumn<Transaction, String> toOwnerCol = new TableColumn<>("To Owner");
        toOwnerCol.setCellValueFactory(cell ->  {
            Transaction t = cell.getValue();
            String ownerString;
            if (t.getToOwner() == null) {
                ownerString = "--";
            }
            else {
                ownerString = t.getToOwner().getName();
            }
            return new ReadOnlyStringWrapper(ownerString);
        });

        TableColumn<Transaction, String> fromOwnerCol = new TableColumn<>("From Owner");
        fromOwnerCol.setCellValueFactory(cell ->  {
            Transaction t = cell.getValue();
            String ownerString;
            if (t.getFromOwner() == null) {
                ownerString = "--";
            }
            else {
                ownerString = t.getFromOwner().getName();
            }
            return new ReadOnlyStringWrapper(ownerString);
        });

        table.getColumns().setAll(timeStampCol, typeCol, toOwnerCol, fromOwnerCol);
        table.setItems(historyData);
        table.setPrefHeight(300);
        table.setPrefWidth(600);

        HBox content = new HBox(table);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();

    }

    public void refreshItems() {
        itemData.setAll(controller.findAllItems());
    }
}
