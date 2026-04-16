package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class EventsActivity extends AppCompatActivity {

    private TextView Username;
    private Button Iventovi, MojiIventovi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Username = findViewById(R.id.tvUsername);
        Iventovi = findViewById(R.id.btnNavEvents);
        MojiIventovi = findViewById(R.id.btnNavMyEvents);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String dobijeniUsername = bundle.getString("USERNAME");
            if (dobijeniUsername != null && !dobijeniUsername.isEmpty()) {
                Username.setText(dobijeniUsername);
            }
        }

        if (savedInstanceState == null) {
            ucitajFragment(new EventsFragment());
        }

        Iventovi.setOnClickListener(v -> {
            ucitajFragment(new EventsFragment());
        });

        MojiIventovi.setOnClickListener(v -> {
            ucitajFragment(new MyEventsFragment());
        });
    }

    private void ucitajFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }
}