package es.ugr.nesg.gmacia.mdsm;

/**
 * Created by gmacia on 17/04/2018.
 */



import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;



/**
 * Receives wifi changes and creates a notification when wifi connects to a network,
 * displaying the SSID and MAC address.
 */
public class WifiReceiver extends BroadcastReceiver {

    private final static String TAG = WifiReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d (TAG, "OnReceive called with intent = [" + intent + "]");
        MDSMUtils mdsmUtils = new MDSMUtils(context);
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
        final String action = intent.getAction();

        // Estos eventos se reciben cuando el usuario activa/desactiva la wifi en el móvil
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            if (WifiManager.WIFI_STATE_ENABLED == wifiState) {
                String ip = mdsmUtils.getLocalIpAddress();
                Log.d(TAG, ip + ", Wifi enabled");
                mdsmUtils.sendDataToServer(ip + ", Wifi enabled", mdsmUtils.IP_MONITOR_MESSAGE);
            } else if (WifiManager.WIFI_STATE_DISABLED == wifiState) {
                String ip = mdsmUtils.getLocalIpAddress();
                Log.d(TAG, ip + ", Wifi disabled");
                mdsmUtils.sendDataToServer(ip+ ", Wifi disabled", mdsmUtils.IP_MONITOR_MESSAGE);
            }
        }

        // Estos eventos se reciben cuando el móvil cambia la red wifi a la que está conectado
        // sin apagar o encender la wifi
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {

            String message;
            Log.d("TAG", "Recibido NETWORK_STATE_CHANGED_ACTION");
            NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (NetworkInfo.State.CONNECTED.equals(nwInfo.getState())) {
                String ip = mdsmUtils.getLocalIpAddress();
                message = ip + ",WIFI network SSID connected";
                Log.d(TAG, message);
                context.startService(new Intent(context, WifiActiveService.class));

            } else if (NetworkInfo.State.DISCONNECTED.equals(nwInfo.getState())) {
                String ip = mdsmUtils.getLocalIpAddress();
                message = ip + ", WIFI network SSID disconnected";
                Log.d(TAG, message);
                mdsmUtils.sendDataToServer(message, mdsmUtils.IP_MONITOR_MESSAGE);
            }
        }
    }

    /**
     * Getting the network info and displaying the notification is handled in a service
     * as we need to delay fetching the SSID name. If this is done when the receiver is
     * called, the name isn't yet available and you'll get null.
     *
     * As the broadcast receiver is flagged for termination as soon as onReceive() completes,
     * there's a chance that it will be killed before the handler has had time to finish. Placing
     * it in a service lets us control the lifetime.
     */
    public static class WifiActiveService extends Service {

        private final static String TAG = WifiActiveService.class.getSimpleName();

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // Need to wait a bit for the SSID to get picked up;
            // if done immediately all we'll get is null
            final MDSMUtils mdsmUtils = new MDSMUtils(super.getApplicationContext());
            mdsmUtils.sendDataToServer("[],Detected connection to SSID. Fetching data...", mdsmUtils.IP_MONITOR_MESSAGE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String ipString = mdsmUtils.getLocalIpAddress();
                    String ssid = mdsmUtils.getSsid();
                    // Notify everywhere
                    createNotification(ssid);
                    String message = ipString + ", Wifi Connected to: [" + ssid + "]";
                    Log.d(TAG, message);
                    mdsmUtils.sendDataToServer(message, mdsmUtils.IP_MONITOR_MESSAGE);
                    stopSelf();
                }
            }, 10000);
            return START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        /**
         * Creates a notification displaying the SSID & MAC addr
         */
        private void createNotification(String ssid) {
            Notification n = new NotificationCompat.Builder(this)
                    .setContentTitle("MDSM Server notified")
                    .setContentText("Connected to " + ssid)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("You're connected to " + ssid))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(0, n);
        }
    }
}