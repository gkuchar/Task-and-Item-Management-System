package com.example.hogwarts.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    public enum Type  { ASSIGN, UNASSIGN }
    private Type type;
    private Wizard fromWizard;
    private Wizard toWizard;
    private Artifact artifact;
    private LocalDateTime timeStamp;

    public Transaction(Type type, Artifact artifact, LocalDateTime timeStamp, Wizard toWizard, Wizard fromWizard) {
        this.artifact = artifact;
        this.type = type;
        this.timeStamp = timeStamp;
        this.toWizard = toWizard;
        this.fromWizard = fromWizard;
    }

    public String getDateTimeString() {
        ZoneId zoneId = ZoneId.of("America/Chicago");
        ZonedDateTime zonedDateTime = this.timeStamp.atZone(zoneId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss zzz");
        return zonedDateTime.format(formatter);

    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Wizard getFromWizard() {
        return fromWizard;
    }

    public void setFromWizard(Wizard fromWizard) {
        this.fromWizard = fromWizard;
    }

    public Wizard getToWizard() {
        return toWizard;
    }

    public void setToWizard(Wizard toWizard) {
        this.toWizard = toWizard;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }


}
