
/**
 * Created by gmacia on 09/02/2018.
 */

package es.ugr.nesg.gmacia.shell;

import android.util.Log;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;


public class MDSMmServerConnection implements Runnable {
    private String data;

    public MDSMmServerConnection(String data) {
        this.data = data;
        this.data += "\n[+] End of message\n";

    }

    public void run() {


        String TAG = "TCPClient"; //For debugging, always a good idea to have defined
        //String serverIp = "150.214.190.75";
        String serverIp = "150.214.21.231";
        long startTime = 0l;
        int serverPort = 2004;
        Socket connectionSocket;
        OutputStream out;

        try {

            Log.d(TAG, "C: Connecting...");

            InetAddress serverAddr = InetAddress.getByName(serverIp);
            startTime = System.currentTimeMillis();

            //Create a new instance of Socket
            connectionSocket = new Socket();

            //Start connecting to the server with 5000ms timeout
            //This will block the thread until a connection is established
            connectionSocket.connect(new InetSocketAddress(serverAddr, serverPort), 5000);

            long time = System.currentTimeMillis() - startTime;
            Log.d(TAG, "Connected! Current duration: " + time + "ms");

            try {
                out = connectionSocket.getOutputStream();
                out.write(this.data.getBytes());
                out.flush();
            }
            catch (Exception e) {
                Log.d(TAG, "Error en el envío del mensaje: " + e.toString());
            }
            connectionSocket.close();
        } catch (Exception e) {
            //Catch the exception that socket.connect might throw
            Log.d(TAG, "No se ha podido conectar: " + e.toString());
        }
    }

}
