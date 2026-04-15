package Sinisa.Stevanovic.eventsApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MyEventsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Učitavamo XML izgled
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        // Mapiranje dugmića unutar Fragmenta
        Button btnInterested = view.findViewById(R.id.btnInterested);
        Button btnAttending = view.findViewById(R.id.btnAttending);
        Button btnProfile = view.findViewById(R.id.btnProfile);

        // 1. Akcija: Prelaz na InterestedEventsActivity
        btnInterested.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InterestedEventsActivity.class);
            startActivity(intent);
        });

        // 2. Akcija: Prelaz na AttendingEventsActivity
        btnAttending.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AttendingEventsActivity.class);
            startActivity(intent);
        });

        // 3. Akcija: Prelaz na ProfileActivity uz prenos podataka
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);

            // "Hvatamo" podatke (Bundle) koji su stigli u glavni EventsActivity iz LoginActivity-ja
            if (getActivity() != null && getActivity().getIntent() != null) {
                Bundle stariBundle = getActivity().getIntent().getExtras();

                if (stariBundle != null) {
                    // Pakujemo username i email u novi Bundle i šaljemo dalje
                    Bundle noviBundle = new Bundle();
                    noviBundle.putString("USERNAME", stariBundle.getString("USERNAME"));
                    noviBundle.putString("EMAIL", stariBundle.getString("EMAIL"));
                    intent.putExtras(noviBundle);
                }
            }

            startActivity(intent);
        });

        return view;
    }
}