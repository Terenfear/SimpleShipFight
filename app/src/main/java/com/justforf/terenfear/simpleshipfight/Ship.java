package com.justforf.terenfear.simpleshipfight;

import java.util.ArrayList;

/**
 * Created by Terenfear on 11.09.2016.
 */
public class Ship {
    private ArrayList<Tile> parts;
    private boolean isAlive;

    public Ship(ArrayList<Tile> parts) {
        this.isAlive = true;
        this.parts = parts;
    }

    public ArrayList<Tile> getAllParts() {
        return parts;
    }

    public boolean addPart(Tile part) {
        if (parts.size() < 4) {
            parts.add(part);
            return true;
        }else return false;
    }

    public Tile getPart(int id) {
        return parts.get(id);
    }

    public Tile getFirstPart() {
        return parts.get(0);
    }

    public Tile getLastPart() {
        return parts.get(parts.size() - 1);
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getSize() {
        return parts.size();
    }
}
