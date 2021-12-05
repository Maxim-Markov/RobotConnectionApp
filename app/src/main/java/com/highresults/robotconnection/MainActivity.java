package com.highresults.robotconnection;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Server Name";

    private static WifiServer mServer;
    private Button connectButton;
    private TextView errorText;
    private SeekBar firstServo;
    private Joystick mJoystick;
    private SeekBar secondServo;
    private TextView textForward;
    private TextView textSide;
    //array for data
    //if first byte -1 then data for joystic(second and third for x and y data, fourth for checkSum)
    //if first byte 11 and second 22 then third for first servo data, fourth for checkSum
    //if first byte 11 and second 33 then third for second servo data, fourth for checkSum
    byte[] message = new byte[4];

    public class WifiServer {
        private final static String ipAddress = "192.168.4.1";
        private Socket mSocket = null;
        private final static int port = 5555;

        public WifiServer() {
        }

        private void openConnection() {
            closeConnection();
            try {
                this.mSocket = new Socket(ipAddress, port);
            } catch (IOException err) {
                errorText.post(() -> errorText.setText("unavailable to create socket. Live with it "));
                Log.e(TAG, "unavailable to create socket. Live with it : " + err.getMessage());
            }
        }

        private void closeConnection() {
            if (mSocket != null && !mSocket.isClosed()) {
                try (Socket ignored = mSocket) {
                } catch (IOException err) {
                    errorText.post(() -> errorText.setText("unavailable to close socket: "));
                    Log.e(TAG, "unavailable to close socket: " + err.getMessage());
                }
            }
        }

        private void sendData(byte[] data) throws IOException {
            Socket socket = this.mSocket;
            if(mServer == null){
                errorText.post(() -> errorText.setText("Connection wasn't created. Try again"));
                Log.e(TAG, "Connection wasn't created. Restart app");
            }
            if (socket == null || socket.isClosed()) {
                errorText.post(() -> errorText.setText("unavailable to send data. The socket is closed or wasn't created.Live with it"));
                Log.e(TAG, "unavailable to send data. The socket is closed or wasn't created.Live with it: ");
            } else {
                try {
                    socket.getOutputStream().write(data[0]);
                    socket.getOutputStream().flush();
                } catch (IOException err) {
                    errorText.post(() -> errorText.setText("unavailable to send data.Live with it"));
                    Log.e(TAG, "unavailable to send data.");
                    throw err;
                }
                socket.getOutputStream().write(data);
                socket.getOutputStream().flush();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            closeConnection();
        }
    }

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

    private void startConnection() {
        mServer = new WifiServer();
        new Thread(() -> {
            try {
                mServer.openConnection();
                errorText.post(() -> errorText.setText("Connection successful"));
                Log.d(TAG, "Connection successful");
            } catch (Exception err) {
                errorText.post(() -> errorText.setText("Connection wasn't created. Live with it"));
                Log.e(TAG, err.getMessage());
                mServer = null;
            }
        }).start();
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
        startConnection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initVariables();
        connectButton.setOnClickListener(V -> startConnection());
        firstServo.setOnSeekBarChangeListener(new SeekBarChangeListener(22));
        secondServo.setOnSeekBarChangeListener(new SeekBarChangeListener(33));
        mJoystick.setOnTouchListener(new JoistickOnTouchListener());
    }

     class JoistickOnTouchListener implements View.OnTouchListener {
        public boolean onTouch(View V, MotionEvent mEvent) {
            mJoystick.x = (int) mEvent.getX();
            mJoystick.y = (int) mEvent.getY();
            switch (mEvent.getAction()) {
                //not touching the joystic
                case MotionEvent.ACTION_DOWN:
                    mJoystick.x0 = (int) mEvent.getX();
                    mJoystick.y0 = (int) mEvent.getY();
                    textSide.setText("side: 0");
                    textForward.setText("forward: 0");
                    mJoystick.cleanCanvasFlag = false;
                    break;
                    //take the finger off the screen
                case MotionEvent.ACTION_UP:
                    message[1] = 0;
                    message[2] = 0;
                    message[3] = calculateChecksum(message);
                    try {
                        //send that we finish send data about moving robot
                        mServer.sendData(message);
                    } catch (Exception err) {
                        Log.e(TAG, err.getMessage());
                    }
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
                    textSide.setText("side: " + message[1]);
                    textForward.setText("forward: " + message[2]);
                    try {
                        mServer.sendData(message);
                    } catch (Exception err2) {
                        Log.e(TAG, err2.getMessage());
                        break;
                    }
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
            try {
                mServer.sendData(message);
            } catch (Exception err) {
                Log.e(TAG, err.getMessage());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            message[0] = 11;
            message[1] = (byte) secondByte;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            message[0] = -1;
        }
    }
}
