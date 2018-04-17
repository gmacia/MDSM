package es.ugr.nesg.gmacia.shell;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

        // Genera el Identificador del móvil
        movilID = UUID.randomUUID().toString();

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
                    Log.d("InfoCollector", message);

                    //Log.d("InfoCollector", "Source dir : " + packageInfo.sourceDir);
                    //Log.d("InfoCollector", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
                }
                out.setText(answer);
                sendDataToServer (answer);
                //new Thread(new MDSM_SslServerConnection(answer, MainActivity.this)).start();

            }
        });


        // BOTON IP
        btn_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String TAG = "Boton IP";
                WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                Integer ip = wm.getDhcpInfo().ipAddress;
                String ipString = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
                Log.d(TAG, "Ip Addr: " + ipString);
                out.setText(ipString);
                //new Thread(new MDSMmServerConnection("IP del movil: " + ipString)).start();
                //new Thread(new MDSM_SslServerConnection("IP del movil: " + ipString, MainActivity.this)).start();
                sendDataToServer(movilID + "," + ipString);
            }
        });
    }
}
