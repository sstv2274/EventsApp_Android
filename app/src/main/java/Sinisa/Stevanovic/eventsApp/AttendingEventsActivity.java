package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AttendingEventsActivity extends AppCompatActivity {

    private ListView lvAttendingEvents;
    private TextView tvEmptyAttending;

    private DBHelper dbHelper;
    private int currentUserId = 1; //!!//Ispraviti u sledecem koraku//!!//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attending_events);

        lvAttendingEvents = findViewById(R.id.lvAttendingEvents);
        tvEmptyAttending = findViewById(R.id.tvEmptyAttending);

        dbHelper = new DBHelper(this);

        // Prikazujem poruku ako je lista prazna
        lvAttendingEvents.setEmptyView(tvEmptyAttending);

        // Prvo punjenje liste pri pokretanju ekrana
        osveziListu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        osveziListu();
    }

    // Da ne pisem 2 puta isti ko, pomocna metoda za osvezavanje liste,bukvalno je isto
    private void osveziListu() {
        //Citamo iz baze sve sa statusom prisustvuje
        List<Event> attendingEvents = dbHelper.getEventsForUserByStatus(currentUserId, "PRISUSTVUJE");

        List<Event> upcomingEvents = new ArrayList<>();
        List<Event> pastEvents = new ArrayList<>();

        //Razvrstavam po datumima
        for (Event event : attendingEvents) {
            if (event.isPast()) {
                pastEvents.add(event);
            } else {
                upcomingEvents.add(event);
            }
        }

        List<Object> combinedList = new ArrayList<>();

        // Dodavanje zaglavlja i objekata u jedinstvenu listu
        if (!upcomingEvents.isEmpty()) {
            combinedList.add(getString(R.string.header_upcoming));
            combinedList.addAll(upcomingEvents);
        }

        if (!pastEvents.isEmpty()) {
            combinedList.add(getString(R.string.header_past));
            combinedList.addAll(pastEvents);
        }

        // osvezavanje adaptera sa novom listom
        AttendingAdapter adapter = new AttendingAdapter(this, combinedList);
        lvAttendingEvents.setAdapter(adapter);
    }
}