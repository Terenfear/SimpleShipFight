package com.justforf.terenfear.simpleshipfight;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.justforf.terenfear.simpleshipfight.FieldView.DIMENSION;

/**
 * Created by Terenfear on 27.09.2016.
 */

public class FieldController {
    private FieldModel fieldModel;
    private FieldView fieldView;
    private GestureDetector gestureDetector;
    private Context parentContext;
    private Drawman drawman;
    private FieldGenerator fieldGenerator;
    private TurnEndListener turnEndListener;
    private int xOffset = 0;
    private int tileSize = 0;

    public FieldController(Context context, FieldView field) {
        this.fieldView = field;
        parentContext = context;
        drawman = new Drawman();
        fieldGenerator = new FieldGenerator();
        final ImageView fieldImageView = fieldView.getFieldImageView();
        fieldImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                fieldImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int fieldHeight = fieldImageView.getHeight();
                int fieldWidth = fieldImageView.getWidth();
                xOffset = (fieldWidth - fieldHeight) / 2;
                tileSize = fieldHeight / DIMENSION;
                fieldModel = new FieldModel(tileSize, DIMENSION);
                Bitmap bitmap = Bitmap.createBitmap(fieldHeight, fieldHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                fieldImageView.setImageBitmap(bitmap);
                fieldView.setCanvas(canvas);
                drawman.drawVisibleField(fieldImageView, canvas, fieldModel.getTileMap());
            }
        });
        gestureDetector = new GestureDetector(parentContext, new StationingGestureListener());
        fieldImageView.setOnTouchListener(new FieldOnTouchListener());
    }

    public void setTurnEndListener(TurnEndListener listener) {
        turnEndListener = listener;
    }

    public void removeListeners() {
        fieldView.getFieldImageView().setOnTouchListener(null);
    }

    public void enableShotListeners() {
        fieldView.getFieldImageView().setOnTouchListener(new FieldOnTouchListener());
        gestureDetector = new GestureDetector(parentContext, new ShotGestureListener());
    }

    public boolean[] checkShipQuantityStatus() {
        int[] shipQuantity = {0, 0, 0, 0};
        for (Ship ship : fieldModel.getShips()) {
            if (ship.isAlive())
                shipQuantity[ship.getSize() - 1]++;
        }
        fieldModel.setShipQuantity(shipQuantity);
        boolean[] enoughShips = {true, true, true, true};
        for (int shipType = 0; shipType < 4; shipType++) {
            int currentNumber = shipQuantity[shipType];
            int desiredNumber = 4 - shipType;
            if (currentNumber != desiredNumber)
                enoughShips[shipType] = false;
        }
        return enoughShips;
    }

    public void updateShipQuantity() {
        boolean[] enoughShips = checkShipQuantityStatus();
        int[] shipQuantity = fieldModel.getShipQuantity();
        List<TextView> quantityTextViews = fieldView.getQuantityTextViews();
        for (int shipType = 0; shipType < 4; shipType++) {
            quantityTextViews.get(shipType).setText("x " + shipQuantity[shipType]);
            if (enoughShips[shipType])
                quantityTextViews.get(shipType).setTextColor(Color.GREEN);
            else
                quantityTextViews.get(shipType).setTextColor(Color.RED);

        }
    }

    public Tile getTile(int colId, int rowId) throws IndexOutOfBoundsException {
        return fieldModel.getTile(colId, rowId);
    }

    public void clearField() {
        fieldModel.clear();
        drawman.drawVisibleField(fieldView.getFieldImageView(), fieldView.getCanvas(), fieldModel.getTileMap());
        updateShipQuantity();
    }

    public void generateField(boolean isVisible) {
        boolean isGenerated;
        do {
            isGenerated = fieldGenerator.generateRandomField();
        } while (!isGenerated);
        if (isVisible)
            drawman.drawVisibleField(fieldView.getFieldImageView(), fieldView.getCanvas(), fieldModel.getTileMap());
        else
            drawman.drawInvisibleField(fieldView.getFieldImageView(), fieldView.getCanvas(), fieldModel.getTileMap());
    }

    public void shotTilesAround(int rowId, int colId) {
        for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
            for (int colOffset = -1; colOffset < 2; colOffset++) {
                try {
                    fieldModel.getTileMap()[rowId + rowOffset][colId + colOffset].setShot(true);
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
    }

    public boolean areAllNearbyShot(Tile tile) {
        int tileX = tile.getX();
        int tileY = tile.getY();
        try {
            if (!getTile(tileY - 1, tileX).isShot()) return false;
        } catch (IndexOutOfBoundsException ignored) {
        }
        try {
            if (!getTile(tileY, tileX + 1).isShot()) return false;
        } catch (IndexOutOfBoundsException ignored) {
        }
        try {
            if (!getTile(tileY + 1, tileX).isShot()) return false;
        } catch (IndexOutOfBoundsException ignored) {
        }
        try {
            if (!getTile(tileY, tileX - 1).isShot()) return false;
        } catch (IndexOutOfBoundsException ignored) {
        }
        return true;
    }

    public boolean isEndgame(String message, String buttonText) {
        boolean isEndgame = true;
        for (int shipSize = 0; shipSize < 4; shipSize++) {
            if (fieldModel.getShipQuantity()[shipSize] > 0) {
                isEndgame = false;
                break;
            }
        }
        if (isEndgame) {
            fieldView.getFieldImageView().setOnTouchListener(null);
            drawman.drawVisibleField(fieldView.getFieldImageView(), fieldView.getCanvas(), fieldModel.getTileMap());
            AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
            builder.setMessage(message).setPositiveButton(buttonText, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return isEndgame;
    }

    public void setVisibility(int visibility) {
        fieldView.setVisibility(visibility);
    }

    public void drawVisibleField() {
        drawman.drawVisibleField(fieldView.getFieldImageView(), fieldView.getCanvas(), fieldModel.getTileMap());
    }

    public void drawInvisibleField() {
        drawman.drawInvisibleField(fieldView.getFieldImageView(), fieldView.getCanvas(), fieldModel.getTileMap());
    }

    public void drawTile(Tile tile) {
        drawman.drawTile(fieldView.getFieldImageView(), fieldView.getCanvas(), tile);
    }

    private boolean placePart(int x, int y) {
        Tile tile = fieldModel.getTile(x, y);
        if (!tile.isInShip()) {
            //checking selected tile
            ArrayList<Tile> neighborTiles = new ArrayList<>();
            for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
                for (int colOffset = -1; colOffset < 2; colOffset++) {
                    try {
                        Tile neighbor = fieldModel.getTile(x + colOffset, y + rowOffset);
                        if (neighbor.isInShip()) {
                            //checking diagonals
                            if (rowOffset != 0 && colOffset != 0) {
                                Toast.makeText(parentContext, "You can't place ships diagonally", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            //not diagonal neighbors will be analyzed
                            neighborTiles.add(neighbor);
                        }
                    } catch (IndexOutOfBoundsException ignored) {
                    }

                }
            }
            switch (neighborTiles.size()) {
                //can we unite neighbors?
                case 2:
                    Ship firstNeighbor = neighborTiles.get(0).getParentShip();
                    Ship secondNeighbor = neighborTiles.get(1).getParentShip();
                    //can we have ship with such size?
                    if (firstNeighbor.getSize() + secondNeighbor.getSize() < 4) {
                        firstNeighbor.addPart(tile);
                        for (Tile part : secondNeighbor.getAllParts())
                            firstNeighbor.addPart(part);
                        fieldModel.removeShip(secondNeighbor);
                        firstNeighbor.sort();
                    } else {
                        Toast.makeText(parentContext, "Resulting ship is too big", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    break;
                case 1:
                    Ship neighborShip = neighborTiles.get(0).getParentShip();
                    if (!neighborShip.addPart(tile)) {
                        Toast.makeText(parentContext, "Resulting ship is too big", Toast.LENGTH_SHORT).show();
                        return true;
                    } else neighborShip.sort();
                    break;
                case 0:
                    ArrayList parts = new ArrayList<Tile>();
                    parts.add(tile);
                    Ship ship = new Ship(parts);
                    tile.setParentShip(ship);
                    ship.sort();
                    fieldModel.addShip(ship);
                    break;
                default:
                    Log.i("long tap switch", "unidentified error");
                    return true;
            }
            updateShipQuantity();
            drawman.drawTile(fieldView.getFieldImageView(), fieldView.getCanvas(), tile);
        }
        return true;
    }

    private void removePart(int x, int y) {
        Tile tile = fieldModel.getTile(x, y);
        if (tile.isInShip()){
            Ship ship = tile.getParentShip();
            int partId = ship.getAllParts().indexOf(tile);
            ship.removePart(tile);
            tile.setParentShip(null);
            if (ship.getSize() == 0) {
                fieldModel.removeShip(ship);
            } else {
                if (partId != 0 && partId != ship.getSize()) {
                    ArrayList<Tile> secondParts = new ArrayList<>();
                    while (partId < ship.getSize()) {
                        secondParts.add(ship.getPart(partId));
                        ship.removePart(partId);
                    }
                    Ship secondShip = new Ship(secondParts);
                    fieldModel.addShip(secondShip);
                }
            }
            updateShipQuantity();
            drawman.drawTile(fieldView.getFieldImageView(), fieldView.getCanvas(), tile);
        }
    }

    private boolean shotTile(int x, int y){
        Tile tile = fieldModel.getTile(x, y);
        if (tile.isShot()) {
            Toast.makeText(parentContext, "This tile is already have been shot", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            tile.setShot(true);
            if (tile.isInShip()) {
                if (!tile.getParentShip().checkIntegrity()) {
                    for (Tile part : tile.getParentShip().getAllParts())
                        shotTilesAround(part.getY(), part.getX());
                    drawman.drawInvisibleField(fieldView.getFieldImageView(), fieldView.getCanvas(), fieldModel.getTileMap());
                    updateShipQuantity();
                    if (isEndgame("You have won, congratulations!", "Great!"))
                        return true;
                }
            } else
                turnEndListener.turnEnded(LastPlayer.HUMAN);
            drawman.drawTile(fieldView.getFieldImageView(), fieldView.getCanvas(), tile);
            return true;
        }
    }

    private final class FieldGenerator {
        private final int VERTICAL = 0;
        private final int HORIZONTAL = 1;

        private boolean generateRandomField() {
            Random random = new Random();
            float startTime = System.currentTimeMillis();
            for (int shipSize = 4; shipSize > 0; shipSize--) {
                for (int shipId = 0; shipId < 5 - shipSize; shipId++) {
                    boolean isShipPlaced;
                    do {
                        if (System.currentTimeMillis() - startTime > 1000) {  //abort long generation
                            fieldModel.clear();
                            return false;
                        }
                        ArrayList<Tile> parts = new ArrayList<>();
                        Ship genShip = new Ship(parts);
                        Tile part;
                        boolean isTileEmpty;
                        do {
                            int tileId = random.nextInt(DIMENSION * DIMENSION);
                            int rowId = tileId / DIMENSION;
                            int colId = tileId % DIMENSION;
                            part = fieldModel.getTileMap()[rowId][colId];
                            isTileEmpty = !part.isInShip() && part.isFarFromShips();
                        } while (!isTileEmpty);
                        genShip.addPart(part);
                        if (shipSize == 1) {
                            fieldModel.addShip(genShip);
                            occupyTilesAround(part.getY(), part.getX());
                            break;
                        }
                        ArrayList<Tile> emptyNeighbors = new ArrayList<>();
                        for (int directionSummand = -1; directionSummand < 2; directionSummand += 2) {
                            if (directionSummand < 0) {
                                accommodateShip(part, shipSize, emptyNeighbors, directionSummand, HORIZONTAL);
                                if (emptyNeighbors.isEmpty()) {
                                    accommodateShip(part, shipSize, emptyNeighbors, directionSummand, VERTICAL);
                                    if (!emptyNeighbors.isEmpty()) break;
                                } else break;
                            } else {
                                accommodateShip(part, shipSize, emptyNeighbors, directionSummand, VERTICAL);
                                if (emptyNeighbors.isEmpty()) {
                                    accommodateShip(part, shipSize, emptyNeighbors, directionSummand, HORIZONTAL);
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
                            fieldModel.addShip(genShip);
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
                        fieldModel.getTileMap()[rowId + rowOffset][colId + colOffset].setFarFromShips(false);
                    } catch (IndexOutOfBoundsException ignored) {
                    }
                }
            }
        }

        private void accommodateShip(Tile firstPart, int desiredSize, ArrayList<Tile> emptyNeighbors, int directionSummand, final int direction) {
            Tile tile;
            switch (direction) {
                case VERTICAL:
                    for (int rowOffset = directionSummand; Math.abs(rowOffset) < desiredSize; rowOffset += directionSummand) {
                        try {
                            tile = fieldModel.getTileMap()[firstPart.getY() + rowOffset][firstPart.getX()];
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
                            tile = fieldModel.getTileMap()[firstPart.getY()][firstPart.getX() + colOffset];
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
    }

    private class FieldOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gestureDetector.onTouchEvent(motionEvent);
            return true;
        }
    }

    private class ShotGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            int x = (int) ((e.getX() - xOffset) / tileSize);
            int y = (int) (e.getY() / tileSize);
            return shotTile(x, y);
        }
    }

    private class StationingGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("onTouch", "long tap");
            int x = (int) ((e.getX() - xOffset) / tileSize);
            int y = (int) (e.getY() / tileSize);
            removePart(x, y);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("onTouch", "single tap");
            int x = (int) ((e.getX() - xOffset) / tileSize);
            int y = (int) (e.getY() / tileSize);
            return placePart(x, y);
        }
    }

}
