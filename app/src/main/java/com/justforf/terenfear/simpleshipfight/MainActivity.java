package com.justforf.terenfear.simpleshipfight;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int DIMENSION = 10;
    private static final int TILE_SIZE = 48;
    Tile[] tileMap[];
    Canvas canvas;
    ArrayList<Ship> ships;
    ImageView myMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ships = new ArrayList<>();
        tileMap = new Tile[DIMENSION][DIMENSION];
        for (int rowId = 0; rowId < DIMENSION; rowId++) {
            for (int colId = 0; colId < DIMENSION; colId++) {
                tileMap[rowId][colId] = new Tile(colId * TILE_SIZE, rowId * TILE_SIZE, TILE_SIZE);
            }
        }
        myMap = new ImageView(this);
        Bitmap bitmap = Bitmap.createBitmap(TILE_SIZE * DIMENSION, TILE_SIZE * DIMENSION, Bitmap.Config.ARGB_8888);
        myMap.setImageBitmap(bitmap);
        myMap.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(myMap);
        canvas = new Canvas(bitmap);
        drawMap();
        myMap.setOnTouchListener(new PrepStageListener());
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
        myMap.invalidate();
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
        myMap.invalidate((int) tile.left, (int) tile.top, (int) tile.right, (int) tile.bottom);
    }

    public class PrepStageListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
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
                                    } catch (IndexOutOfBoundsException e) {
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
                                        tile.setParentShip(firstNeighbor);
                                        firstNeighbor.addPart(tile);
                                        for (Tile part : secondNeighbor.getAllParts())
                                            firstNeighbor.addPart(part);
                                        ships.remove(secondNeighbor);
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
                                    }
                                    tile.setParentShip(neighborShip);
                                    break;
                                case 0:
                                    ArrayList parts = new ArrayList<Tile>();
                                    parts.add(tile);
                                    Ship ship = new Ship(parts);
                                    tile.setParentShip(ship);
                                    ships.add(ship);
                                    break;
                                default:
                                    Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    return true;
                            }
                            //TODO: Redraw
                            drawTile(tile);
//                            drawMap();
                            return true;
                        }
                    }
                }
            }
            return true;
        }
    }
}
