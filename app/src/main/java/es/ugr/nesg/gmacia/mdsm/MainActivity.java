package es.ugr.nesg.gmacia.mdsm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import java.util.*;
import java.util.List;


public class MainActivity extends Activity {

    EditText input;
    Button btn, btn_getApps, btn_ip;
    TextView out,deviceid;
    String command;
    MDSMUtils mdsmUtils;
    String movilID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = findViewById(R.id.txt);
        btn = findViewById(R.id.btn);
        btn_getApps = findViewById(R.id.btn_info);
        btn_ip = findViewById(R.id.btn_ip);
        out = findViewById(R.id.out);
        deviceid = findViewById(R.id.deviceid);
        mdsmUtils = new MDSMUtils(getApplicationContext());

        // Si el UUID que identifica al móvil no está generado lo genera y lo guarda en las preferencias.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("es.ugr.nesg.gmacia.mdsm.PREFERENCES_FILE", Context.MODE_PRIVATE);

        if (!sharedPref.contains("UUID")) {
            movilID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("UUID", movilID);
            editor.commit();
            Log.d ("Preferences", "Guardado UUID en fichero de preferencias: " + movilID);
        } else {
            movilID = sharedPref.getString("UUID", "");
        }
        deviceid.setText("Device ID: " + movilID);



        // BOTON SHELL
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                // Primero escondo el keyboard para que se vea el resultado
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                // Ahora ejecuto el comando en una shell
                ShellExecuter exe = new ShellExecuter();
                command = input.getText().toString();
                String outp = exe.Executer(command);
                out.setText(outp);
                Log.d("Output: ", outp);
                mdsmUtils.sendDataToServer(outp, mdsmUtils.ACCESS_CONTROL_MESSAGE);
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
                mdsmUtils.sendDataToServer (answer, mdsmUtils.ACCESS_CONTROL_MESSAGE);
            }
        });


        // BOTON IP
        btn_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String ipString = mdsmUtils.getLocalIpAddress();
                String ssid = mdsmUtils.getSsid();
                out.setText(ipString + ":" + ssid);
                mdsmUtils.sendDataToServer(ipString + ":" + ssid, mdsmUtils.ACCESS_CONTROL_MESSAGE);
            }
        });
    }
}
