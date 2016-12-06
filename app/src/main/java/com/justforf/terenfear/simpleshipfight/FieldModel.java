package com.justforf.terenfear.simpleshipfight;

import java.util.ArrayList;

/**
 * Created by Terenfear on 27.09.2016.
 */

public class FieldModel {
    private Tile[] tileMap[];
    private ArrayList<Ship> ships = new ArrayList<>();
    private int shipQuantity[] = {0, 0, 0, 0};

    public FieldModel(int tileSize, int dimension) {
        tileMap = new Tile[dimension][dimension];
        for (int rowId = 0; rowId < dimension; rowId++) {
            for (int colId = 0; colId < dimension; colId++) {
                getTileMap()[rowId][colId] = new Tile(colId, rowId, tileSize);
            }
        }
    }

    public void clear() {
        ships.clear();
        shipQuantity = new int[]{0, 0, 0, 0};
        for (Tile[] row : tileMap) {
            for (Tile tile : row) {
                tile.setParentShip(null);
                tile.setFarFromShips(true);
            }
        }
    }

    public void removeShip(Ship ship) {
        ships.remove(ship);
    }

    public void addShip(Ship ship) {
        ships.add(ship);
    }

    public void clearShips() {
        ships.clear();
    }

    public int[] getShipQuantity() {
        return shipQuantity;
    }

    public void setShipQuantity(int[] shipQuantity) {
        this.shipQuantity = shipQuantity;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }

    public void setShips(ArrayList<Ship> ships) {
        this.ships = ships;
    }

    public Tile[][] getTileMap() {
        return tileMap;
    }

    public void setTileMap(Tile[][] tileMap) {
        this.tileMap = tileMap;
    }

    public Tile getTile(int colId, int rowId){
        return tileMap[rowId][colId];
    }
}
