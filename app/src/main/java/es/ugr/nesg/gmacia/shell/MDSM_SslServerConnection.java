package es.ugr.nesg.gmacia.shell;

import android.content.Context;
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
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by gmacia on 16/04/2018.
 */

public class MDSM_SslServerConnection implements Runnable {

    private String data;
    private Context c;

    public MDSM_SslServerConnection (String data, Context c) {
        long tim=System.currentTimeMillis();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String curTime =df.format(tim);
        this.data = curTime + "," + data;
        this.c = c;
    }
    public void run() {

        String TAG = "MDSM_SslServerConnection";

        String serverIp =  "150.214.190.75";
        int serverPort = 4343;
        Socket connectionSocket;
        OutputStream out;
        String TRUSTSTORE_PASSWORD = "prueba";

        KeyStore localTrustStore = null;
        try {
            URL url = new URL("https://mdsm1.ugr.es:4343/");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(c));

            out = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            out.close();

            String answer = readInputStreamToString(connection);

            Log.d(TAG, "Recibido: " + answer);
            connection.disconnect();


        } catch (Exception e) {
            Log.d(TAG, "Capturada excepcion: " + e.toString());
        }

    }

    private String readInputStreamToString(HttpsURLConnection connection) {
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
