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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attending_events);

        lvAttendingEvents = findViewById(R.id.lvAttendingEvents);
        tvEmptyAttending = findViewById(R.id.tvEmptyAttending);

        //Prikazujem poruku ako je lista prazna
        lvAttendingEvents.setEmptyView(tvEmptyAttending);

        //Pravim dve liste(nadolazeci i prosli iventovi)
        List<Event> upcomingEvents = new ArrayList<>();
        List<Event> pastEvents = new ArrayList<>();

        for (Event event : AppData.attendingEvents) {
            if (event.isPast()) {
                pastEvents.add(event);
            } else {
                upcomingEvents.add(event);
            }
        }

        //Ovde samo spajam te dve liste
        List<Object> combinedList = new ArrayList<>();
        //Jako jednostavno resenje za tekst. Dodacu i tekst u listu
        if (!upcomingEvents.isEmpty()) {
            combinedList.add(getString(R.string.header_upcoming)); // tekst(zaglavlje)
            combinedList.addAll(upcomingEvents); // iventovi
        }

        if (!pastEvents.isEmpty()) {
            combinedList.add(getString(R.string.header_past)); //tekst
            combinedList.addAll(pastEvents); //iventovi
        }

        //Samo spajam sa adapterom
        AttendingAdapter adapter = new AttendingAdapter(this, combinedList);
        lvAttendingEvents.setAdapter(adapter);
    }
}