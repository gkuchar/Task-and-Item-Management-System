package com.example.management.model;


import java.util.ArrayList;
import java.util.Objects;


public class Item {
    private int id;
    private String name;
    private String description;


    private Owner owner; // can be null
    private ArrayList<Transaction> history; // can be empty, but not null
    private int condition;

    public Item(String name, String description) {
        this.name = Objects.requireNonNullElse(name, "name must not be null");
        this.description = Objects.requireNonNullElse(description, "description must not be null");
        this.owner = null;
        this.history = new ArrayList<>();
        this.condition = 100;
    }

    public Item() {

    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Owner getOwner() { return owner; }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public void setHistory(ArrayList<Transaction> history) {
        this.history = history != null ? history : new ArrayList<>();
    }


    public void setId(int id) { this.id = id; }
    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "name must not be null");
    }
    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "description must not be null");
    }
    public void setOwner(Owner owner) { this.owner = owner; } // package-private to restrict access

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
