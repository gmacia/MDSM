package es.ugr.nesg.gmacia.shell;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

/**
 * Created by gmacia on 13/02/2018.
 */

public class InfoCollector {

    private static final String TAG = "InfoCollector";
    private Context context;

    public InfoCollector (Context context){
        this.context = context;
    }
    public String Collect () {
        /* Función que recopila información en el teléfono utilizando las APIs*/
        String resultado = CollectInstalledApplications();
        return resultado;

    }
    public String CollectInstalledApplications () {
        String resultado = "Prueba";
        PackageManager pm = this.context.getPackageManager();
//get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            Log.d(TAG, "Installed package :" + packageInfo.packageName);
            Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
            Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
        }
        return resultado;

    }


}
