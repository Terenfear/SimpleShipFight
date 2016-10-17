package com.justforf.terenfear.simpleshipfight;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Terenfear on 27.09.2016.
 */

public class FieldModel {
    private Tile[] tileMap[];
    private ArrayList<Ship> ships = new ArrayList<>();
    private int shipQuantity[] = {0, 0, 0, 0};
    private int dimension;

    public FieldModel(int tileSize, int dimension) {
        this.dimension = dimension;
        tileMap = new Tile[dimension][dimension];
        for (int rowId = 0; rowId < dimension; rowId++) {
            for (int colId = 0; colId < dimension; colId++) {
                getTileMap()[rowId][colId] = new Tile(colId, rowId, tileSize);
            }
        }
    }

    public void updateShipNumbers() {
        shipQuantity = new int[]{0, 0, 0, 0};
        for (Ship ship : ships) {
            if (ship.isAlive())
                shipQuantity[ship.getSize() - 1]++;
        }
    }

//    private boolean checkAllNeighbors(int rowId, int colId, @Nullable Ship yourShip) {
//        for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
//            for (int colOffset = -1; colOffset < 2; colOffset++) {
//                try {
//                    Tile neighbor = tileMap[rowId + rowOffset][colId + colOffset];
//                    Ship neighborShip = neighbor.getParentShip();
//                    if (neighborShip != null) {
//                        if ((rowOffset != 0 && colOffset != 0) || neighborShip != yourShip)
//                            return false;
//                    }
//                } catch (IndexOutOfBoundsException e) {
//                }
//            }
//        }
//        return true;
//    }

    public boolean generateRandomField() {
        Random random = new Random();
        float startTime = System.currentTimeMillis();
        for (int shipSize = 4; shipSize > 0; shipSize--) {
            for (int shipId = 0; shipId < 5 - shipSize; shipId++) {
                boolean isShipPlaced;
                do {
                    if (System.currentTimeMillis() - startTime > 1000) {  //abort long generation
                        clear();
                        return false;
                    }
                    ArrayList<Tile> parts = new ArrayList<>();
                    Ship genShip = new Ship(parts);
                    Tile part;
                    boolean isTileEmpty;
                    do {
                        int tileId = random.nextInt(dimension * dimension);
                        int rowId = tileId / dimension;
                        int colId = tileId % dimension;
                        part = tileMap[rowId][colId];
                        isTileEmpty = !part.isInShip() && part.isFarFromShips();
                    } while (!isTileEmpty);
                    genShip.addPart(part);
                    if (shipSize == 1) {
                        ships.add(genShip);
                        occupyTilesAround(part.getY(), part.getX());
                        break;
                    }
                    ArrayList<Tile> emptyNeighbors = new ArrayList<>();
                    for (int directionSummand = -1; directionSummand < 2; directionSummand += 2) {
                        if (directionSummand < 0) {
                            checkPossiblePlaces(part, shipSize, emptyNeighbors, directionSummand, Direction.HORIZONTAL);
                            if (emptyNeighbors.isEmpty()) {
                                checkPossiblePlaces(part, shipSize, emptyNeighbors, directionSummand, Direction.VERTICAL);
                                if (!emptyNeighbors.isEmpty()) break;
                            } else break;
                        } else {
                            checkPossiblePlaces(part, shipSize, emptyNeighbors, directionSummand, Direction.VERTICAL);
                            if (emptyNeighbors.isEmpty()) {
                                checkPossiblePlaces(part, shipSize, emptyNeighbors, directionSummand, Direction.HORIZONTAL);
                                if (!emptyNeighbors.isEmpty()) break;
                            } else break;
                        }

                    }
                    if (emptyNeighbors.isEmpty()) {
                        isShipPlaced = false;
                    } else {
                        occupyTilesAround(part.getY(), part.getX());
                        for (Tile neighborTile :
                                emptyNeighbors) {
                            genShip.addPart(neighborTile);
                            occupyTilesAround(neighborTile.getY(), neighborTile.getX());
                        }
                        genShip.sort();
                        ships.add(genShip);
                        isShipPlaced = true;
                    }
                } while (!isShipPlaced);
            }
        }
        return true;
    }

    private void occupyTilesAround(int rowId, int colId) {
        for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
            for (int colOffset = -1; colOffset < 2; colOffset++) {
                try {
                    tileMap[rowId + rowOffset][colId + colOffset].setFarFromShips(false);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
    }

    private void checkPossiblePlaces(Tile firstPart, int desiredSize, ArrayList<Tile> emptyNeighbors, int directionSummand, Direction direction) {
        Tile tile;
        switch (direction) {
            case VERTICAL:
                for (int rowOffset = directionSummand; Math.abs(rowOffset) < desiredSize; rowOffset += directionSummand) {
                    try {
                        tile = tileMap[firstPart.getY() + rowOffset][firstPart.getX()];
                        if (tile.isFarFromShips())
                            emptyNeighbors.add(tile);
                        else throw new Exception("Can't place whole ship");
                    } catch (Exception e) {
                        emptyNeighbors.clear();
                        break;
                    }

                }
                break;
            case HORIZONTAL:
                for (int colOffset = directionSummand; Math.abs(colOffset) < desiredSize; colOffset += directionSummand) {
                    try {
                        tile = tileMap[firstPart.getY()][firstPart.getX() + colOffset];
                        if (tile.isFarFromShips())
                            emptyNeighbors.add(tile);
                        else throw new Exception("Can't place whole ship");
                    } catch (Exception e) {
                        emptyNeighbors.clear();
                        break;
                    }

                }
                break;
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

    private enum Direction {VERTICAL, HORIZONTAL}

}
