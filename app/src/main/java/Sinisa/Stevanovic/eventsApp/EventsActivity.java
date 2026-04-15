package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class EventsActivity extends AppCompatActivity {

    private TextView tvUsername;
    private Button btnNavEvents, btnNavMyEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Mapiranje UI elemenata
        tvUsername = findViewById(R.id.tvUsername);
        btnNavEvents = findViewById(R.id.btnNavEvents);
        btnNavMyEvents = findViewById(R.id.btnNavMyEvents);

        // Preuzimanje podataka iz LoginActivity-ja
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String dobijeniUsername = bundle.getString("USERNAME");
            if (dobijeniUsername != null && !dobijeniUsername.isEmpty()) {
                tvUsername.setText(dobijeniUsername);
            }
        }

        // ZAHTEV: Podrazumevano se pri pokretanju prikazuje EventsFragment
        if (savedInstanceState == null) {
            ucitajFragment(new EventsFragment());
        }

        // Akcija za donje dugme "EVENTS"
        btnNavEvents.setOnClickListener(v -> {
            ucitajFragment(new EventsFragment());
        });

        // Akcija za donje dugme "MY EVENTS" (Učitava novi MyEventsFragment)
        btnNavMyEvents.setOnClickListener(v -> {
            ucitajFragment(new MyEventsFragment());
        });
    }

    // Pomoćna metoda za zamenu fragmenta
    private void ucitajFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }
}