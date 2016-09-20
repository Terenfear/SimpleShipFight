package com.justforf.terenfear.simpleshipfight;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int DIMENSION = 10;
    private static int TILE_SIZE = 72;
    private static int X_OFFSET = 0;
    private Tile[] tileMap[];
    private Canvas canvas;
    private ArrayList<Ship> ships;
    private ImageView firstField;
    private int shipQuantity[] = {0, 0, 0, 0};
    private TextView tvQuantityOf1;
    private TextView tvQuantityOf2;
    private TextView tvQuantityOf3;
    private TextView tvQuantityOf4;
    private ArrayList<TextView> quantityTextViews;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvQuantityOf1 = (TextView) findViewById(R.id.tvShip1);
        tvQuantityOf2 = (TextView) findViewById(R.id.tvShip2);
        tvQuantityOf3 = (TextView) findViewById(R.id.tvShip3);
        tvQuantityOf4 = (TextView) findViewById(R.id.tvShip4);
        quantityTextViews = new ArrayList<>();
        quantityTextViews.add(tvQuantityOf1);
        quantityTextViews.add(tvQuantityOf2);
        quantityTextViews.add(tvQuantityOf3);
        quantityTextViews.add(tvQuantityOf4);
        firstField = (ImageView) findViewById(R.id.ivFirstField);
        firstField.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                firstField.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int fieldHeight = firstField.getHeight();
                X_OFFSET = (firstField.getWidth() - firstField.getHeight()) / 2;
                TILE_SIZE = fieldHeight / DIMENSION;
                ships = new ArrayList<>();
                tileMap = new Tile[DIMENSION][DIMENSION];
                for (int rowId = 0; rowId < DIMENSION; rowId++) {
                    for (int colId = 0; colId < DIMENSION; colId++) {
                        tileMap[rowId][colId] = new Tile(colId * TILE_SIZE, rowId * TILE_SIZE, TILE_SIZE);
                    }
                }
                Bitmap bitmap = Bitmap.createBitmap(TILE_SIZE * DIMENSION, TILE_SIZE * DIMENSION, Bitmap.Config.ARGB_8888);
                firstField.setImageBitmap(bitmap);
                canvas = new Canvas(bitmap);
                drawMap();
                gestureDetector = new GestureDetector(MainActivity.this, new PrepGestureListener());
                firstField.setOnTouchListener(new PrepTouchListener());
            }
        });
    }

    void updateShipQuantity() {
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
        tvQuantityOf1.setText("x " + shipQuantity[0]);
        tvQuantityOf2.setText("x " + shipQuantity[1]);
        tvQuantityOf3.setText("x " + shipQuantity[2]);
        tvQuantityOf4.setText("x " + shipQuantity[3]);
        for (int shipType = 0; shipType < 4; shipType++){
            int totalQuantity = shipQuantity[shipType];
            int desiredQuantity = 4 - shipType;
            TextView correspondingTV = quantityTextViews.get(shipType);
            if (totalQuantity > desiredQuantity)
                correspondingTV.setTextColor(Color.RED);
            else {
                if (totalQuantity < desiredQuantity)
                    correspondingTV.setTextColor(0xffffaa00);
                else correspondingTV.setTextColor(0xff00aa00);
            }

        }
    }

    public void drawMap() {
        Paint clearing = new Paint(Paint.ANTI_ALIAS_FLAG);
        clearing.setColor(Color.WHITE);
        Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setColor(Color.BLACK);
        Paint shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipPaint.setColor(Color.BLUE);
        canvas.drawPaint(clearing);
        for (Tile[] row : tileMap)
            for (Tile tile : row) {
                if (tile.isInShip())
                    canvas.drawRect(tile, shipPaint);
                else canvas.drawRect(tile, emptyPaint);
            }
        firstField.invalidate();
    }

    public void drawTile(Tile tile) {
        Paint clearing = new Paint(Paint.ANTI_ALIAS_FLAG);
        clearing.setColor(Color.WHITE);
        Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setColor(Color.BLACK);
        Paint shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipPaint.setColor(Color.BLUE);
        canvas.drawRect(tile, clearing);
        if (tile.isInShip())
            canvas.drawRect(tile, shipPaint);
        else canvas.drawRect(tile, emptyPaint);
        firstField.invalidate((int) tile.left, (int) tile.top, (int) tile.right, (int) tile.bottom);
    }

    public class PrepTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gestureDetector.onTouchEvent(motionEvent);
            return true;
        }
    }

    public class PrepGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("onTouch", "long tap");
            float x = e.getX() - X_OFFSET;
            float y = e.getY();
            for (Ship ship : ships) {
                for (Tile part : ship.getAllParts()) {
                    if (x > part.left && x < part.right && y > part.top && y < part.bottom) {
                        int partId = ship.getAllParts().indexOf(part);
                        ship.removePart(part);
                        part.setParentShip(null);
                        if (ship.getSize() == 0) {
                            ships.remove(ship);
                        } else {
                            if (partId != 0 && partId != ship.getSize()) {
                                ArrayList<Tile> secondParts = new ArrayList<>();
                                while (partId < ship.getSize()) {
                                    secondParts.add(ship.getPart(partId));
                                    ship.removePart(partId);
                                }
                                Ship secondShip = new Ship(secondParts);
                                ships.add(secondShip);
                            }
                        }
                        updateShipQuantity();
                        drawTile(part);
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
            float x = e.getX() - X_OFFSET;
            float y = e.getY();
            for (int rowId = 0; rowId < tileMap.length; rowId++) {
                Tile[] row = tileMap[rowId];
                for (int colId = 0; colId < row.length; colId++) {
                    Tile tile = row[colId];
                    if (!tile.isInShip() && x > tile.left && x < tile.right && y > tile.top && y < tile.bottom) {
                        //checking selected tile
                        ArrayList<Tile> neighborTiles = new ArrayList<>();
                        for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
                            for (int colOffset = -1; colOffset < 2; colOffset++) {
                                try {
                                    Tile neighbor = tileMap[rowId + rowOffset][colId + colOffset];
                                    if (neighbor.isInShip()) {
                                        //checking diagonals
                                        if (rowOffset != 0 && colOffset != 0) {
                                            Toast.makeText(MainActivity.this, "You can't place ships diagonally", Toast.LENGTH_SHORT).show();
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
                                    ships.remove(secondNeighbor);
                                    firstNeighbor.sort();
                                } else {
                                    Toast.makeText(MainActivity.this, "Resulting ship is too big", Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                break;
                            case 1:
                                Ship neighborShip = neighborTiles.get(0).getParentShip();
                                if (!neighborShip.addPart(tile)) {
                                    Toast.makeText(MainActivity.this, "Resulting ship is too big", Toast.LENGTH_SHORT).show();
                                    return true;
                                } else neighborShip.sort();
                                break;
                            case 0:
                                ArrayList parts = new ArrayList<Tile>();
                                parts.add(tile);
                                Ship ship = new Ship(parts);
                                tile.setParentShip(ship);
                                ship.sort();
                                ships.add(ship);
                                break;
                            default:
                                Log.i("long tap switch", "unindetified error");
//                                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                return true;
                        }
                        updateShipQuantity();
                        drawTile(tile);
//                            drawMap();
                        return true;
                    }
                }
            }
            return true;
        }
    }
/*
    public class PrepRemoveListener implements OnLong*/
}
