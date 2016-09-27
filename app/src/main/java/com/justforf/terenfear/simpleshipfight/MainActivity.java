package com.justforf.terenfear.simpleshipfight;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FieldView firstField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                firstField.clearField();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
