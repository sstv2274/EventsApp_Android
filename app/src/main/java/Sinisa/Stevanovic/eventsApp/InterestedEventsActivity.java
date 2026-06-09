package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InterestedEventsActivity extends AppCompatActivity {

    private ListView lvInterestedEvents;
    private TextView tvEmptyInterested;
    private EventAdapter eventAdapter;

    private DBHelper dbHelper;
    private int currentUserId;

    // Serverski string ID za slanje parametra rute
    private String serverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_events);

        lvInterestedEvents = findViewById(R.id.lvInterestedEvents);
        tvEmptyInterested = findViewById(R.id.tvEmptyInterested);

        dbHelper = new DBHelper(this);

        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String loggedInUser = sp.getString("LOGGED_IN_USER", "");

        currentUserId = dbHelper.getUserIdByUsername(loggedInUser);

        // Preuzimanje MongoDB string ID-ja za prijavljenog korisnika
        serverUserId = dbHelper.getServerUserIdByUsername(loggedInUser);

        lvInterestedEvents.setEmptyView(tvEmptyInterested);

        // Otvaranje detalja dogadjaja
        lvInterestedEvents.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = (Event) eventAdapter.getItem(position);
            Intent intent = new Intent(InterestedEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("EVENT_NAME", selectedEvent.getName());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        osveziListu();
    }

    private void osveziListu() {
        if (serverUserId == null) {
            Toast.makeText(this, R.string.no_user_id, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://192.168.0.14:3000/attendance/" + serverUserId);
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

                        JSONArray jsonArray = new JSONArray(sb.toString());
                        List<Event> interestedEvents = new ArrayList<>();
                        List<Event> promotedEvents = new ArrayList<>();
                        List<Event> regularEvents = new ArrayList<>();

                        SQLiteDatabase dbReadable = dbHelper.getReadableDatabase();

                        // Prolazak kroz sve zapise i filtriranje prema commitment vrednosti
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject attendanceObj = jsonArray.getJSONObject(i);
                            String commitment = attendanceObj.getString("commitment");

                            // Na osnovu commitment vrednosti izdvajamo samo "ZAINTERESOVAN" događaje
                            if ("ZAINTERESOVAN".equals(commitment)) {
                                JSONObject eventObj = null;

                                if (attendanceObj.get("eventId") instanceof JSONObject) {
                                    // Ako je server poslao ceo objekat, pročitaj ga normalno
                                    eventObj = attendanceObj.getJSONObject("eventId");
                                } else if (attendanceObj.get("eventId") instanceof String) {
                                    // Ako je server poslao samo string ID, izvuci podatke iz lokalnog SQLite-a preko zajedničke konekcije
                                    String eventServerId = attendanceObj.getString("eventId");
                                    Cursor cursor = dbReadable.query("events", null, "server_id = ?", new String[]{eventServerId}, null, null, null);

                                    if (cursor != null && cursor.moveToFirst()) {
                                        try {
                                            eventObj = new JSONObject();
                                            eventObj.put("naziv", cursor.getString(cursor.getColumnIndexOrThrow("naziv")));
                                            eventObj.put("opis", cursor.getString(cursor.getColumnIndexOrThrow("opis")));
                                            eventObj.put("lokacija", cursor.getString(cursor.getColumnIndexOrThrow("lokacija")));
                                            eventObj.put("datumVreme", cursor.getString(cursor.getColumnIndexOrThrow("datumVreme")));
                                            eventObj.put("kategorija", cursor.getString(cursor.getColumnIndexOrThrow("kategorija")));
                                            eventObj.put("promoted", cursor.getInt(cursor.getColumnIndexOrThrow("promoted")));
                                            eventObj.put("kapacitet", cursor.getInt(cursor.getColumnIndexOrThrow("kapacitet")));
                                            eventObj.put("brojPrisutnih", cursor.getInt(cursor.getColumnIndexOrThrow("brojPrisutnih")));
                                            eventObj.put("prosecnaOcena", cursor.getDouble(cursor.getColumnIndexOrThrow("prosecnaOcena")));
                                            eventObj.put("brojOcena", cursor.getInt(cursor.getColumnIndexOrThrow("brojOcena")));
                                        } catch (org.json.JSONException jsonEx) {
                                            jsonEx.printStackTrace();
                                        } finally {
                                            cursor.close();
                                        }
                                    }
                                }

                                if (eventObj != null) {
                                    String name = eventObj.has("naziv") ? eventObj.getString("naziv") : eventObj.getString("name");
                                    String desc = eventObj.has("opis") ? eventObj.optString("opis", "") : eventObj.optString("description", "");
                                    String location = eventObj.has("lokacija") ? eventObj.getString("lokacija") : eventObj.getString("location");
                                    String dateTime = eventObj.has("datumVreme") ? eventObj.getString("datumVreme") : eventObj.optString("eventTime", "");
                                    String category = eventObj.has("kategorija") ? eventObj.getString("kategorija") : eventObj.getString("category");

                                    boolean isPromoted = false;
                                    if (eventObj.has("promoted")) {
                                        Object p = eventObj.get("promoted");
                                        if (p instanceof Boolean) isPromoted = (Boolean) p;
                                        else if (p instanceof Number) isPromoted = ((Number) p).intValue() == 1;
                                    }

                                    int capacity = eventObj.has("kapacitet") ? eventObj.optInt("kapacitet", 0) : eventObj.optInt("capacity", 0);
                                    int attendingCount = eventObj.has("brojPrisutnih") ? eventObj.optInt("brojPrisutnih", 0) : eventObj.optInt("numberOfAttendees", 0);
                                    double averageRating = eventObj.has("prosecnaOcena") ? eventObj.optDouble("prosecnaOcena", 0.0) : eventObj.optDouble("averageRating", 0.0);
                                    int ratingCount = eventObj.has("brojOcena") ? eventObj.optInt("brojOcena", 0) : eventObj.optInt("ratingCount", 0);

                                    int imgResId = R.drawable.default_picture;
                                    Cursor imgCursor = dbReadable.query("events", new String[]{"imageResId"}, "naziv = ?", new String[]{name}, null, null, null);
                                    if (imgCursor != null) {
                                        if (imgCursor.moveToFirst()) {
                                            imgResId = imgCursor.getInt(imgCursor.getColumnIndexOrThrow("imageResId"));
                                        }
                                        imgCursor.close();
                                    }

                                    Event event;
                                    if (isPromoted) {
                                        event = new Event(name, desc, location, dateTime, category, imgResId, true, capacity, attendingCount, averageRating, ratingCount,false,"");//menjano
                                        promotedEvents.add(event);
                                    } else {
                                        event = new Event(name, desc, location, dateTime, category, imgResId, attendingCount, averageRating, ratingCount,false,"");//Menjano
                                        regularEvents.add(event);
                                    }
                                }
                            }
                        }

                        dbReadable.close();

                        interestedEvents.addAll(promotedEvents);
                        interestedEvents.addAll(regularEvents);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                eventAdapter = new EventAdapter(InterestedEventsActivity.this, interestedEvents);
                                lvInterestedEvents.setAdapter(eventAdapter);
                            }
                        });
                    } else {
                        prikaziMreznuGresku();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    prikaziMreznuGresku();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void prikaziMreznuGresku() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(InterestedEventsActivity.this, R.string.server_conn_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}