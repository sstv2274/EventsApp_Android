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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        lvEvents = view.findViewById(R.id.lvEvents);
        llCategoryFilters = view.findViewById(R.id.llCategoryFilters);
        btnAddEvent = view.findViewById(R.id.btnAddEvent);

        // Ucitavam niz stringova iz string.xml
        categories = getResources().getStringArray(R.array.event_categories);

        // Ucitavam i prosledjujem dogadjaje
        List<Event> initialEvents = AppData.getSortedEvents();
        eventAdapter = new EventAdapter(getActivity(), initialEvents);
        lvEvents.setAdapter(eventAdapter);

        //!!// Postavljam klik na listu, za pocetak samo toast poruka.
        lvEvents.setOnItemClickListener((parent, view1, position, id) -> {
            Event selectedEvent = (Event) eventAdapter.getItem(position);//Izvlacimo event uz pomoc pozicije
            String toastMsg = getString(R.string.toast_clicked_event) + selectedEvent.getName();//taoast poruka
            Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();//ispisavanje toast poruke


        });

        //Samo iskoci toast kad stisnemo na AddEvent
        btnAddEvent.setOnClickListener(v -> {
            Toast.makeText(getActivity(), R.string.toast_add_event_transition, Toast.LENGTH_SHORT).show();

        });

        setupCategoryButtons();

        return view;
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

            // Dugme SVE je obojeno u ljubicastu na pocetku zato sto prikazujemo sve eventove(zato if(i==0)).
            // Kasnije pritiskom na neko od ostalih dugmadi njihova boja se menja u ljubicastu a proslo dugme vraca boju u crnu
            if (i == 0) {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.purple_button));
            } else {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.black));
            }

            int finalI = i;
            //Ovako ce svako dugme zapamtiti svoj ID
            btn.setOnClickListener(v -> {
                updateActiveCategoryColors(finalI); // Menja boju dugmića

                //Proveravamo da li je stisnuto sve.Ako jeste odna ispisujemo sve events sortirano a ako je nego drugo
                //onda ispisujemo samo tu kategoriju sortirano
                if (category.equals(categories[0])) {
                    eventAdapter.setEvents(AppData.getSortedEvents());
                } else {
                    eventAdapter.setEvents(AppData.getSortedEventsByCategory(category));
                }
            });

            categoryButtons[i] = btn;
            llCategoryFilters.addView(btn);


        }
    }

    // Metoda za vizuelno naglašavanje samo onog dugmeta koje je kliknuto
    private void updateActiveCategoryColors(int activeIndex) {
        for (int i = 0; i < categoryButtons.length; i++) {
            if (i == activeIndex) {
                categoryButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.purple_button));
            } else {
                categoryButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.black));
            }
        }
    }
}