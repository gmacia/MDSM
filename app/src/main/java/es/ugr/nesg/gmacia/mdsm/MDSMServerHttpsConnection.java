package es.ugr.nesg.gmacia.mdsm;

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

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by gmacia on 16/04/2018.
 */

public class MDSMServerHttpsConnection implements Runnable {

    private String data;
    private Context c;
    private static String movilID;
    private int port;

    public MDSMServerHttpsConnection(String data, Context c, int port) {
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

        String TAG = "MDSMServerHttpsConnection";
        OutputStream out;
        int intentos = 0;
        String answer = "";


        // El protocolo establece que se si no se recibe un OK al mensaje, se intentará mandar
        // de nuevo al servidor hasta un máximo de tres veces.
        while (!answer.equals("OK") && intentos <3) {
            try {
                intentos += 1;

                URL url = new URL("https", "mdsm1.ugr.es", this.port, "/");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setSSLSocketFactory(CustomSSLSocketFactory.getSSLSocketFactory(c));

                out = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                Log.d(TAG, "Sending data to server: " + data);
                writer.write(data);
                writer.flush();
                writer.close();
                out.close();

                answer = readInputStreamToString(connection);

                Log.d(TAG, "Recibido: " + answer);
                connection.disconnect();
            } catch (Exception e) {
                Log.d(TAG, "Capturada excepcion: " + e.toString());
            }
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
