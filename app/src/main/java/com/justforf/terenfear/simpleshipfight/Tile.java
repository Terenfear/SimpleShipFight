package com.justforf.terenfear.simpleshipfight;

import android.graphics.RectF;

/**
 * Created by Terenfear on 08.09.2016.
 */
public class Tile extends RectF implements Comparable {
    public static final Creator<RectF> CREATOR = null;
    private int x;
    private int y;
    private Ship parentShip;
    private boolean isShot;
    private boolean isFarFromShips;

    public Tile(int x, int y, float side) {
        this.parentShip = null;
        this.isShot = false;
        this.isFarFromShips = true;
        this.x = x;
        this.y = y;
        left = x * side;
        right = left + side;
        top = y * side;
        bottom = top + side;
    }

    public boolean isFarFromShips() {
        return isFarFromShips;
    }

    public void setFarFromShips(boolean farFromShips) {
        isFarFromShips = farFromShips;
    }

    public boolean isInShip() {
        return parentShip != null;
    }

    public Ship getParentShip() {
        return parentShip;
    }

    public void setParentShip(Ship ship) {
        this.parentShip = ship;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isShot() {
        return isShot;
    }

    public void setShot(boolean shot) {
        isShot = shot;
    }

    @Override
    public int compareTo(Object otherTile) {
        int otherX = ((Tile) otherTile).getX();
        int otherY = ((Tile) otherTile).getY();
        if (this.x != otherX)
            return this.x - otherX;
        else
            return this.y - otherY;
    }
}
