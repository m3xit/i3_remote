package com.example.i3remote;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SSHConnection ssh;
    private boolean move;
    private Button moveButton;
    private List<Host> hosts;
    private int currentHost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        hosts = new ArrayList<>();
        hosts.add(new Host("max-laptop-arch", "max", "tinn10ja"));
        hosts.add(new Host("max-nuc", "max", "tinn10ja"));

        changeHost();

        moveButton = findViewById(R.id.move);
        setMove(false);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleText(intent.getStringExtra(Intent.EXTRA_TEXT));
            }
        }
    }

    @Override
    protected void onDestroy() {
        ssh.disconnect();

        super.onDestroy();
    }

        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setText(url);
        if (url.contains("youtu.be") || url.contains("youtube")) {
            Toast.makeText(this, "Playing: " + url, Toast.LENGTH_LONG).show();
            ssh.sendParallelCommand(CommandGenerator.generateCommand("./.scripts/mpv_parameter " + url + " > /dev/null 2>&1 &"));
        } else {
            Toast.makeText(this, "Unknown URL: " + url, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.host_left:
                currentHost --;
                changeHost();
                return true;
            case R.id.host_right:
                currentHost ++;
                changeHost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeHost() {
        currentHost = Math.floorMod(currentHost, hosts.size());
        Host newHost = hosts.get(currentHost);

        setTitle(getResources().getString(R.string.app_name) + "@" + newHost.hostname);

        ssh = new SSHConnection(newHost);
    }

    private void sendCommand(Command command) {
        ssh.sendCommand(command);
        setMove(false);
    }

    private void setMove(boolean move) {
        if (move) {
            this.move = true;
            moveButton.setBackgroundColor(getColor(R.color.colorAccent));
        } else {
            this.move = false;
            moveButton.setBackgroundColor(getColor(R.color.colorButton));
        }
    }

    private void workspace(int workspace) {
        if (move) {
            sendCommand(CommandGenerator.generatei3Command("move container to workspace number " + String.valueOf(workspace)));
        } else {
            sendCommand(CommandGenerator.generatei3Command("workspace " + String.valueOf(workspace)));
        }
    }

    private void direction(String direction) {
        if (move) {
            sendCommand(CommandGenerator.generatei3Command("move " + direction));
        } else {
            sendCommand(CommandGenerator.generatei3Command("focus " + direction));
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ws1:
                workspace(1);
                break;
            case R.id.ws2:
                workspace(2);
                break;
            case R.id.ws3:
                workspace(3);
                break;
            case R.id.ws4:
                workspace(4);
                break;
            case R.id.ws5:
                workspace(5);
                break;
            case R.id.ws6:
                workspace(6);
                break;
            case R.id.ws7:
                workspace(7);
                break;
            case R.id.ws8:
                workspace(8);
                break;
            case R.id.ws9:
                workspace(9);
                break;
            case R.id.ws0:
                workspace(0);
                break;

            case R.id.up:
                direction("up");
                break;
            case R.id.down:
                direction("down");
                break;
            case R.id.left:
                direction("left");
                break;
            case R.id.right:
                direction("right");
                break;
            case R.id.fullscreen:
                sendCommand(CommandGenerator.generatei3Command("fullscreen"));
                break;

            case R.id.move:
                setMove(!move);
                break;

            case R.id.youtube:
                handleText((String) ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).getText());
                break;
            case R.id.exec:
                getCommand();
                break;

            case R.id.q:
                sendCommand(CommandGenerator.generateCommand("xte 'key q'"));
                break;
            case R.id.space:
                sendCommand(CommandGenerator.generateCommand("./.scripts/xte_pause"));
                break;
        }
    }

    private void getCommand() {
        int nightModeFlags =
                getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        final AlertDialog.Builder dialogBuilder;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_NO:
                dialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                break;
            default:
                dialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        }

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_edit_text, null);

        final EditText editText = dialogView.findViewById(R.id.text);
        editText.requestFocus();

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                sendCommand(CommandGenerator.generateCommand(editText.getText().toString()));
                closeKeyboard();
            }
        });

        dialogBuilder.setMessage("Enter Command");
        dialogBuilder.create().show();
        showKeyboard();
    }

    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}