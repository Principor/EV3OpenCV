package com.example.ev3opencv;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

class EV3Communicator {

    private static ConnectTask connectTask;

    EV3Communicator() {
        connectTask = new ConnectTask();
    }

    static class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private ServerSocket socket;
        private DataOutputStream out;
        private boolean isConnected = false;
        protected Boolean doInBackground(Void... params) {
            try {
                Log.i(MainActivity.TAG, "Serversocket creation");
                socket = new ServerSocket(1234);
                Log.i(MainActivity.TAG, "accepting connection");
                Socket conn = socket.accept();
                out = new DataOutputStream(conn.getOutputStream());
                return true;
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
                Log.e(MainActivity.TAG, e.getCause().toString());
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                isConnected = true;
            }
            Log.i(MainActivity.TAG, "Connect state:" + isConnected);
        }

        void close() {
            try {
                out.close();
                socket.close();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, "Cannot close connection");
            }
        }
    }

    boolean isConnected() {
        return connectTask.isConnected;
    }

    void execute() {
        connectTask.execute();
    }


    void sendDirection(double direction) {
        if( isConnected() ) {
            try {
                connectTask.out.writeDouble(direction);
            } catch (IOException e) {
                Log.e(MainActivity.TAG,"Cannot send, connection terminated");
                connectTask.close();
                connectTask = new ConnectTask();
                connectTask.execute();
            }
        }
    }

    static String getIPAddress(boolean useIPv4) throws SocketException {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    String sAddr = addr.getHostAddress();
                    boolean isIPv4 = sAddr.indexOf(':')<0;

                    if (useIPv4) {
                        if (isIPv4)
                            return sAddr;
                    } else {
                        if (!isIPv4) {
                            int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                            return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                        }
                    }
                }
            }
        }
        return null;
    }
}
