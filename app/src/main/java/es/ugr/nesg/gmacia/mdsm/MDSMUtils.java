package es.ugr.nesg.gmacia.mdsm;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gmacia on 18/04/2018.
 */

public class MDSMUtils {
    private Context context;
    private final static String TAG = MDSMUtils.class.getSimpleName();

    public MDSMUtils(Context context) {
        this.context = context;
    }

    // Una función de test para la clase
    public void test() {
        Log.d (TAG,"prueba de ejecución");

    }

    // getLocalIpAddress: Devuelve la lista de direcciones IP del móvil, que no tiene por qué ser una sola
    // ya que a veces se obtiene la IP por DHCP pero hay un portal cautivo y se mantiene la
    // conexión en datos sin WIFI.

    public String getLocalIpAddress(){
        String resultado = "[";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                //Log.d("MDSMUTILS", "Interfaz: " + intf.toString());
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    String ip = inetAddress.getHostAddress();
                    if (!inetAddress.isLoopbackAddress() && isIpv4Address(ip)) {
                        resultado += ip + "/";

                        //Log.d("MDSMUtils", valida + inetAddress.getHostAddress());
                        //return inetAddress.getHostAddress();
                    }
                }
            }

            // Formateamos para que no aparezca la coma final en la lista de direcciones IP.
            if (!resultado.equals("[")) {
                resultado = resultado.substring(0,resultado.length()-1);
            }
            return resultado + "]";

        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    public String getWifiDhcpAddress () {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        Integer ip = wifiManager.getDhcpInfo().ipAddress;
        String ipString = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
        return ipString;
    }

    public String getSsid () {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid = info.getSSID();
        return ssid;
    }

    public void sendDataToServer (String data) {
        /* A partir de la versión 3.0, el envío de mensajes por red debe hacerse en una hebra separada
           Por este motivo creo la clase con la interfaz runnable, que permite enviar los datos al servidor MDSM.
         */

        // Send with SSL plain socket
        // new Thread(new MDSMServerSslSocketConnection(data, context)).start();

        // Send with plain socket
        // new Thread(new MDSMServerPlainSocketConnection(data, context)).start();

        // Send with HTTPS
        new Thread(new MDSMServerHttpsConnection(data, context)).start();

    }


    private boolean isIpv4Address(String ipAddress) {
        final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
        Pattern VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);

        Matcher m1 = VALID_IPV4_PATTERN.matcher(ipAddress);
        if (m1.matches()) {
            return true;
        }
        return false;
    }


}
