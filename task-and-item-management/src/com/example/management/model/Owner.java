package com.example.management.model;
import com.fasterxml.jackson.annotation.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Owner {
    private int id;
    private String name;

    @JsonProperty("items")
    private final List<Item> items = new ArrayList<>();

    public Owner(String name) {
        this.name = Objects.requireNonNull(name, "name"); // name must not be null
    }

    public Owner() {

    }

    @JsonProperty("items")
    public void setItems(List<Item> items) {
        this.items.clear();
        if (items != null) {
            for (Item a : items) {
                addItem(a); // ensures back-references are in sync
            }
        }
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @JsonProperty("items")
    public List<Item> getItems() {
        return Collections.unmodifiableList(items); // good defensive programming: prevent external modification
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = Objects.requireNonNull(name, "name"); }

    public void addItem(Item item) {
        Objects.requireNonNull(item, "items"); // item must not be null

        if (this.items.contains(item)) return; // already in the collection

        Owner currentOwner = item.getOwner();

        if (currentOwner != null) {
            currentOwner.removeItem(item); // detach from previous owner
        }

        // now attach to this owner
        items.add(item);
        item.setOwner(this); // keep back-reference in sync
    }

    public boolean removeItem(Item item) {
        boolean removed = items.remove(item);
        if (removed) {
            item.setOwner(null);
        }
        return removed;
    }

    public boolean removeAllItems() {
        if (items.isEmpty()) return false;

        for (Item a : items) {
            a.setOwner(null);   // package-private
        }
        items.clear();
        return true;
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }
}
