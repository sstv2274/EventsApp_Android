    package Sinisa.Stevanovic.eventsApp;

    import android.content.Intent;
    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.LinearLayout;
    import android.widget.ListView;
    import android.widget.Toast;
    import Sinisa.Stevanovic.eventsApp.DBHelper;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.core.content.ContextCompat;
    import androidx.fragment.app.Fragment;

    import java.util.List;

    public class EventsFragment extends Fragment {
        //Deklaracija promenjivih
        private ListView lvEvents;
        private EventAdapter eventAdapter;
        private LinearLayout llCategoryFilters;
        private Button btnAddEvent;

        private String[] categories;
        private Button[] categoryButtons;
        private DBHelper dbHelper;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_events, container, false);

            lvEvents = view.findViewById(R.id.lvEvents);
            llCategoryFilters = view.findViewById(R.id.llCategoryFilters);
            btnAddEvent = view.findViewById(R.id.btnAddEvent);

            dbHelper = new DBHelper(getActivity());

            // Ucitavam niz stringova iz string.xml
            categories = getResources().getStringArray(R.array.event_categories);

            // Ucitavam i prosledjujem dogadjaje
            List<Event> initialEvents = dbHelper.getAllEvents();
            eventAdapter = new EventAdapter(getActivity(), initialEvents);
            lvEvents.setAdapter(eventAdapter);

            //Postavljam klik na listu
            lvEvents.setOnItemClickListener((parent, view1, position, id) -> {
                Event selectedEvent = (Event) eventAdapter.getItem(position);

                // Prelaz na formu sa detaljima i slanje imena događaja
                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra("EVENT_NAME", selectedEvent.getName());
                startActivity(intent);
            });

            //Menja se aktivity i cuva se intent
            btnAddEvent.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                startActivity(intent);
            });

            setupCategoryButtons();

            return view;
        }

        private android.graphics.drawable.GradientDrawable getRoundedShape(int colorResId) {
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            shape.setCornerRadius(12f); // Podesavanja zaobljenosti dugmica zbog mog mozgovanja o dinamickom dodavanju kategorija
            shape.setColor(ContextCompat.getColor(getActivity(), colorResId));
            return shape;
        }

        private void setupCategoryButtons() {
            //kreiramo dugmica onoliko koliko imamo kategorija(Cisto da bi olaksalo ako dodamo jos neku kategoriju)
            categoryButtons = new Button[categories.length];

            for (int i = 0; i < categories.length; i++) {
                //Ovde kupimo string imena kategorije
                final String category = categories[i];
                Button btn = new Button(getActivity());
                //Postavljamo da dugme bude belo i da na njemu pise categories[i] tekst
                btn.setText(category);
                btn.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

                // Takodje mozemo ovako da dinamicki pravimo dugmice za svaku novu kategoriju a ne da uvek dodajemo u xml kodu
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                btn.setLayoutParams(params);

                btn.setPadding(50, 0, 50, 0);

                // Dugme SVE je obojeno u ljubicastu na pocetku zato sto prikazujemo sve eventove(zato if(i==0)).
                // Kasnije pritiskom na neko od ostalih dugmadi njihova boja se menja u ljubicastu a proslo dugme vraca boju u crnu
                if (i == 0) {
                    btn.setBackground(getRoundedShape(R.color.purple_button));
                } else {
                    btn.setBackground(getRoundedShape(R.color.black));
                }

                int finalI = i;
                //Ovako ce svako dugme zapamtiti svoj ID
                btn.setOnClickListener(v -> {
                    for (int j = 0; j < categoryButtons.length; j++) {
                        if (j == finalI) {
                            categoryButtons[j].setBackground(getRoundedShape(R.color.purple_button));
                        } else {
                            categoryButtons[j].setBackground(getRoundedShape(R.color.black));
                        }
                    }
                     // Menja boju dugmića

                    //Proveravamo da li je stisnuto sve.Ako jeste odna ispisujemo sve events sortirano a ako je nego drugo
                    //onda ispisujemo samo tu kategoriju sortirano
                    if (category.equals(categories[0])) { // "Sve" kategorija
                        eventAdapter.setEvents(dbHelper.getAllEvents());
                    } else {
                        eventAdapter.setEvents(dbHelper.getEventsByCategory(category));
                    }
                });

                categoryButtons[i] = btn;
                llCategoryFilters.addView(btn);


            }
        }
        @Override
        public void onResume() {
            super.onResume();
            if (eventAdapter != null) {
                // ucitavam sve dogadjaje
                eventAdapter.setEvents(dbHelper.getAllEvents());

                // I vracam dugmetu SVE da bude crno
                if (categoryButtons != null && categoryButtons.length > 0) {
                    for (int i = 0; i < categoryButtons.length; i++) {
                        if (i == 0) {
                            categoryButtons[i].setBackground(getRoundedShape(R.color.purple_button));
                        } else {
                            categoryButtons[i].setBackground(getRoundedShape(R.color.black));
                        }
                    }
                }
            }
        }
    }