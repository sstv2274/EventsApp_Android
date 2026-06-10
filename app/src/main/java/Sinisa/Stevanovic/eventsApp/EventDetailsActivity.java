package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private ImageView ivDetailsImage;
    private TextView tvDetailsName, tvDetailsCategory, tvDetailsLocation, tvDetailsDateTime;
    private TextView tvDetailsDescription, tvDetailsFreeSpots, tvDetailsRating;
    private Button btnInterested, btnAttending;

    private DBHelper dbHelper;
    private int currentUserId;//Svaki user ima svoj ID
    private String serverUserId;
    private String serverEventId;
    private String eventName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        dbHelper = new DBHelper(this);

        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String loggedInUser = sp.getString("LOGGED_IN_USER", "");

        currentUserId = dbHelper.getUserIdByUsername(loggedInUser);

        SQLiteDatabase dbReadable = dbHelper.getReadableDatabase();
        Cursor userCursor = dbReadable.query("users", new String[]{"server_id"}, "username=?", new String[]{loggedInUser}, null, null, null);
        if (userCursor != null && userCursor.moveToFirst()) {
            serverUserId = userCursor.getString(userCursor.getColumnIndexOrThrow("server_id"));
            userCursor.close();
        }

        // Povezivanje elemenata iz xml-a
        ivDetailsImage = findViewById(R.id.ivDetailsImage);
        tvDetailsName = findViewById(R.id.tvDetailsName);
        tvDetailsCategory = findViewById(R.id.tvDetailsCategory);
        tvDetailsLocation = findViewById(R.id.tvDetailsLocation);
        tvDetailsDateTime = findViewById(R.id.tvDetailsDateTime);
        tvDetailsDescription = findViewById(R.id.tvDetailsDescription);
        tvDetailsFreeSpots = findViewById(R.id.tvDetailsFreeSpots);
        tvDetailsRating = findViewById(R.id.tvDetailsRating);
        btnInterested = findViewById(R.id.btnInterested);
        btnAttending = findViewById(R.id.btnAttending);

        //Preuzimanje imena event-a na koji smo kliknuli
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventName = extras.getString("EVENT_NAME", "");
        }

        Cursor eventCursor = dbReadable.query("events", new String[]{"server_id"}, "naziv=?", new String[]{eventName}, null, null, null);
        if (eventCursor != null && eventCursor.moveToFirst()) {
            serverEventId = eventCursor.getString(eventCursor.getColumnIndexOrThrow("server_id"));
            eventCursor.close();
        }
        dbReadable.close(); // Zatvaramo konekciju nakon što smo pokupili sve potrebne ID-jeve iz nje

        // Pronalazak iventa ali sada iz baze
        Event event = dbHelper.findEventByName(eventName);

        if (event != null) {
            // Kupim podatke
            ivDetailsImage.setImageResource(event.getImageResId());
            tvDetailsName.setText(event.getName());
            tvDetailsCategory.setText(event.getCategory());
            tvDetailsLocation.setText(event.getLocation());
            tvDetailsDateTime.setText(event.getDateTime());
            tvDetailsDescription.setText(event.getDescription());
            // Provera kapaciteta (Osvežava se iz baze!)
            if (event.isPromoted()) {
                tvDetailsFreeSpots.setVisibility(View.VISIBLE);
                osvezavanjeSlobodnihMesta(event);
            } else {
                tvDetailsFreeSpots.setVisibility(View.GONE);
            }
            // Ispis rejtinga
            if (event.getRatingCount() == 0) {
                tvDetailsRating.setText(getString(R.string.no_rating));
            } else {
                String ratingText = getString(R.string.rating_format, event.getAverageRating(), event.getRatingCount());
                tvDetailsRating.setText(ratingText);
            }


            SharedPreferences exclusiveSp = getSharedPreferences("ExclusiveEventsPrefs", Context.MODE_PRIVATE);
            long expirationTime = exclusiveSp.getLong(eventName, 0);


            if (expirationTime > 0 || event.isExclusive()) {


                if (expirationTime == 0 && event.getExpirationTime() != null && !event.getExpirationTime().isEmpty()) {
                    try {
                        expirationTime = Long.parseLong(event.getExpirationTime());
                    } catch (NumberFormatException e) {
                        expirationTime = 0;
                    }
                }

                long preostaloVreme = expirationTime - System.currentTimeMillis();

                if (preostaloVreme <= 0) {

                    btnAttending.setEnabled(false);
                    btnAttending.setText(getString(R.string.btn_closed_text));
                } else {

                    new android.os.CountDownTimer(preostaloVreme, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            long minuti = (millisUntilFinished / 1000) / 60;
                            long sekunde = (millisUntilFinished / 1000) % 60;

                            String uslovniTekst = getString(R.string.btn_attending_text) +
                                    String.format(Locale.getDefault(), " (%02d:%02d)", minuti, sekunde);
                            btnAttending.setText(uslovniTekst);
                        }

                        @Override
                        public void onFinish() {

                            btnAttending.setEnabled(false);
                            btnAttending.setText(getString(R.string.btn_closed_text));
                            Toast.makeText(EventDetailsActivity.this, R.string.toast_window_closed, Toast.LENGTH_LONG).show();
                        }
                    }.start();
                }
            }

        }
        // Klik na dugme INTERESTED
        btnInterested.setOnClickListener(v -> {
            posaljiPrisustvoNaServer("ZAINTERESOVAN");
        });

        // Klik na dugme ATTENDING
        btnAttending.setOnClickListener(v -> {
            posaljiPrisustvoNaServer("PRISUSTVUJE");
        });
    }

    private void posaljiPrisustvoNaServer(final String commitment) {
        if (serverUserId == null || serverEventId == null) {
            Toast.makeText(this, R.string.sync_unsuccessful, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://192.168.0.14:3000/attendance");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setDoOutput(true);

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("userId", serverUserId);
                    jsonBody.put("eventId", serverEventId);
                    jsonBody.put("commitment", commitment);

                    OutputStream os = urlConnection.getOutputStream();
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == 201) {
                        final boolean uspeh = dbHelper.handleAttendance(currentUserId, eventName, commitment);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (uspeh) {
                                    if (commitment.equals("ZAINTERESOVAN")) {
                                        Toast.makeText(EventDetailsActivity.this, R.string.toast_added_interested, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(EventDetailsActivity.this, R.string.toast_added_attending, Toast.LENGTH_SHORT).show();

                                        // Posto se broj mesta promenio u bazi, ponovo ucitaj event i osvezi UI tekst
                                        Event azuriraniEvent = dbHelper.findEventByName(eventName);
                                        if (azuriraniEvent != null && azuriraniEvent.isPromoted()) {
                                            osvezavanjeSlobodnihMesta(azuriraniEvent);
                                        }
                                    }
                                } else {
                                    Toast.makeText(EventDetailsActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EventDetailsActivity.this, R.string.no_space, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EventDetailsActivity.this, R.string.server_conn_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void osvezavanjeSlobodnihMesta(Event event) {
        int slobodnaMesta = event.getCapacity() - event.getAttendingCount();
        String freeSpotsText = getString(R.string.free_spots_format, slobodnaMesta, event.getCapacity());
        tvDetailsFreeSpots.setText(freeSpotsText);
    }
}