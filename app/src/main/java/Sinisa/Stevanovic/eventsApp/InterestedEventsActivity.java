package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class InterestedEventsActivity extends AppCompatActivity {

    private ListView lvInterestedEvents;
    private TextView tvEmptyInterested;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_events);

        lvInterestedEvents = findViewById(R.id.lvInterestedEvents);
        tvEmptyInterested = findViewById(R.id.tvEmptyInterested);


        lvInterestedEvents.setEmptyView(tvEmptyInterested);

        // Prosledjujemo listu u postojeci adapter
        eventAdapter = new EventAdapter(this, AppData.interestedEvents);
        lvInterestedEvents.setAdapter(eventAdapter);

        // Otvaramo detalje liste kada stisnemo na Listner i prosledjujemo preko intenta podatak o imenu(da bi znali o kom aktivitiju detalje da prikazemo)
        lvInterestedEvents.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = (Event) eventAdapter.getItem(position);
            Intent intent = new Intent(InterestedEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("EVENT_NAME", selectedEvent.getName());
            startActivity(intent);
        });
    }

    //Dodao sam ovo kako bi pratio ako dodam jos neki event u IntrestedEvents a ne da zamrzne ekran i ceka
    //da se vratim
    @Override
    protected void onResume() {
        super.onResume();
        if (eventAdapter != null) {
            eventAdapter.setEvents(AppData.interestedEvents);
        }
    }
}