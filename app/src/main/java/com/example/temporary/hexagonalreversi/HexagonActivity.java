//  HexagonalReversi (c) 2017 Doug Lee

package com.example.temporary.hexagonalreversi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class HexagonActivity extends ActionBarActivity
{
    HexagonView view;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hexagon);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_hexagon);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                                              | ActionBar.DISPLAY_SHOW_TITLE);

        Position.SetSide(6);

        view        = (HexagonView) findViewById(R.id.hexagon);
        view.pass   = (Button)      findViewById(R.id.pass);
        view.red    = (TextView)    findViewById(R.id.red);
        view.blue   = (TextView)    findViewById(R.id.blue);
        view.status = (TextView)    findViewById(R.id.status);
        view.pass.setOnClickListener(view.new PassListener());
        view.Restart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        view.position.PutState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        view.position.GetState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_hexagon, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.findItem(R.id.menu_size4).setChecked(false);
        menu.findItem(R.id.menu_size5).setChecked(false);
        menu.findItem(R.id.menu_size6).setChecked(false);
        menu.findItem(R.id.menu_level6).setChecked(false);
        menu.findItem(R.id.menu_level7).setChecked(false);
        menu.findItem(R.id.menu_level8).setChecked(false);

        switch(Position.side)
        {
            case 4:
                menu.findItem(R.id.menu_size4).setChecked(true);
                break;
            case 5:
                menu.findItem(R.id.menu_size5).setChecked(true);
                break;
            case 6:
                menu.findItem(R.id.menu_size6).setChecked(true);
                break;
        }

        switch(Position.level)
        {
            case 6:
                menu.findItem(R.id.menu_level6).setChecked(true);
                break;
            case 7:
                menu.findItem(R.id.menu_level7).setChecked(true);
                break;
            case 8:
                menu.findItem(R.id.menu_level8).setChecked(true);
                break;
        }

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch(item.getItemId())
        {
            case R.id.menu_restart:
                view.Restart();
                return true;
            case R.id.menu_size4:
                Position.SetSide(4);
                view.Restart();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_size5:
                Position.SetSide(5);
                view.Restart();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_size6:
                Position.SetSide(6);
                view.Restart();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_level6:
                Position.level = 6;
                invalidateOptionsMenu();
                return true;
            case R.id.menu_level7:
                Position.level = 7;
                invalidateOptionsMenu();
                return true;
            case R.id.menu_level8:
                Position.level = 8;
                invalidateOptionsMenu();
                return true;
            case R.id.menu_about:
                new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher_hexagon)
                .setTitle(R.string.app_name)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.about_button, null)
                .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
