package es.ugr.nesg.gmacia.mdsm;

/**
 * Created by gmacia on 21/04/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;


/**
 * Created by gmacia on 16/04/2018.
 */

public class MDSMServerSslSocketConnection implements Runnable {

    private String data;
    private Context c;
    private static String movilID;
    private int port;

    public MDSMServerSslSocketConnection(String data, Context c, int port) {
        long tim=System.currentTimeMillis();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String curTime =df.format(tim);
        SharedPreferences sharedPref = c.getSharedPreferences("es.ugr.nesg.gmacia.mdsm.PREFERENCES_FILE", Context.MODE_PRIVATE);
        movilID = sharedPref.getString("UUID", "");
        this.data = curTime + "," + movilID + "," + data;
        this.c = c;
        this.port = port;
    }
    public void run() {

        String TAG = MainActivity.class.getName();
        OutputStream out;

        try {
            SocketFactory sf = CustomSSLSocketFactory.getSSLSocketFactory(c);
            SSLSocket socket = (SSLSocket) sf.createSocket("mdsm1.ugr.es", this.port);
            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            SSLSession s = socket.getSession();

// Verify that the certicate hostname is for mail.google.com
// This is due to lack of SNI support in the current SSLSocket.
            if (!hv.verify("mdsm1.ugr.es", s)) {
                Log.d(TAG,"Expected mdsm1.ugr.es, found " + s.getPeerPrincipal() );
                throw new SSLHandshakeException("Expected mdsm1.ugr.es, found " + s.getPeerPrincipal());

            }
            out = new BufferedOutputStream(socket.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            out.close();

            String answer = readInputStreamToString(socket);

            Log.d(TAG, "Recibido: " + answer);
            socket.close();


        } catch (Exception e) {
            Log.d(TAG, "Capturada excepcion: " + e.toString());
        }

    }

    private String readInputStreamToString(SSLSocket connection) {
        String TAG = "readInputStreamToString";
        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        }
        catch (Exception e) {
            Log.i(TAG, "Error reading InputStream");
            result = null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    Log.i(TAG, "Error closing InputStream");
                }
            }
        }
        return result;
    }

}