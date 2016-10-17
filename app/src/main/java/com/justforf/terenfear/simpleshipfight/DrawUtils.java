package com.justforf.terenfear.simpleshipfight;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

/**
 * Created by Terenfear on 27.09.2016.
 */

public final class DrawUtils {
    private static Paint clearing;
    private static Paint emptyPaint;
    private static Paint shipPaint;
    private static Paint shotPaint;
    private static Paint destroyedPaint;
    private static boolean isInit = false;

    private static void init() {
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

    public static void drawVisibleField(Canvas canvas, ImageView field, Tile[][] tileMap) {
        if (!isInit) init();
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
        field.invalidate();
    }

    public static void drawInvisibleField(Canvas canvas, ImageView field, Tile[][] tileMap) {
        if (!isInit) init();
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
        field.invalidate();
    }

    public static void drawTile(Canvas canvas, ImageView field, Tile tile) {
        if (!isInit) init();
        if (tile.isInShip())
            if (tile.isShot())
                canvas.drawRect(tile, destroyedPaint);
            else canvas.drawRect(tile, shipPaint);
        else if (tile.isShot())
            canvas.drawRect(tile, shotPaint);
        canvas.drawRect(tile, emptyPaint);
        field.invalidate((int) tile.left, (int) tile.top, (int) tile.right, (int) tile.bottom);
    }
}
