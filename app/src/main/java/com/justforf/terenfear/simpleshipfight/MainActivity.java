package com.justforf.terenfear.simpleshipfight;

import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements TurnEndListener {
    private Toolbar toolbar;
    private FieldController playerField;
    private FieldController enemyField;
    private Bot enemy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerField = new FieldController(this, (FieldView) this.findViewById(R.id.firstField));
        enemyField = new FieldController(this, (FieldView) this.findViewById(R.id.secondField));
        enemyField.setVisibility(View.INVISIBLE);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.arrangement_menu, menu);
        menu.findItem(R.id.doneArrangementItem).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.doneArrangementItem:
                String message = "";
                boolean[] enoughShips = playerField.checkShipQuantityStatus();
                boolean isQuantityRight = true;
                for (int shipType = 0; shipType < 4; shipType++) {
                    if (!enoughShips[shipType]) {
                        message += "Wrong number of " + (shipType + 1) + "-ships.\n";
                        isQuantityRight = false;
                    }
                }
                if (!isQuantityRight) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(message).setPositiveButton("Ok", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    playerField.removeListeners();
                    enemy = new Bot(playerField, enemyField, this, 4);
                    toolbar.getMenu().clear();
                }
                return true;
            case R.id.clearArrangementItem:
                playerField.clearField();
//                playerField.generateField(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void turnEnded(LastPlayer lastPlayer) {
        switch (lastPlayer) {
            case HUMAN:
                enemyField.removeListeners();
                enemy.makeShot(null);
                break;
            case BOT:
                enemyField.enableShotListeners();
                break;
        }
    }
}
