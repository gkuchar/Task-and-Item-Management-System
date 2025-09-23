package com.example.hogwarts.model;

import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Artifact {
    private int id;
    private String name;
    private String description;
    private Wizard owner; // can be null
    private ArrayList<Transaction> history; // can be empty, but not null

    public Artifact(String name, String description) {
        this.name = Objects.requireNonNullElse(name, "name must not be null");
        this.description = Objects.requireNonNullElse(description, "description must not be null");
        this.owner = null;
        this.history = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Wizard getOwner() { return owner; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "name must not be null");
    }
    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "description must not be null");
    }
    public void setOwner(Wizard owner) { this.owner = owner; } // package-private to restrict access

    public ArrayList<Transaction> getHistory() {
        return this.history;
    }


    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }

    public Transaction addTransaction(Transaction transaction) {
        this.history.add(transaction);
        return transaction;
    }

}
