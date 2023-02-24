package com.ethan.twclaim.data;

public class Building {
    // Buildings are the way tribes own land. Just like with players and tribes, there will be a database of buildings
    // that is loaded every time the server starts. Building templates will be taken from configurable files like
    // Movecraft.

    String owner;
    String name;
    int claim_radius;
    // How do the building's claims compare to the other buildings around it?
    int priority;
    float cost;
    // Can the claims be built on by anybody?
    boolean open;

    public Building(String owner, String name, int claim_radius, int priority, float cost, boolean open) {
        this.owner = owner;
        this.name = name;
        this.claim_radius = claim_radius;
        this.priority = priority;
        this.cost = cost;
        this.open = open;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClaim_radius() {
        return claim_radius;
    }

    public void setClaim_radius(int claim_radius) {
        this.claim_radius = claim_radius;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
