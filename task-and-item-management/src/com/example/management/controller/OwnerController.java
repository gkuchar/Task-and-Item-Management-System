package com.example.management.controller;

import com.example.management.data.DataStore;
import com.example.management.model.Item;
import com.example.management.model.Owner;
import com.example.management.model.Transaction;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;



public class OwnerController {
    private final DataStore store = DataStore.getInstance();

    public Collection<Owner> findAllOwners() {
        return this.store.findAllOwners();
    }

    public Owner addOwner(String name) {
        Owner owner = new Owner(name);
        return this.store.addOwner(owner);
    }

    public void updateOwner(int id, String newName) {
        Owner owner = this.store.findOwnersById(id);
        if(owner == null) {
            throw new IllegalArgumentException("Wizard with ID " + id + " not found.");
        }
        owner.setName(newName);
    }

    public void deleteOwner(int id) {
        this.store.deleteOwnerById(id);
    }

    public boolean assignItemToOwner(Owner owner, Item item) {
        boolean success = this.store.assignItemToOwner(owner.getId(), item.getId());
        if (success) {
            Transaction transaction = new Transaction("ASSIGN", item, LocalDateTime.now().withNano(0), owner, null);
            item.addTransaction(transaction);
        }
        return success;
    }



    public List<Item> getUnassignedItems() {
        return this.store.findAllItems().stream()
                .filter(a -> a.getOwner() == null)
                .collect(Collectors.toList());
    }
}
