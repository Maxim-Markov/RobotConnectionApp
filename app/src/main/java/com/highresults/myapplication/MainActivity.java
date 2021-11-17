package com.highresults.myapplication;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
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
    static WifiServer mServer;
    Button connectButton;
    Button doNotTouchThis;
    TextView errorText;
    SeekBar firstServo;
    Joystick mJoystick;
    byte[] message = new byte[4];
    SeekBar secondServo;
    Boolean stop = false;
    TextView textForward;
    TextView textSide;
    Boolean touchFlag = false;

    public class WifiServer {
        private String ipAddress = "192.168.4.1";
        private Socket mSocket = null;
        private int port = 5555;

        public WifiServer() {
        }

        public void openConnection() throws Exception {
            closeConnection();
            try {
                this.mSocket = new Socket(this.ipAddress, this.port);
            } catch (IOException err) {
                MainActivity.this.errorText.post(() -> MainActivity.this.errorText.setText("unavailable to create socket. Live with it "));
                throw new Exception("unavailable to create socket : " + err.getMessage());
            }
        }

        public void closeConnection() {
            Socket socket = this.mSocket;
            if (socket != null && !socket.isClosed()) {
                try {
                    this.mSocket.close();
                } catch (IOException err) {
                    MainActivity.this.errorText.post(() -> MainActivity.this.errorText.setText("unavailable to close socket: "));
                    Log.e("Server Name", "unavailable to close socket: " + err.getMessage());
                } catch (Throwable th) {
                    this.mSocket = null;
                    throw th;
                }
                this.mSocket = null;
            }
            this.mSocket = null;
        }

        public void sendData(byte[] data) throws Exception {
            Socket socket = this.mSocket;
            if (socket == null || socket.isClosed()) {
                MainActivity.this.errorText.post(() -> MainActivity.this.errorText.setText("unavailable to send data. The socket is closed or wasn't created.Live with it"));
                throw new Exception("unavailable to send data. The socket is closed or wasn't created");
            }
            int i = 0;
            while (i < 4) {
                try {
                    this.mSocket.getOutputStream().write(data[i]);
                    this.mSocket.getOutputStream().flush();
                    i++;
                } catch (IOException err) {
                    MainActivity.this.errorText.post(new Runnable() {
                        public void run() {
                            MainActivity.this.errorText.setText("unavailable to send data.Live with it");
                        }
                    });
                    throw new Exception("unavailable to send data:" + err.getMessage());
                }
            }
            this.mSocket.getOutputStream().write(data);
            this.mSocket.getOutputStream().flush();
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            super.finalize();
            closeConnection();
        }
    }

    public byte calculateChecksum(byte[] array, int size) {
        int i;
        byte byteWord = -1;
        for (int i2 = 0; i2 < size; i2++) {
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

    public void startConnection() {
        mServer = new WifiServer();
        new Thread(new Runnable() {
            public void run() {
                try {
                    MainActivity.mServer.openConnection();
                    MainActivity.this.errorText.post(new Runnable() {
                        public void run() {
                            MainActivity.this.errorText.setText("Connection successful");
                        }
                    });
                } catch (Exception err) {
                    MainActivity.this.errorText.post(new Runnable() {
                        public void run() {
                            MainActivity.this.errorText.setText("Connection wasn't created. Live with it");
                        }
                    });
                    Log.e("My server", err.getMessage());
                    MainActivity.mServer = null;
                }
            }
        }).start();
    }

    public void initVariables() {
        this.firstServo = findViewById(R.id.firstServo);
        this.secondServo = findViewById(R.id.secondServo);
        this.doNotTouchThis = findViewById(R.id.doNotTouch);
        this.errorText = findViewById(R.id.errorText);
        this.textForward = findViewById(R.id.forwardMovementNumber);
        this.textSide = findViewById(R.id.sideMovementNumber);
        this.connectButton = findViewById(R.id.connectButton);
        this.message[0] = -1;
        this.mJoystick = findViewById(R.id.Joystick);
        startConnection();
    }

    public void DoNotTouchButtonListener() {
        final MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.sample);
        this.doNotTouchThis.setOnClickListener(V -> {
            if (!MainActivity.this.touchFlag) {
                MainActivity.this.doNotTouchThis.setText("I SAID NO!!!");
                MainActivity.this.touchFlag = true;
            } else if (!MainActivity.this.stop) {
                MainActivity.this.stop = true;
                mPlayer.start();
            } else {
                MainActivity.this.stop = false;
                mPlayer.stop();
                MainActivity.this.touchFlag = false;
                MainActivity.this.doNotTouchThis.setText("Don't touch");
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initVariables();
        DoNotTouchButtonListener();
        this.connectButton.setOnClickListener(V -> MainActivity.this.startConnection());
        this.firstServo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.this.message[2] = (byte) progress;
                byte[] bArr = MainActivity.this.message;
                MainActivity mainActivity = MainActivity.this;
                bArr[3] = mainActivity.calculateChecksum(mainActivity.message, 3);
                try {
                    MainActivity.mServer.sendData(MainActivity.this.message);
                } catch (Exception err) {
                    Log.e("My server", err.getMessage());
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                MainActivity.this.message[0] = 11;
                MainActivity.this.message[1] = 22;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.this.message[0] = -1;
            }
        });
        this.secondServo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.this.message[2] = (byte) progress;
                byte[] bArr = MainActivity.this.message;
                MainActivity mainActivity = MainActivity.this;
                bArr[3] = mainActivity.calculateChecksum(mainActivity.message, 3);
                try {
                    MainActivity.mServer.sendData(MainActivity.this.message);
                } catch (Exception err) {
                    Log.e("My server", err.getMessage());
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                MainActivity.this.message[0] = 11;
                MainActivity.this.message[1] = 33;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.this.message[0] = -1;
            }
        });
        this.mJoystick.setOnTouchListener(new View.OnTouchListener() {

            /* renamed from: X */
            int f36X;

            /* renamed from: X0 */
            Integer f37X0;

            /* renamed from: Y */
            int f38Y;

            /* renamed from: Y0 */
            Integer f39Y0;

            public boolean onTouch(View V, MotionEvent mEvent) {
                MainActivity.this.mJoystick.f32X = Integer.valueOf((int) mEvent.getX());
                MainActivity.this.mJoystick.f34Y = Integer.valueOf((int) mEvent.getY());
                switch (mEvent.getAction()) {
                    case 0:
                        MainActivity.this.mJoystick.f33X0 = Integer.valueOf((int) mEvent.getX());
                        MainActivity.this.mJoystick.f35Y0 = Integer.valueOf((int) mEvent.getY());
                        MainActivity.this.textSide.setText("side: 0");
                        MainActivity.this.textForward.setText("forward: 0");
                        MainActivity.this.mJoystick.cleanCanvasFlag = false;
                        break;
                    case 1:
                        MainActivity.this.message[1] = 0;
                        MainActivity.this.message[2] = 0;
                        byte[] bArr = MainActivity.this.message;
                        MainActivity mainActivity = MainActivity.this;
                        bArr[3] = mainActivity.calculateChecksum(mainActivity.message, 3);
                        try {
                            MainActivity.mServer.sendData(MainActivity.this.message);
                        } catch (Exception err) {
                            Log.e("My server", err.getMessage());
                        }
                        MainActivity.this.mJoystick.cleanCanvasFlag = true;
                        break;
                    case 2:
                        Integer shiftX = Integer.valueOf(MainActivity.this.mJoystick.f32X.intValue() - MainActivity.this.mJoystick.f33X0.intValue());
                        Integer shiftY = Integer.valueOf((-MainActivity.this.mJoystick.f34Y.intValue()) + MainActivity.this.mJoystick.f35Y0.intValue());
                        if (shiftX.intValue() > 200) {
                            shiftX = 200;
                        }
                        if (shiftX.intValue() < -200) {
                            shiftX = -200;
                        }
                        if (shiftY.intValue() > 200) {
                            shiftY = 200;
                        }
                        if (shiftY.intValue() < -200) {
                            shiftY = -200;
                        }
                        MainActivity.this.message[1] = (byte) (shiftX.intValue() / 4);
                        MainActivity.this.message[2] = (byte) (shiftY.intValue() / 4);
                        byte[] bArr2 = MainActivity.this.message;
                        MainActivity mainActivity2 = MainActivity.this;
                        bArr2[3] = mainActivity2.calculateChecksum(mainActivity2.message, 3);
                        TextView textView = MainActivity.this.textSide;
                        textView.setText("side: " + MainActivity.this.message[1]);
                        TextView textView2 = MainActivity.this.textForward;
                        textView2.setText("forward: " + MainActivity.this.message[2]);
                        try {
                            MainActivity.mServer.sendData(MainActivity.this.message);
                            break;
                        } catch (Exception err2) {
                            Log.e("My server", err2.getMessage());
                            break;
                        }
                }
                MainActivity.this.mJoystick.invalidate();
                return true;
            }
        });
    }
}
