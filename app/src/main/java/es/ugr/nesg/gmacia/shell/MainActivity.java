package es.ugr.nesg.gmacia.shell;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;

import java.math.BigInteger;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends Activity {

    EditText input;
    Button btn, btn_getApps, btn_ip;
    TextView out;
    String command;
    String movilID;

    private void sendDataToServer (String data) {
        /* A partir de la versión 3.0, el envío de mensajes por red debe hacerse en una hebra separada
           Por este motivo creo la clase con la interfaz runnable, que permite enviar los datos al servidor MDSM.
         */
        new Thread(new MDSM_SslServerConnection(data, MainActivity.this)).start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = findViewById(R.id.txt);
        btn = findViewById(R.id.btn);
        btn_getApps = findViewById(R.id.btn_info);
        btn_ip = findViewById(R.id.btn_ip);
        out = findViewById(R.id.out);

        Context context = getApplicationContext();


        // Genera o lee el Identificador del móvil

        SharedPreferences sharedPref = context.getSharedPreferences("es.ugr.nesg.gmacia.shell.PREFERENCES_FILE", Context.MODE_PRIVATE);
        if (sharedPref.contains("UUID")) {
            movilID = sharedPref.getString("UUID", "");
            Log.d ("Preferences", "Leidas preferences desde disco: " + movilID);
        } else {
            movilID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("UUID", movilID);
            editor.commit();
            Log.d ("Preferences", "Guardado UUID en fichero de preferencias: " + movilID);
        }

        //movilID = UUID.randomUUID().toString();

        // BOTON SHELL
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                ShellExecuter exe = new ShellExecuter();
                command = input.getText().toString();
                String outp = exe.Executer(command);
                out.setText(outp);
                Log.d("Output: ", outp);
                sendDataToServer(outp);
            }
        });

        // BOTON GETAPPS
        btn_getApps.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View arg0) {
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                String answer = "";
                for (ApplicationInfo packageInfo : packages) {
                    String message = "Installed package: " + packageInfo.packageName;
                    answer += message + "\n";
                    Log.d("getApps", message);
                }
                out.setText(answer);
                sendDataToServer (answer);

            }
        });


        // BOTON IP
        btn_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String TAG = "Boton IP";
                String ssid = "";
                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                Integer ip = wm.getDhcpInfo().ipAddress;
                String ipString = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

                WifiInfo wifiInfo = wm.getConnectionInfo();
                if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
                    ssid = wifiInfo.getSSID();
                }

                Log.d(TAG, "Ip Addr: " + ipString);
                Log.d(TAG, "BSSID: " + ssid);
                Log.d(TAG, wifiInfo.toString());
                out.setText(ipString);
                sendDataToServer(movilID + "," + ipString + "," + wifiInfo.toString());



            }
        });
    }
}
