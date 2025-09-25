package com.example.hogwarts.data;

import com.example.hogwarts.model.Artifact;
import com.example.hogwarts.model.Wizard;
import com.example.hogwarts.model.Role;
import com.example.hogwarts.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.max;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * TODO: Make this a thread-safe singleton
 * TODO: Use atomic integers for ID generation to avoid race conditions
 */
public class DataStore {
    private static DataStore instance; // Singleton instance

    private final List<User> users = new ArrayList<>();

    private Map<Integer, Wizard> wizards = new HashMap<>();
    private Map<Integer, Artifact> artifacts = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private int wizardIdCounter; // Wizard ID generator
    private int artifactIdCounter; // Artifact ID generator

    private User currentUser; // Currently authenticated user

    private final File wizardFile = new File("data/wizards.json");
    private final File artifactFile = new File("data/artifacts.json");



    private DataStore() {
        // Hardcoded users
        this.users.add(new User("admin", "123", Role.ADMIN));
        this.users.add(new User("user", "123", Role.USER));


    }

    public static DataStore getInstance() {

        if (instance == null) {
            instance = new DataStore();
            instance.loadDataOrSeed();
        }

        return instance;
    }

    private void loadDataOrSeed() {
        if (wizardFile.exists()) {
            try {
                wizards = mapper.readValue(wizardFile, new TypeReference<HashMap<Integer, Wizard>>() {});
                if (wizards.isEmpty()) {
                    wizardIdCounter = 1;
                }
                else {
                    wizardIdCounter = max(wizards.keySet()) + 1;
                }

            } catch (IOException e) {
                System.out.println("Error loading wizards.json");
                loadDefaultWizards();
            }
        }
        else {
            loadDefaultWizards();
        }

        if (artifactFile.exists()) {
            try {
                artifacts = mapper.readValue(artifactFile, new TypeReference<HashMap<Integer, Artifact>>() {});
                if (artifacts.isEmpty()) {
                    artifactIdCounter = 1;
                } else {
                    artifactIdCounter = max(artifacts.keySet()) + 1;
                }
            } catch (IOException e) {
                System.out.println("Error loading artifacts.json: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                loadDefaultArtifacts();
            }
        }
        else {
            loadDefaultArtifacts();
        }

        for (Wizard w : wizards.values()) {
            List<Artifact> fixed = new ArrayList<>();
            for (Artifact a : w.getArtifacts()) {
                Artifact canonical = artifacts.get(a.getId());
                if (canonical != null) {
                    canonical.setOwner(w);     // make sure owner points to w
                    fixed.add(canonical);      // replace with the canonical artifact instance
                }
            }
            w.setArtifacts(fixed);             // reset to canonical objects
        }


    }

    public void saveData() {
        try {
            File folder = new File("data");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(wizardFile, wizards);
            mapper.writerWithDefaultPrettyPrinter().writeValue(artifactFile, artifacts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDefaultArtifacts() {
        Artifact a1 = new Artifact("Invisibility Cloak", "A magical cloak that makes the wearer invisible.");
        Artifact a2 = new Artifact("Time-Turner", "A device used for time travel.");
        this.addArtifact(a1);
        this.addArtifact(a2);
    }

    private void loadDefaultWizards() {
        Wizard w1 = new Wizard("Harry Potter");
        Wizard w2 = new Wizard("Hermione Granger");
        this.addWizard(w1);
        this.addWizard(w2);
    }

    // User authentication
    public User authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    // Wizards
    public Wizard addWizard(Wizard wizard) {
        wizard.setId(wizardIdCounter++);
        this.wizards.put(wizard.getId(), wizard);
        return wizard;
    }

    public void deleteWizardById(int id) {
        Wizard wizard = this.wizards.remove(id);
        if (wizard != null) {
            wizard.removeAllArtifacts();
        }
    }

    public Collection<Wizard> findAllWizards() {
        return this.wizards.values();
    }

    public Wizard findWizardById(int id) {
        return this.wizards.get(id);
    }

    // Artifacts
    public Artifact addArtifact(Artifact artifact) {
        artifact.setId(artifactIdCounter++);
        this.artifacts.put(artifact.getId(), artifact);
        return artifact;
    }

    public void deleteArtifactById(int id) {
        Artifact artifact = this.artifacts.remove(id);
        if (artifact != null && artifact.getOwner() != null) {
            artifact.getOwner().removeArtifact(artifact);
        }
    }

    public Collection<Artifact> findAllArtifacts() {
        return this.artifacts.values();
    }

    public Artifact findArtifactById(int id) {
        return this.artifacts.get(id);
    }

    public boolean assignArtifactToWizard(int wizardId, int artifactId) {
        Artifact artifact = this.artifacts.get(artifactId);
        Wizard wizard = this.wizards.get(wizardId);
        if (artifact == null || wizard == null) return false;

        if (artifact.getCondition() < 10) {
            return false;
        }

        wizard.addArtifact(artifact);
        artifact.setCondition(artifact.getCondition() - 5);
        return true;
    }

    public boolean unassignArtifactFromWizard(int wizardId, int artifactId) {
        Artifact artifact = this.artifacts.get(artifactId);
        Wizard wizard = this.wizards.get(wizardId);
        if (artifact == null || wizard == null) return false;

        wizard.removeArtifact(artifact);
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }



}
