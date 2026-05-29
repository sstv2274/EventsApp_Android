package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private int currentUserId;

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

        lvInterestedEvents.setEmptyView(tvEmptyInterested);

        osveziListu();

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
        List<Event> interestedEvents = dbHelper.getEventsForUserByStatus(currentUserId, "ZAINTERESOVAN");
        eventAdapter = new EventAdapter(this, interestedEvents);
        lvInterestedEvents.setAdapter(eventAdapter);
    }
}