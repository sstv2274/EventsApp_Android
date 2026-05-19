package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import android.content.Context;
import android.content.SharedPreferences;
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

    private DBHelper dbHelper;
    private int currentUserId;//Svaki user ima svoj ID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        dbHelper = new DBHelper(this);

        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String loggedInUser = sp.getString("LOGGED_IN_USER", "");

        currentUserId = dbHelper.getUserIdByUsername(loggedInUser);

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
        String eventName = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventName = extras.getString("EVENT_NAME", "");
        }

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
        }

        final String finalEventName = eventName;

        // Klik na dugme INTERESTED
        btnInterested.setOnClickListener(v -> {
            boolean uspeh = dbHelper.handleAttendance(currentUserId, finalEventName, "ZAINTERESOVAN");
            if (uspeh) {
                Toast.makeText(this, R.string.toast_added_interested, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.input_error, Toast.LENGTH_SHORT).show();
            }
        });

        // Klik na dugme ATTENDING
        btnAttending.setOnClickListener(v -> {
            boolean uspeh = dbHelper.handleAttendance(currentUserId, finalEventName, "PRISUSTVUJE");
            if (uspeh) {
                Toast.makeText(this, R.string.toast_added_attending, Toast.LENGTH_SHORT).show();

                // Posto se broj mesta promenio u bazi, ponovo ucitaj event i osvezi UI tekst
                Event azuriraniEvent = dbHelper.findEventByName(finalEventName);
                if (azuriraniEvent != null && azuriraniEvent.isPromoted()) {
                    osvezavanjeSlobodnihMesta(azuriraniEvent);
                }
            } else {
                // Ako je handleAttendance vratio false, znaci da nema mesta
                Toast.makeText(this, R.string.no_space, Toast.LENGTH_LONG).show();
            }
        });
    }

    //metoda za osvezavanje prikaza mesta da ne ponavljam isti kod
    private void osvezavanjeSlobodnihMesta(Event event) {
        int slobodnaMesta = event.getCapacity() - event.getAttendingCount();
        String freeSpotsText = getString(R.string.free_spots_format, slobodnaMesta, event.getCapacity());
        tvDetailsFreeSpots.setText(freeSpotsText);
    }
}