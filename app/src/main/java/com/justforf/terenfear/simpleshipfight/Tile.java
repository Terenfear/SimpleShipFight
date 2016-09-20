package com.justforf.terenfear.simpleshipfight;

import android.graphics.RectF;

/**
 * Created by Terenfear on 08.09.2016.
 */
public class Tile extends RectF implements Comparable {
    public static final Creator<RectF> CREATOR = null;
    private Ship parentShip;
    private boolean isShot;

    public Tile(float x, float y, float side) {
        this.parentShip = null;
        this.isShot = false;
        left = x;
        right = x + side;
        top = y;
        bottom = y + side;
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

    public boolean isShot() {
        return isShot;
    }

    public void setShot(boolean shot) {
        isShot = shot;
    }

    @Override
    public int compareTo(Object otherTile) {
        int otherTop = (int) ((Tile) otherTile).top;
        int otherLeft = (int) ((Tile) otherTile).left;
        if ((int)this.top != otherTop)
            return (int)this.top - otherTop;
        else
            return (int)this.left - otherLeft;
    }
}
