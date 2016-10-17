package com.justforf.terenfear.simpleshipfight;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Terenfear on 11.09.2016.
 */
public class Ship {
    private ArrayList<Tile> parts;
    private boolean isAlive;

    public Ship(ArrayList<Tile> parts) {
        this.isAlive = true;
        this.parts = parts;
        for (Tile part : parts)
            part.setParentShip(this);
    }

    public ArrayList<Tile> getAllParts() {
        return parts;
    }

    public boolean addPart(Tile part) {
        if (parts.size() < 4) {
            parts.add(part);
            part.setParentShip(this);
            return true;
        } else return false;
    }

    public void removePart(Tile part) {
        parts.remove(part);
    }

    public void removePart(int partId) {
        parts.remove(partId);
    }

    public void removeAllParts() {
        for (Tile part : parts)
            part.setParentShip(null);
        parts.clear();
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

    public boolean checkIntegrity() {
        int numberOfDamaged = 0;
        for (Tile part : parts) {
            if (part.isShot())
                numberOfDamaged++;
        }
        if (numberOfDamaged == parts.size())
            isAlive = false;
        else isAlive = true;
        return isAlive;
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

    public void sort() {
        Collections.sort(parts);
    }
}
