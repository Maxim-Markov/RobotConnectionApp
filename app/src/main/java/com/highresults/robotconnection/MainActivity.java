package com.highresults.robotconnection;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button connectButton;
    private SeekBar firstServo;
    private Joystick mJoystick;
    private SeekBar secondServo;
    private TextView textForward;
    private TextView textSide;
    private TextView errorText;
    private WifiServer mServer;

    //array for data
    //if first byte 0xFF then data for joystic(second and third for x and y data, fourth for checkSum)
    //if first byte 0x11 and second 0x22 then third for first servo data, fourth for checkSum
    //if first byte 0x11 and second 33 then third for second servo data, fourth for checkSum
    byte[] message = new byte[4];

    //classical crc8
    private byte calculateChecksum(byte[] array) {
        int i;
        byte byteWord = -1;
        for (int i2 = 0; i2 < 3; i2++) {
            byteWord = (byte) (array[i2] ^ byteWord);
            for (int j = 0; j < 8; j++) {
                if ((byteWord & 128) != 0) {
                    i = ((byte) (byteWord << 1)) ^ 49;
                } else {
                    i = byteWord << 1;
                }
                byteWord = (byte) i;
            }
        }
        return byteWord;
    }

    public void initVariables() {
        firstServo = findViewById(R.id.firstServo);
        secondServo = findViewById(R.id.secondServo);
        errorText = findViewById(R.id.errorText);
        textForward = findViewById(R.id.forwardMovementNumber);
        textSide = findViewById(R.id.sideMovementNumber);
        connectButton = findViewById(R.id.connectButton);
        message[0] = -1;
        mJoystick = findViewById(R.id.Joystick);
        mServer = new WifiServer(errorText);
        mServer.startConnection();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initVariables();
        connectButton.setOnClickListener(V -> mServer.startConnection());
        firstServo.setOnSeekBarChangeListener(new SeekBarChangeListener(0x22));
        secondServo.setOnSeekBarChangeListener(new SeekBarChangeListener(0x33));
        mJoystick.setOnTouchListener(new JoystickOnTouchListener());
    }

    class JoystickOnTouchListener implements View.OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View V, MotionEvent mEvent) {
            mJoystick.x = (int) mEvent.getX();
            mJoystick.y = (int) mEvent.getY();
            switch (mEvent.getAction()) {
                //not touching the joystic
                case MotionEvent.ACTION_DOWN:
                    mJoystick.x0 = (int) mEvent.getX();
                    mJoystick.y0 = (int) mEvent.getY();
                    textSide.setText(R.string.side);
                    textForward.setText(R.string.forward);
                    mJoystick.cleanCanvasFlag = false;
                    break;
                //take the finger off the screen
                case MotionEvent.ACTION_UP:
                    message[1] = 0;
                    message[2] = 0;
                    message[3] = calculateChecksum(message);
                    //send that we finish send data about moving robot
                    if (mServer == null) {
                        errorText.post(() -> errorText.setText(R.string.error_conn));
                    }
                    mServer.sendData(message);
                    mJoystick.cleanCanvasFlag = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int shiftX = mJoystick.x - mJoystick.x0;
                    int shiftY = mJoystick.y0 - mJoystick.y;
                    if (shiftX > 200) {
                        shiftX = 200;
                    }
                    if (shiftX < -200) {
                        shiftX = -200;
                    }
                    if (shiftY > 200) {
                        shiftY = 200;
                    }
                    if (shiftY < -200) {
                        shiftY = -200;
                    }
                    message[1] = (byte) (shiftX / 4);
                    message[2] = (byte) (shiftY / 4);
                    message[3] = calculateChecksum(message);
                    textSide.setText(String.format(getString(R.string.sideVal), message[1]));
                    textForward.setText(String.format(getString(R.string.forwardVal), message[2]));
                    mServer.sendData(message);
                    break;
            }
            mJoystick.invalidate();
            return true;
        }
    }

    class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private final int secondByte;

        public SeekBarChangeListener(int secondByte) {
            this.secondByte = secondByte;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            message[2] = (byte) progress;
            message[3] = calculateChecksum(message);
            mServer.sendData(message);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            message[0] = 0x11;
            message[1] = (byte) secondByte;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            message[0] = -1;
        }
    }
}
