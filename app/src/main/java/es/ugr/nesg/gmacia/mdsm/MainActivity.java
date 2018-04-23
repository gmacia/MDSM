package es.ugr.nesg.gmacia.mdsm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;

import java.util.*;
import java.util.List;


public class MainActivity extends Activity {

    EditText input;
    Button btn, btn_getApps, btn_ip;
    TextView out;
    String command;

    MDSMUtils mdsmUtils;


    private void sendDataToServer (String data) {
        /* A partir de la versión 3.0, el envío de mensajes por red debe hacerse en una hebra separada
           Por este motivo creo la clase con la interfaz runnable, que permite enviar los datos al servidor MDSM.
         */
        new Thread(new MDSMServerSslSocketConnection(data, MainActivity.this)).start();
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

        mdsmUtils = new MDSMUtils(getApplicationContext());

        // Si el UUID que identifica al móvil no está generado lo genera y lo guarda en las preferencias.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("es.ugr.nesg.gmacia.mdsm.PREFERENCES_FILE", Context.MODE_PRIVATE);
        if (!sharedPref.contains("UUID")) {
            String movilID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("UUID", movilID);
            editor.commit();
            Log.d ("Preferences", "Guardado UUID en fichero de preferencias: " + movilID);
        }

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
                mdsmUtils.sendDataToServer(outp);
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
                mdsmUtils.sendDataToServer (answer);

            }
        });


        // BOTON IP
        btn_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //mdsmUtils.test();
                //Log.d("PRUEBA!!", "IP Local: " + mdsmUtils.getLocalIpAddress());
                //Log.d("PRUEBA!!", "IP DHCP: " + mdsmUtils.getWifiDhcpAddress());
                //Log.d("PRUEBA!!", "SSID: " + mdsmUtils.getSsid());


                String ipString = mdsmUtils.getLocalIpAddress();
                String ssid = mdsmUtils.getSsid();
                mdsmUtils.sendDataToServer(ipString + ":" + ssid);

/*
                String TAG = "Boton IP";
                String ssid = "";
                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                Integer ip = wm.getDhcpInfo().ipAddress;
                String ipString = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

                WifiInfo wifiInfo = wm.getConnectionInfo();
                Log.d(TAG, "Ip Addr: " + ipString);
                Log.d(TAG, "SSID: " + ssid);
                Log.d(TAG, wifiInfo.toString());
                out.setText(ipString);
                sendDataToServer (ipString + "," + wifiInfo.getSSID());
*/

            }
        });
    }
}
