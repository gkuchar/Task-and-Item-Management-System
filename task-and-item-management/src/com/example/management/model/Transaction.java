package com.example.management.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    private String type;

    @JsonIgnore
    private Owner fromOwner;

    @JsonIgnore
    private Owner toOwner;

    @JsonIgnore
    private Item item;
    private LocalDateTime timeStamp;

    public Transaction(String type, Item item, LocalDateTime timeStamp, Owner toOwner, Owner fromOwner) {
        this.item = item;
        this.type = type;
        this.timeStamp = timeStamp;
        this.toOwner = toOwner;
        this.fromOwner = fromOwner;
    }

    public Transaction() {

    }

    @JsonIgnore
    public String getDateTimeString() {
        ZoneId zoneId = ZoneId.of("America/Chicago");
        ZonedDateTime zonedDateTime = this.timeStamp.atZone(zoneId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss zzz");
        return zonedDateTime.format(formatter);

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Owner getFromOwner() {
        return fromOwner;
    }

    public void setFromOwner(Owner fromOwner) {
        this.fromOwner = fromOwner;
    }

    public Owner getToOwner() {
        return toOwner;
    }

    public void setToOwner(Owner toOwner) {
        this.toOwner = toOwner;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }


}
