package com.justforf.terenfear.simpleshipfight;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
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
    private Toolbar toolbar;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                DrawUtils.drawField(canvas, firstField, tileMap);
                gestureDetector = new GestureDetector(MainActivity.this, new PrepGestureListener());
                firstField.setOnTouchListener(new PrepTouchListener());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.arrangement_menu, menu);
        menu.findItem(R.id.doneArrangementItem).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.clearArrangementItem).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.doneArrangementItem:
                String message = "";
                boolean isQuantityRight = true;
                for (int shipType = 0; shipType < 4; shipType++) {
                    int currentNumber = shipQuantity[shipType];
                    int desiredNumber = 4 - shipType;
                    if (currentNumber != desiredNumber) {
                        message += "Wrong number of " + (shipType + 1) + "-ships. You need " + desiredNumber + " of it (" + (desiredNumber - currentNumber) + " more).\n";
                        isQuantityRight = false;
                    }
                }
                if (!isQuantityRight) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(message).setPositiveButton("Ok", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            case R.id.clearArrangementItem:
                for (Ship ship : ships)
                    ship.removeAllParts();
                ships.clear();
                DrawUtils.drawField(canvas, firstField, tileMap);
                updateShipQuantity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        for (int shipType = 0; shipType < 4; shipType++) {
            int totalQuantity = shipQuantity[shipType];
            int desiredQuantity = 4 - shipType;
            TextView correspondingTV = quantityTextViews.get(shipType);
            if (totalQuantity > desiredQuantity)
                correspondingTV.setTextColor(Color.RED);
            else {
                if (totalQuantity < desiredQuantity)
                    correspondingTV.setTextColor(ContextCompat.getColor(this, R.color.colorLightOrange));
                else correspondingTV.setTextColor(Color.GREEN);
            }

        }
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
                        DrawUtils.drawTile(canvas, firstField, part);
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
                        DrawUtils.drawTile(canvas, firstField, tile);
//                            drawField();
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
