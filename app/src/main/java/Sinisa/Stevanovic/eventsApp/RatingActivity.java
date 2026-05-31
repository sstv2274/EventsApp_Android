package Sinisa.Stevanovic.eventsApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RatingActivity extends AppCompatActivity {

    private TextView tvRatingEventName;
    private ImageView[] stars = new ImageView[5];
    private Button btnConfirmRating;
    private int selectedRating = 0;

    private DBHelper dbHelper;
    private int currentUserId;
    private String eventName;

    private String serverUserId;
    private String serverEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        tvRatingEventName = findViewById(R.id.tvRatingEventName);
        btnConfirmRating = findViewById(R.id.btnConfirmRating);

        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        dbHelper = new DBHelper(this);

        // Kupim id korisnika
        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String loggedInUser = sp.getString("LOGGED_IN_USER", "");
        currentUserId = dbHelper.getUserIdByUsername(loggedInUser);

        serverUserId = dbHelper.getServerUserIdByUsername(loggedInUser);

        // Preuzimanje imena dogadjaja preko intenta
        eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName != null) {
            tvRatingEventName.setText(eventName);
            serverEventId = dbHelper.getServerEventIdByName(eventName);

            // Proveravam da li postoji ocena i ako postoji palim zvezdice(kako bi korisnik znao da je vec ocenio)
            int postojecaOcena = dbHelper.getUserRatingForEvent(currentUserId, eventName);
            if (postojecaOcena > 0) {
                updateStarUI(postojecaOcena);
            }

            preuzmiOcenuSaServera();
        }

        // Postavljanje klika na zvezdice
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i;
            stars[i].setOnClickListener(v -> updateStarUI(starIndex + 1));
        }

        // Potvrda ocenjivanja
        btnConfirmRating.setOnClickListener(v -> {
            if (selectedRating == 0) {
                Toast.makeText(this, R.string.toast_select_rating, Toast.LENGTH_SHORT).show();
            } else {
                posaljiOcenuNaServer(selectedRating);
            }
        });
    }

    private void updateStarUI(int rating) {
        selectedRating = rating;

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.star_filled);
            } else {
                stars[i].setImageResource(R.drawable.star_empty);
            }
        }
    }

    private void preuzmiOcenuSaServera() {
        if (serverUserId == null || serverEventId == null) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://192.168.0.16:3000/ratings/" + serverUserId + "/" + serverEventId);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();

                        String responseStr = sb.toString().trim();
                        int ocenaSaServera = -1;

                        if (responseStr.startsWith("[")) {
                            JSONArray arr = new JSONArray(responseStr);
                            if (arr.length() > 0) {
                                JSONObject obj = arr.getJSONObject(0);
                                if (obj.has("rating")) ocenaSaServera = obj.getInt("rating");
                            }
                        } else if (responseStr.startsWith("{")) {
                            JSONObject obj = new JSONObject(responseStr);
                            if (obj.has("rating")) ocenaSaServera = obj.getInt("rating");
                        }

                        if (ocenaSaServera > 0) {
                            final int finalOcena = ocenaSaServera;

                            dbHelper.handleRating(currentUserId, eventName, finalOcena);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateStarUI(finalOcena);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void posaljiOcenuNaServer(final int ratingValue){
        if(serverUserId == null || serverEventId == null){
            Toast.makeText(this, R.string.sync_unsuccessful, Toast.LENGTH_LONG).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try{
                    URL url = new URL("http://192.168.0.16:3000/ratings");
                    urlConnection = (HttpURLConnection) url.openConnection();


                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type","application/json");
                    urlConnection.setDoOutput(true);

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("userId", serverUserId);
                    jsonBody.put("eventId", serverEventId);
                    jsonBody.put("rating", ratingValue);

                    OutputStream os = urlConnection.getOutputStream();
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = urlConnection.getResponseCode();

                    if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == 201){
                        final boolean uspeh = dbHelper.handleRating(currentUserId, eventName, ratingValue);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (uspeh) {
                                    Toast.makeText(RatingActivity.this, R.string.toast_rating_saved, Toast.LENGTH_SHORT).show();
                                    finish(); // Povratak na prethodni ekran
                                } else {
                                    Toast.makeText(RatingActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else if(responseCode == HttpURLConnection.HTTP_CONFLICT){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(RatingActivity.this, R.string.already_rated, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RatingActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RatingActivity.this, R.string.server_conn_error, Toast.LENGTH_SHORT).show();
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
}