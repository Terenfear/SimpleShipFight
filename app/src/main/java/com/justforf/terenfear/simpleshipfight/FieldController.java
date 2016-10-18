package com.justforf.terenfear.simpleshipfight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Terenfear on 27.09.2016.
 */

public class FieldController extends android.support.constraint.ConstraintLayout {
    private static final int DIMENSION = 10;
    private int xOffset = 0;
    private int tileSize;
    private FieldModel fieldModel;
    private Canvas canvas;
    private ImageView fieldImageView;
    private TextView tvQuantityOf1;
    private TextView tvQuantityOf2;
    private TextView tvQuantityOf3;
    private TextView tvQuantityOf4;
    private ArrayList<TextView> quantityTextViews;
    private GestureDetector gestureDetector;
    private Context parentContext;
    private Drawman drawman;

    public FieldController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FieldController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public FieldController(Context context) {
        super(context);
        initView(context);
    }

    public FieldModel getFieldModel() {
        return fieldModel;
    }

    private void initView(Context context) {
        parentContext = context;
        drawman = new Drawman();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.field_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvQuantityOf1 = (TextView) this.findViewById(R.id.tvShip1);
        tvQuantityOf2 = (TextView) this.findViewById(R.id.tvShip2);
        tvQuantityOf3 = (TextView) this.findViewById(R.id.tvShip3);
        tvQuantityOf4 = (TextView) this.findViewById(R.id.tvShip4);
        quantityTextViews = new ArrayList<>();
        quantityTextViews.add(tvQuantityOf1);
        quantityTextViews.add(tvQuantityOf2);
        quantityTextViews.add(tvQuantityOf3);
        quantityTextViews.add(tvQuantityOf4);
        fieldImageView = (ImageView) this.findViewById(R.id.ivField);
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
                fieldImageView.setImageBitmap(bitmap);
                canvas = new Canvas(bitmap);
                drawman.drawVisibleField();
                gestureDetector = new GestureDetector(parentContext, new FieldController.PrepGestureListener());
                fieldImageView.setOnTouchListener(new gridOnTouchListener());
            }
        });

    }

    public void removeListeners() {
        fieldImageView.setOnTouchListener(null);
    }

    public void enableFightListeners() {
//        fieldImageView.setOnTouchListener(null);
        gestureDetector = new GestureDetector(parentContext, new FieldController.FightGestureListener());
//        fieldImageView.setOnTouchListener(new gridOnTouchListener());
    }

    public void updateShipQuantity() {
        int[] shipQuantity = {0, 0, 0, 0};
        for (Ship ship : fieldModel.getShips()) {
            if (ship.isAlive())
                shipQuantity[ship.getSize() - 1]++;
        }
        fieldModel.setShipQuantity(shipQuantity);
        tvQuantityOf1.setText("x " + shipQuantity[0]);
        tvQuantityOf2.setText("x " + shipQuantity[1]);
        tvQuantityOf3.setText("x " + shipQuantity[2]);
        tvQuantityOf4.setText("x " + shipQuantity[3]);
        for (int shipType = 0; shipType < 4; shipType++) {
            int totalQuantity = shipQuantity[shipType];
            int desiredQuantity = 4 - shipType;
            TextView correspondingTV = quantityTextViews.get(shipType);
            if (totalQuantity > desiredQuantity)
                correspondingTV.setTextColor(Color.RED);
            else {
                if (totalQuantity < desiredQuantity)
                    correspondingTV.setTextColor(ContextCompat.getColor(parentContext, R.color.colorLightOrange));
                else correspondingTV.setTextColor(Color.GREEN);
            }

        }
    }

    public void clearField() {
        fieldModel.clear();
        drawman.drawVisibleField();
        updateShipQuantity();
    }

    public void generateEnemyField() {
        boolean isGenerated;
        do {
            isGenerated = generateRandomField();
        } while (!isGenerated);
        drawman.drawInvisibleField();
    }

    public boolean generateRandomField() {
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

    private enum Direction {VERTICAL, HORIZONTAL}

    private final class Drawman {
        private Paint clearing;
        private Paint emptyPaint;
        private Paint shipPaint;
        private Paint shotPaint;
        private Paint destroyedPaint;

        private Drawman() {
            clearing = new Paint(Paint.ANTI_ALIAS_FLAG);
            clearing.setColor(Color.WHITE);
            emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            emptyPaint.setStyle(Paint.Style.STROKE);
            emptyPaint.setColor(Color.BLACK);
            shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shipPaint.setColor(Color.BLUE);
            shotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shotPaint.setColor(Color.LTGRAY);
            destroyedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            destroyedPaint.setColor(Color.RED);
        }

        public void drawVisibleField() {
            canvas.drawPaint(clearing);
            for (Tile[] row : fieldModel.getTileMap())
                for (Tile tile : row) {
                    if (tile.isInShip())
                        if (tile.isShot())
                            canvas.drawRect(tile, destroyedPaint);
                        else canvas.drawRect(tile, shipPaint);
                    else if (tile.isShot())
                        canvas.drawRect(tile, shotPaint);
                    canvas.drawRect(tile, emptyPaint);
                }
            fieldImageView.invalidate();
        }

        public void drawInvisibleField() {
            canvas.drawPaint(clearing);
            for (Tile[] row : fieldModel.getTileMap())
                for (Tile tile : row) {
                    if (tile.isShot()) {
                        if (tile.isInShip())
                            canvas.drawRect(tile, destroyedPaint);
                        else canvas.drawRect(tile, shotPaint);
                    }
                    canvas.drawRect(tile, emptyPaint);
                }
            fieldImageView.invalidate();
        }

        public void drawTile(Tile tile) {
            if (tile.isInShip())
                if (tile.isShot())
                    canvas.drawRect(tile, destroyedPaint);
                else canvas.drawRect(tile, shipPaint);
            else if (tile.isShot())
                canvas.drawRect(tile, shotPaint);
            canvas.drawRect(tile, emptyPaint);
            fieldImageView.invalidate((int) tile.left, (int) tile.top, (int) tile.right, (int) tile.bottom);
        }
    }


    private class gridOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gestureDetector.onTouchEvent(motionEvent);
            return true;
        }
    }

    private class FightGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            int x = (int) ((e.getX() - xOffset) / tileSize);
            int y = (int) (e.getY() / tileSize);
            for (int rowId = 0; rowId < DIMENSION; rowId++) {
                for (int colId = 0; colId < DIMENSION; colId++) {
                    Tile tile = fieldModel.getTileMap()[rowId][colId];
                    if (x == tile.getX() && y == tile.getY()) {
                        if (tile.isShot()) {
                            Toast.makeText(parentContext, " You've already shot there", Toast.LENGTH_SHORT).show();
                            return false;
                        } else {
                            tile.setShot(true);
                            if (tile.isInShip())
                                if (!tile.getParentShip().checkIntegrity()) {
                                    for (Tile part : tile.getParentShip().getAllParts())
                                        shotTilesAround(part.getY(), part.getX());
                                    drawman.drawInvisibleField();
                                    updateShipQuantity();
                                }
                            drawman.drawTile(tile);
                            Toast.makeText(parentContext, "SHOTS FIRED", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                }
            }
            return true;
        }

        private void shotTilesAround(int rowId, int colId) {
            for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
                for (int colOffset = -1; colOffset < 2; colOffset++) {
                    try {
                        fieldModel.getTileMap()[rowId + rowOffset][colId + colOffset].setShot(true);
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
            }
        }
    }

    private class PrepGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("onTouch", "long tap");
            int x = (int) ((e.getX() - xOffset) / tileSize);
            int y = (int) (e.getY() / tileSize);
            for (Ship ship : fieldModel.getShips()) {
                for (Tile part : ship.getAllParts()) {
                    if (x == part.getX() && y == part.getY()) {
                        int partId = ship.getAllParts().indexOf(part);
                        ship.removePart(part);
                        part.setParentShip(null);
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
                        drawman.drawTile(part);
                        return;
                    }
                }

            }
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
            for (int rowId = 0; rowId < DIMENSION; rowId++) {
                Tile[] row = fieldModel.getTileMap()[rowId];
                for (int colId = 0; colId < row.length; colId++) {
                    Tile tile = row[colId];
                    if (!tile.isInShip() && x == tile.getX() && y == tile.getY()) {
                        //checking selected tile
                        ArrayList<Tile> neighborTiles = new ArrayList<>();
                        for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
                            for (int colOffset = -1; colOffset < 2; colOffset++) {
                                try {
                                    Tile neighbor = fieldModel.getTileMap()[rowId + rowOffset][colId + colOffset];
                                    if (neighbor.isInShip()) {
                                        //checking diagonals
                                        if (rowOffset != 0 && colOffset != 0) {
                                            Toast.makeText(parentContext, "You can't place ships diagonally", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                        //not diagonal neighbors will be analyzed
                                        neighborTiles.add(neighbor);
                                    }
                                } catch (IndexOutOfBoundsException exception) {
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
                        drawman.drawTile(tile);
                        return true;
                    }
                }
            }
            return true;
        }
    }

}
