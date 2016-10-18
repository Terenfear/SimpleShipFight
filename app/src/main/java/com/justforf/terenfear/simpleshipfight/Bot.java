package com.justforf.terenfear.simpleshipfight;

import android.support.annotation.Nullable;

import java.util.Random;

import static com.justforf.terenfear.simpleshipfight.FieldController.DIMENSION;

/**
 * Created by Terenfear on 18.10.2016.
 */

public class Bot {
    private final static int AUTO_AIM_TURN = 4;
    private FieldController enemyField;
    private int turnCounter = 0;

    public Bot(FieldController enemyField) {
        this.enemyField = enemyField;
    }

    public void makeShot(@Nullable Tile target) {
        Random random = new Random();
        boolean autoAimOn = (turnCounter == AUTO_AIM_TURN);
        if (target == null) {
            boolean isTileFresh;
            do {
                int tileId = random.nextInt(DIMENSION * DIMENSION);
                int rowId = tileId / DIMENSION;
                int colId = tileId % DIMENSION;
                target = enemyField.getTile(rowId, colId);
                isTileFresh = (autoAimOn ? !target.isShot() : !target.isShot() && target.isInShip());
            }while (!isTileFresh);
            turnCounter = (autoAimOn ? 0 : turnCounter + 1);
        }
        target.setShot(true);
//        tile.setShot(true);
//        if (tile.isInShip())
//            if (!tile.getParentShip().checkIntegrity()) {
//                for (Tile part : tile.getParentShip().getAllParts())
//                    shotTilesAround(part.getY(), part.getX());
//                drawman.drawInvisibleField();
//                updateShipQuantity();
//            }
//        drawman.drawTile(tile);
    }
}
