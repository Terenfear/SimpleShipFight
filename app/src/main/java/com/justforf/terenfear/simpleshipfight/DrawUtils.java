package com.justforf.terenfear.simpleshipfight;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

/**
 * Created by Terenfear on 27.09.2016.
 */

public final class DrawUtils {

    public static void drawField(Canvas canvas, ImageView field, Tile[][] tileMap) {
        Paint clearing = new Paint(Paint.ANTI_ALIAS_FLAG);
        clearing.setColor(Color.WHITE);
        Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setColor(Color.BLACK);
        Paint shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipPaint.setColor(Color.BLUE);
        Paint closePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        closePaint.setColor(Color.RED);
        canvas.drawPaint(clearing);
        for (Tile[] row : tileMap)
            for (Tile tile : row) {
                if (!tile.isFarFromShips())
                    canvas.drawRect(tile, closePaint);
                if (tile.isInShip())
                    canvas.drawRect(tile, shipPaint);
                if (!tile.isInShip() && tile.isFarFromShips()) canvas.drawRect(tile, emptyPaint);
            }
        field.invalidate();
    }

    public static void drawTile(Canvas canvas, ImageView field, Tile tile) {
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
        field.invalidate((int) tile.left, (int) tile.top, (int) tile.right, (int) tile.bottom);
    }
}
