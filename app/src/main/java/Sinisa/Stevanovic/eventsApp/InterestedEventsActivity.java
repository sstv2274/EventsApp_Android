package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class InterestedEventsActivity extends AppCompatActivity {

    private ListView lvInterestedEvents;
    private TextView tvEmptyInterested;
    private EventAdapter eventAdapter;

    private DBHelper dbHelper;
    private int currentUserId = 1; //!!//U sledecem koraku promeniti//!!//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_events);

        lvInterestedEvents = findViewById(R.id.lvInterestedEvents);
        tvEmptyInterested = findViewById(R.id.tvEmptyInterested);

        dbHelper = new DBHelper(this);

        lvInterestedEvents.setEmptyView(tvEmptyInterested);

        // Ucitavanje iz baze podataka
        List<Event> interestedEvents = dbHelper.getEventsForUserByStatus(currentUserId, "ZAINTERESOVAN");
        eventAdapter = new EventAdapter(this, interestedEvents);
        lvInterestedEvents.setAdapter(eventAdapter);

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
        if (eventAdapter != null) {
            //Sada vucem sveze podatke iz baze umesto iz appdata.
            List<Event> updatedEvents = dbHelper.getEventsForUserByStatus(currentUserId, "ZAINTERESOVAN");
            eventAdapter.setEvents(updatedEvents);
        }
    }
}