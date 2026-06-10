package Sinisa.Stevanovic.eventsApp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExclusiveEventService extends Service {

    private boolean isRunning = false;
    private Thread serviceThread;

    //Za sad okidam na 2 minute umesto na 24h za testiranje
    private static final long INTERVAL = 2 * 60 * 1000;
    //5m=5*60*1000ms
    private static final long EXCLUSIVE_WINDOW = 2 * 60 * 1000;
    private static final String CHANNEL_ID = "ExclusiveEventChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            serviceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            //pauza od 10s da se aplikacija podigne
                            Thread.sleep(10000);
                            //kreiranje iventa
                            createExclusiveEvent();

                            //pauza do sledeceg kreiranja
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            isRunning = false;
                        }
                    }
                }
            });
            serviceThread.start();
        }
        return START_STICKY; //Sistem ga pokrece automatski
    }

    private void createExclusiveEvent() {
        // Trenutno vreme
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = currentTimeMillis + EXCLUSIVE_WINDOW;

        //Stavljamo da je zurka za 2 dana
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String eventTime = sdf.format(new Date(currentTimeMillis + (48 * 60 * 60 * 1000)));
        String expirationTimeStr = String.valueOf(expirationTimeMillis);

        //Generisanje random imena
        String eventName = getString(R.string.pecanje) + " " + (currentTimeMillis % 100);

        try {
            URL url = new URL("http://192.168.0.14:3000/events");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", eventName);
            jsonBody.put("description", getString(R.string.ovo_je_ekskluziva));
            jsonBody.put("location", getString(R.string.hidden_location));
            jsonBody.put("eventTime", eventTime);
            jsonBody.put("category", "PARTY");
            jsonBody.put("promoted", true);
            jsonBody.put("capacity", 50);
            jsonBody.put("isExclusive", true);
            jsonBody.put("expirationTime", expirationTimeStr);

            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == 201) {

                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    sb.append(responseLine);
                }
                br.close();

                JSONObject kreiraniEvent = new JSONObject(sb.toString());
                String serverId = kreiraniEvent.getString("_id");

                DBHelper dbHelper = new DBHelper(ExclusiveEventService.this);
                dbHelper.addEvent(
                        serverId,
                        eventName,
                        getString(R.string.ovo_je_ekskluziva),
                        getString(R.string.hidden_location),
                        eventTime,
                        "PARTY",
                        R.drawable.default_picture,
                        1,
                        50,
                        true,
                        expirationTimeStr
                );

                SharedPreferences sp = getSharedPreferences("ExclusiveEventsPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putLong(eventName, expirationTimeMillis);
                editor.apply();

                sendNotification(eventName);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(EXCLUSIVE_WINDOW);
                            sendExpirationNotification(eventName);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String eventName) {
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notifikacija okida EventDetailsActivity i prosledjuje mu naziv dogadjaja
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_NAME", eventName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.default_picture)
                .setContentTitle(getString(R.string.new_exclusive_event) + " " + eventName)
                .setContentText(getString(R.string.pet_minuta))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        mgr.notify((int) System.currentTimeMillis(), builder.build());
    }
    private void sendExpirationNotification(String eventName) {
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.default_picture)
                .setContentTitle(getString(R.string.notification_closed_title))
                .setContentText(String.format(getString(R.string.notification_closed_text), eventName))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

         mgr.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, getString(R.string.channel_exclusive_name), NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Ovo je pokrenuti (Unbounded) Servis
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (serviceThread != null) {
            serviceThread.interrupt();
        }
        super.onDestroy();
    }
}