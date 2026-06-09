package Sinisa.Stevanovic.eventsApp;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AttendingEventsActivity extends AppCompatActivity {

    private ListView lvAttendingEvents;
    private TextView tvEmptyAttending;

    private DBHelper dbHelper;
    private int currentUserId;//ID korisnika
    private String serverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attending_events);

        lvAttendingEvents = findViewById(R.id.lvAttendingEvents);
        tvEmptyAttending = findViewById(R.id.tvEmptyAttending);

        dbHelper = new DBHelper(this);

        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String loggedInUser = sp.getString("LOGGED_IN_USER", "");
        currentUserId = dbHelper.getUserIdByUsername(loggedInUser);
        serverUserId = dbHelper.getServerUserIdByUsername(loggedInUser);

        // Prikazujem poruku ako je lista prazna
        lvAttendingEvents.setEmptyView(tvEmptyAttending);

    }

    @Override
    protected void onResume() {
        super.onResume();
        osveziListu();
    }

    // Da ne pisem 2 puta isti ko, pomocna metoda za osvezavanje liste,bukvalno je isto
    private void osveziListu() {
        if (serverUserId == null) {
            Toast.makeText(this, R.string.no_user, Toast.LENGTH_SHORT).show();
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
                        List<Event> upcomingEvents = new ArrayList<>();
                        List<Event> pastEvents = new ArrayList<>();

                        SQLiteDatabase dbReadable = dbHelper.getReadableDatabase();

                        // Prolazak kroz sve dobijene zapise sa servera
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject attendanceObj = jsonArray.getJSONObject(i);
                            String commitment = attendanceObj.getString("commitment");

                            if ("PRISUSTVUJE".equals(commitment)) {
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
                                    } else {
                                        event = new Event(name, desc, location, dateTime, category, imgResId, attendingCount, averageRating, ratingCount,false,"");//menjano
                                    }

                                    if (event.isPast()) {
                                        pastEvents.add(event);
                                    } else {
                                        upcomingEvents.add(event);
                                    }
                                }
                            }
                        }

                        dbReadable.close();

                        List<Object> combinedList = new ArrayList<>();

                        if (!upcomingEvents.isEmpty()) {
                            combinedList.add(getString(R.string.header_upcoming));
                            combinedList.addAll(upcomingEvents);
                        }

                        if (!pastEvents.isEmpty()) {
                            combinedList.add(getString(R.string.header_past));
                            combinedList.addAll(pastEvents);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AttendingAdapter adapter = new AttendingAdapter(AttendingEventsActivity.this, combinedList);
                                lvAttendingEvents.setAdapter(adapter);
                            }
                        });
                    } else {
                        PrikazGreske("Server je vratio HTTP kod: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    PrikazGreske(e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void PrikazGreske(final String detalji) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AttendingEventsActivity.this, detalji, Toast.LENGTH_LONG).show();
            }
        });
    }
}