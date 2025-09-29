package com.example.management.controller;

import com.example.management.data.DataStore;
import com.example.management.model.Item;
import com.example.management.model.Owner;
import com.example.management.model.Transaction;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NoSuchElementException;



public class ItemController {
    private final DataStore store = DataStore.getInstance();

    public Collection<Item> findAllItems() {
        return this.store.findAllItems();
    }

    public Item addItem(String name, String description) {
        Item item = new Item(name, description);
        return this.store.addItem(item);
    }

    public void updateItem(int id, String newName, String newDesc) {
        Item item = this.store.findItemById(id);
        if(item == null) {
            throw new NoSuchElementException("Artifact with ID " + id + " not found.");
        }
        item.setName(newName);
        item.setDescription(newDesc);
    }

    public boolean unassignItemFromOwner(Owner owner, Item item) {
        boolean success = store.unassignItemFromOwner(owner.getId(), item.getId());
        if (success) {
            Transaction transaction = new Transaction("UNASSIGN", item, LocalDateTime.now().withNano(0), null, owner);
            item.addTransaction(transaction);
        }
        return success;
    }


    public void deleteItem(int id) {
        this.store.deleteItemById(id);
    }

    public boolean repairItem(Item item, int amount) {
        int total = item.getCondition() + amount;
        boolean hitMax = (total >= 100);
        String type = "REPAIR by " + amount;
        if (hitMax) {
            item.setCondition(100);
            type = type + " (hit max)";
        }
        else {
            item.setCondition(item.getCondition() + amount);
        }

        Transaction transaction = new Transaction(type, item, LocalDateTime.now().withNano(0), null, null);
        item.addTransaction(transaction);

        return hitMax;

    }
}
