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

        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        Button btnInterested = view.findViewById(R.id.btnInterested);
        Button btnAttending = view.findViewById(R.id.btnAttending);
        Button btnProfile = view.findViewById(R.id.btnProfile);


        btnInterested.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InterestedEventsActivity.class);
            startActivity(intent);
        });

        btnAttending.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AttendingEventsActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            if (getActivity() != null && getActivity().getIntent() != null) {
                Bundle stariBundle = getActivity().getIntent().getExtras();

                if (stariBundle != null) {

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