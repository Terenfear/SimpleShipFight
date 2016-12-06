package com.justforf.terenfear.simpleshipfight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Terenfear on 21.11.2016.
 */

public class FieldView extends android.support.constraint.ConstraintLayout {
    public static final int DIMENSION = 10;
    private int xOffset = 0;

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    private Canvas canvas;
    private ImageView fieldImageView;
    private ArrayList<TextView> quantityTextViews;
    private FieldController fieldController;

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

    public int getxOffset() {
        return xOffset;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public ArrayList<TextView> getQuantityTextViews() {
        return quantityTextViews;
    }

    public ImageView getFieldImageView() {
        return fieldImageView;
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.field_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        TextView tvQuantityOf1 = (TextView) this.findViewById(R.id.tvShip1);
        TextView tvQuantityOf2 = (TextView) this.findViewById(R.id.tvShip2);
        TextView tvQuantityOf3 = (TextView) this.findViewById(R.id.tvShip3);
        TextView tvQuantityOf4 = (TextView) this.findViewById(R.id.tvShip4);
        quantityTextViews = new ArrayList<>();
        quantityTextViews.add(tvQuantityOf1);
        quantityTextViews.add(tvQuantityOf2);
        quantityTextViews.add(tvQuantityOf3);
        quantityTextViews.add(tvQuantityOf4);
        fieldImageView = (ImageView) this.findViewById(R.id.ivField);

    }
}
