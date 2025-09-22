package com.example.hogwarts.controller;

import com.example.hogwarts.data.DataStore;
import com.example.hogwarts.model.Artifact;
import com.example.hogwarts.model.Wizard;

import java.util.Collection;
import java.util.NoSuchElementException;

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
        return store.unassignArtifactFromWizard(wizard.getId(), artifact.getId());
    }


    public void deleteArtifact(int id) {
        this.store.deleteArtifactById(id);
    }
}
