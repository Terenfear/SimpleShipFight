package com.justforf.terenfear.simpleshipfight;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

/**
 * Created by Terenfear on 21.11.2016.
 */


public final class Drawman {
    private Paint clearing;
    private Paint emptyPaint;
    private Paint shipPaint;
    private Paint shotPaint;
    private Paint destroyedPaint;

    public Drawman() {
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

    public void drawVisibleField(ImageView fieldImageView, Canvas canvas, Tile[][] tileMap) {
        canvas.drawPaint(clearing);
        for (Tile[] row : tileMap)
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

    public void drawInvisibleField(ImageView fieldImageView, Canvas canvas, Tile[][] tileMap) {
        canvas.drawPaint(clearing);
        for (Tile[] row : tileMap)
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

    public void drawTile(ImageView fieldImageView, Canvas canvas, Tile tile) {
        canvas.drawRect(tile, clearing);
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
