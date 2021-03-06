package me.eighttenlabs.toggleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


public class ControllerActivity extends Activity implements View.OnTouchListener {

    MouseControl mControl;
    TypeBox text;
    Sender sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String ip = getIntent().getStringExtra("server_ip");
        new ConnectTask().execute(ip);

        text = (TypeBox) findViewById(R.id.text);
        text.setActivity(this);

        mouseButtons();
        specialButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sender != null) {
            sender.stopClient();
            sender = null;
        }
    }

    private void controlMouse(MotionEvent event) {
        mControl.onTouch(event);
    }

    private void specialButtons() {
        findViewById(R.id.button_shift).setOnTouchListener(this);
        findViewById(R.id.button_delete).setOnTouchListener(this);
        findViewById(R.id.button_alt).setOnTouchListener(this);
        findViewById(R.id.button_ctrl).setOnTouchListener(this);
    }

    public boolean onTouch(View v, MotionEvent event) {
        int type = 0;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            type = 1;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            type = 2;
        }
        String key = null;
        switch (v.getId()) {
            case R.id.button_shift:
                key = Constants.SHIFT;
                break;
            case R.id.button_delete:
                key = Constants.DELETE;
                break;
            case R.id.button_alt:
                key = Constants.ALT;
                break;
            case R.id.button_ctrl:
                key = Constants.CTRL;
                break;
            case R.id.button_click_left:
                key = Constants.LEFT;
                type += 3;
                break;
            case R.id.button_click_right:
                key = Constants.RIGHT;
                type += 3;
                break;
        }
        if (type != 0 && type != 3) {
            sender.sendCommand(Constants.SPECIAL, key + ":" + type);
        }
        return true;
    }

    private void mouseButtons() {
        FrameLayout mousePad = (FrameLayout) findViewById(R.id.mouse_pad);
        mControl = new MouseControl(this);
        mousePad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                controlMouse(event);
                return true;
            }
        });
        findViewById(R.id.button_click_left).setOnTouchListener(this);
        findViewById(R.id.button_click_right).setOnTouchListener(this);
    }

    public class ConnectTask extends AsyncTask<String, String, Sender> {
        @Override
        protected Sender doInBackground(String... params) {
            sender = new Sender(getApplicationContext(), params[0]);
            sender.run();
            return sender;
        }

        @Override
        protected void onPostExecute(Sender sender) {
            if (sender != null && !sender.isSystemStop()) {
                Toast rejectToast = Toast.makeText(ControllerActivity.this, "Connection Failed", Toast.LENGTH_LONG);
                rejectToast.show();
            }
            Intent intent = new Intent(ControllerActivity.this, ServerSelectActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ControllerActivity.this.startActivity(intent);
            ControllerActivity.this.finish();
        }
    }
}
