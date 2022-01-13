package com.highresults.robotconnection;

import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WifiServer {
    private static final String TAG = "Server Name";

    private final static String ipAddress = "192.168.4.1";
    private Socket mSocket = null;
    private final static int port = 5555;
    private final TextView errorText;

    public WifiServer(TextView errorText) {
        this.errorText = errorText;
    }

    void startConnection() {
        new Thread(() -> {
            try {
                openConnection();
            } catch (Exception err) {
                errorText.post(() -> errorText.setText(R.string.error_conn));
                Log.e(TAG, err.getMessage());
            }
        }).start();
    }

    private void openConnection() {
        try {
            this.mSocket = new Socket(ipAddress, port);
            errorText.post(() -> errorText.setText(R.string.succes_conn));
            Log.d(TAG, "Connection successful");
        } catch (IOException err) {
            errorText.post(() -> errorText.setText(R.string.error_create_socket));
            Log.e(TAG, "unavailable to create socket with 192.168.4.1, 5555" + err.getMessage());
        }
    }

    private void closeConnection() {
        if (mSocket != null && !mSocket.isClosed()) {
            try (Socket ignored = mSocket) {
            } catch (IOException err) {
                errorText.post(() -> errorText.setText(R.string.error_close_socket));
                Log.e(TAG, "unavailable to close socket: " + err.getMessage());
            }
        }
    }

    void sendData(byte[] data) {
        final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        Socket socket = this.mSocket;
        if (socket == null || socket.isClosed()) {
            errorText.post(() -> errorText.setText(R.string.error_send_soc_close));
            Log.e(TAG, "unavailable to send data. The socket is closed or wasn't created.Live with it: ");
        } else {
            executor.execute(() -> {
                try {
                    socket.getOutputStream().write(data);
                    socket.getOutputStream().flush();
                } catch (IOException err) {
                    errorText.post(() -> errorText.setText(R.string.error_send));
                    Log.e(TAG, "unavailable to send data.");
                }
            });
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }
}
