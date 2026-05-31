package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class EventsActivity extends AppCompatActivity {

    private TextView Username;
    private Button Iventovi, MojiIventovi, Prijatelji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Username = findViewById(R.id.tvUsername);
        Iventovi = findViewById(R.id.btnNavEvents);
        MojiIventovi = findViewById(R.id.btnNavMyEvents);
        Prijatelji = findViewById(R.id.btnNavFriends);

        Prijatelji.setEnabled(true);
        Prijatelji.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.purple_button));

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

        Prijatelji.setOnClickListener(v -> {
            ucitajFragment(new FriendsFragment());
        });
    }

    private void ucitajFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }
}