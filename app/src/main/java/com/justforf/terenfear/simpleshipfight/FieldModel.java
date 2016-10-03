package com.justforf.terenfear.simpleshipfight;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Terenfear on 27.09.2016.
 */

public class FieldModel {
    private Tile[] tileMap[] = new Tile[10][10];
    private ArrayList<Ship> ships = new ArrayList<>();
    private int shipQuantity[] = {0, 0, 0, 0};

    public FieldModel(int tileSize) {
        for (int rowId = 0; rowId < 10; rowId++) {
            for (int colId = 0; colId < 10; colId++) {
                getTileMap()[rowId][colId] = new Tile(colId * tileSize, rowId * tileSize, tileSize);
            }
        }
    }

    public void updateShipNumbers() {
        shipQuantity[0] = shipQuantity[1] = shipQuantity[2] = shipQuantity[3] = 0;
        for (Ship ship : ships) {
            switch (ship.getSize()) {
                case 1:
                    shipQuantity[0]++;
                    break;
                case 2:
                    shipQuantity[1]++;
                    break;
                case 3:
                    shipQuantity[2]++;
                    break;
                case 4:
                    shipQuantity[3]++;
                    break;
            }
        }
    }

    public void generateRandomField() {
        Random random = new Random();
        for (int partId = 0; partId < 20; partId++) {
            Tile tile;
            boolean diagonalFree;
            do {
                diagonalFree = true;
                int tileId = random.nextInt(100);
                int rowId = tileId / 10;
                int colId = tileId % 10;
                tile = tileMap[rowId][colId];
                if (tile.isInShip())
                    continue;
                for (int rowOffset = -1; rowOffset < 2; rowOffset += 2) {
                    for (int colOffset = -1; colOffset < 2; colOffset += 2) {
                        try {
                            Tile neighbor = tileMap[rowId + rowOffset][colId + colOffset];
                            if (neighbor.isInShip()) {
                                diagonalFree = false;
                                break;
                            }
                        } catch (IndexOutOfBoundsException exception) {
                        }

                    }
                }
            } while (!diagonalFree);
            ArrayList parts = new ArrayList<Tile>();
            parts.add(tile);
            Ship ship = new Ship(parts);
            ships.add(ship);
            tile.setParentShip(ship);
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

}
