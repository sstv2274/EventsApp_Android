package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {

    private ImageView ivDetailsImage;
    private TextView tvDetailsName, tvDetailsCategory, tvDetailsLocation, tvDetailsDateTime;
    private TextView tvDetailsDescription, tvDetailsFreeSpots, tvDetailsRating;
    private Button btnInterested, btnAttending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

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

        //Preuzimanje event-a na koji smo kliknuli
        String eventName = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventName = extras.getString("EVENT_NAME", "");
        }

        //Pronalazak event-a po imenu
        Event event = AppData.findByName(eventName);

        if (event != null) {
            //Kupim podatke
            ivDetailsImage.setImageResource(event.getImageResId());
            tvDetailsName.setText(event.getName());
            tvDetailsCategory.setText(event.getCategory());
            tvDetailsLocation.setText(event.getLocation());
            tvDetailsDateTime.setText(event.getDateTime());
            tvDetailsDescription.setText(event.getDescription());

            //Proveravam da li je u promoted
            if (event.isPromoted()) {
                tvDetailsFreeSpots.setVisibility(View.VISIBLE);
                int slobodnaMesta = event.getCapacity() - event.getAttendingCount();
                String freeSpotsText = getString(R.string.free_spots_format, slobodnaMesta, event.getCapacity());
                tvDetailsFreeSpots.setText(freeSpotsText);
            }

            //Ispis rejtina. Ako je nula ispisujemo Jos uvek nema ocena
            if (event.getRatingCount() == 0) {
                tvDetailsRating.setText(getString(R.string.no_rating));
            } else {
                String ratingText = getString(R.string.rating_format, event.getAverageRating(), event.getRatingCount());
                tvDetailsRating.setText(ratingText);
            }
        }
        //Dodavanje u InrestedEvents
        btnInterested.setOnClickListener(v -> {
            //Zastita od duplikata
            if (!AppData.interestedEvents.contains(event)) {
                AppData.interestedEvents.add(event);
            }
            Toast.makeText(this, R.string.toast_added_interested, Toast.LENGTH_SHORT).show();
        });
        //Dodavanje u AttendingEvents
        btnAttending.setOnClickListener(v -> {
            //Zastita od duplikata
            if (!AppData.attendingEvents.contains(event)) {
                AppData.attendingEvents.add(event);
            }
            Toast.makeText(this, R.string.toast_added_attending, Toast.LENGTH_SHORT).show();
        });
    }
}