package com.example.hogwarts.controller;

import com.example.hogwarts.data.DataStore;
import com.example.hogwarts.model.Artifact;
import com.example.hogwarts.model.Transaction;
import com.example.hogwarts.model.Wizard;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NoSuchElementException;

import static com.example.hogwarts.model.Transaction.Type.ASSIGN;
import static com.example.hogwarts.model.Transaction.Type.UNASSIGN;

public class ArtifactController {
    private final DataStore store = DataStore.getInstance();

    public Collection<Artifact> findAllArtifacts() {
        return this.store.findAllArtifacts();
    }

    public Artifact addArtifact(String name, String description) {
        Artifact artifact = new Artifact(name, description);
        return this.store.addArtifact(artifact);
    }

    public void updateArtifact(int id, String newName, String newDesc) {
        Artifact artifact = this.store.findArtifactById(id);
        if(artifact == null) {
            throw new NoSuchElementException("Artifact with ID " + id + " not found.");
        }
        artifact.setName(newName);
        artifact.setDescription(newDesc);
    }

    public boolean unassignArtifactFromWizard(Wizard wizard, Artifact artifact) {
        boolean success = store.unassignArtifactFromWizard(wizard.getId(), artifact.getId());
        if (success) {
            Transaction transaction = new Transaction("UNASSIGN", artifact, LocalDateTime.now(), null, wizard);
            artifact.addTransaction(transaction);
        }
        return success;
    }


    public void deleteArtifact(int id) {
        this.store.deleteArtifactById(id);
    }

    public boolean repairArtifact(Artifact artifact, int amount) {
        int total = artifact.getCondition() + amount;
        boolean hitMax = (total >= 100);
        String type = "REPAIR by " + amount;
        if (hitMax) {
            artifact.setCondition(100);
            type = type + " (hit max)";
        }
        else {
            artifact.setCondition(artifact.getCondition() + amount);
        }

        Transaction transaction = new Transaction(type, artifact, LocalDateTime.now(), null, null);
        artifact.addTransaction(transaction);

        return hitMax;

    }
}
