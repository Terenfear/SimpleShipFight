package com.justforf.terenfear.simpleshipfight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

/**
 * Created by Terenfear on 27.09.2016.
 */

public class FieldView extends android.support.constraint.ConstraintLayout {
    private static final int DIMENSION = 10;
    private int xOffset = 0;
    private int tileSize = 72;
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

    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public FieldView(Context context) {
        super(context);
        initView(context);
    }

    public FieldModel getFieldModel() {
        return fieldModel;
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
                DrawUtils.drawField(canvas, fieldImageView, fieldModel.getTileMap());
                gestureDetector = new GestureDetector(parentContext, new FieldView.PrepGestureListener());
                fieldImageView.setOnTouchListener(new gridOnTouchListener());
            }
        });

    }

    private void initView(Context context) {
        parentContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.field_layout, this);
    }

    public void removeListeners() {
        fieldImageView.setOnTouchListener(null);
    }

    public void enableFightListeners() {
//        fieldImageView.setOnTouchListener(null);
        gestureDetector = new GestureDetector(parentContext, new FieldView.FightGestureListener());
//        fieldImageView.setOnTouchListener(new gridOnTouchListener());
    }

    public void updateShipQuantity() {
        fieldModel.updateShipNumbers();
        int shipQuantity[] = fieldModel.getShipQuantity();
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
        DrawUtils.drawField(canvas, fieldImageView, fieldModel.getTileMap());
        updateShipQuantity();
    }

    public void generateRandomField() {
        boolean isGenerated;
        do {
            isGenerated = fieldModel.generateRandomField();
        }while (!isGenerated);
        DrawUtils.drawField(canvas, fieldImageView, fieldModel.getTileMap());
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
            Toast.makeText(parentContext, "SHOTS FIRED", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private class PrepGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("onTouch", "long tap");
            float x = e.getX() - xOffset;
            float y = e.getY();
            for (Ship ship : fieldModel.getShips()) {
                for (Tile part : ship.getAllParts()) {
                    if (x > part.left && x < part.right && y > part.top && y < part.bottom) {
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
                        DrawUtils.drawTile(canvas, fieldImageView, part);
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
            float x = e.getX() - xOffset;
            float y = e.getY();
            for (int rowId = 0; rowId < DIMENSION; rowId++) {
                Tile[] row = fieldModel.getTileMap()[rowId];
                for (int colId = 0; colId < row.length; colId++) {
                    Tile tile = row[colId];
                    if (!tile.isInShip() && x > tile.left && x < tile.right && y > tile.top && y < tile.bottom) {
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
//                                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                return true;
                        }
                        updateShipQuantity();
                        DrawUtils.drawTile(canvas, fieldImageView, tile);
//                            drawField();
                        return true;
                    }
                }
            }
            return true;
        }
    }

}
