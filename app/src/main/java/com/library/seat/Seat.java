package com.library.seat;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Seat implements Serializable {
    private UUID uuid;
    private String id;
    private String name;
    private int min;
    private String session;

    public Seat() {
    }

    public Seat(String id, String name, int min, String session) {
        this.uuid = UUID.randomUUID();
        this.id = id;
        this.name = name;
        this.min = min;
        this.session = session;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
