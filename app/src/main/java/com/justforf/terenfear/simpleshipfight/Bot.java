package com.justforf.terenfear.simpleshipfight;

import android.os.Handler;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

import static com.justforf.terenfear.simpleshipfight.FieldController.DIMENSION;

/**
 * Created by Terenfear on 18.10.2016.
 */

public class Bot {
    private final int autoAimTurn;
    private FieldController enemyField;
    private TurnEndListener turnEndListener;
    private int turnCounter = 0;
    private ArrayList<Tile> burningTiles = new ArrayList<>();
    //private Tile lastHit = null;
    private Random random = new Random();

    public Bot(FieldController enemyField, TurnEndListener listener, int autoAimTurn) {
        this.enemyField = enemyField;
        this.turnEndListener = listener;
        this.autoAimTurn = autoAimTurn;
    }

    public void makeShot(@Nullable final Tile targetTile) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            private Tile target = targetTile;

            @Override
            public void run() {
                if (target == null) {
                    if (burningTiles.isEmpty()) {
                        target = getRandomTarget();
                    } else {
                        do {
                            Tile lastHit = burningTiles.get(burningTiles.size() - 1);
                            if (enemyField.areAllNearbyShot(lastHit))
                                burningTiles.remove(lastHit);
                            else target = getNearbyTarget(lastHit);
                        } while (!burningTiles.isEmpty() && target == null);
                        if (target == null)
                            target = getRandomTarget();
                    }
                }
                target.setShot(true);
                if (target.isInShip()) {
                    burningTiles.add(target);
                    Tile nextTarget;
                    if (!target.getParentShip().checkIntegrity()) {
                        for (Tile part : target.getParentShip().getAllParts()) {
                            enemyField.shotTilesAround(part.getY(), part.getX());
                            burningTiles.remove(part);
                        }
                        nextTarget = null;
                        enemyField.getDrawman().drawVisibleField();
                        enemyField.updateShipQuantity();
                        if (enemyField.isEndgame("You have lost, better luck next time.", "Ok"))
                            return;
                    } else {
                        nextTarget = getNearbyTarget(target);
                    }
                    makeShot(nextTarget);
                } else {
                    turnEndListener.turnEnded(LastPlayer.BOT);
                }
                enemyField.getDrawman().drawTile(target);
            }
        }, 1500);
    }

    private Tile getRandomTarget() {
        Tile target;
        boolean autoAimOn = (turnCounter == autoAimTurn);
        boolean canShootHere;
        do {
            int tileId = random.nextInt(DIMENSION * DIMENSION);
            int rowId = tileId / DIMENSION;
            int colId = tileId % DIMENSION;
            target = enemyField.getTile(rowId, colId);
            canShootHere = (autoAimOn ? !target.isShot() && target.isInShip() : !target.isShot());
        } while (!canShootHere);
        turnCounter = (autoAimOn ? 0 : turnCounter + 1);
        return target;
    }

    private Tile getNearbyTarget(Tile lastTarget) {
        Tile nextTarget = null;
        int lastX = lastTarget.getX();
        int lastY = lastTarget.getY();
        int offsetX = 1;
        int offsetY = 1;
        boolean foundNextTarget = false;
        do {
            int nextDirection = random.nextInt(4);
            switch (nextDirection) {
                case 0:
                    offsetX = 0;
                    offsetY = -1;
                    break;
                case 1:
                    offsetX = 1;
                    offsetY = 0;
                    break;
                case 2:
                    offsetX = 0;
                    offsetY = 1;
                    break;
                case 3:
                    offsetX = -1;
                    offsetY = 0;
                    break;
            }
            try {
                nextTarget = enemyField.getTile(lastY + offsetY, lastX + offsetX);
                foundNextTarget = !nextTarget.isShot();
            } catch (IndexOutOfBoundsException e) {
            }
        } while (!foundNextTarget);
        return nextTarget;
    }
}
