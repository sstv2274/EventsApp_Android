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
    import org.json.JSONArray;
    import org.json.JSONObject;
    import java.io.BufferedReader;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.util.ArrayList;

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
                        preuzmiDogadjajeSaServera(null);
                    } else {
                        String serverCategoryParam = category.toUpperCase().replace(" ", "_");
                        preuzmiDogadjajeSaServera(serverCategoryParam);
                    }
                });

                categoryButtons[i] = btn;
                llCategoryFilters.addView(btn);


            }
        }

        private void preuzmiDogadjajeSaServera(final String kategorijaParam){
            Runnable getZadatak = new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection urlConnection = null;
                    try{
                        String urlString= "http://192.168.0.14:3000/events";
                        if(kategorijaParam!=null){
                            urlString += "/" + kategorijaParam;
                        }

                        URL url = new URL(urlString);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setRequestProperty("Content-Type", "application/json");
                        int responseCode=urlConnection.getResponseCode();

                        if (responseCode==HttpURLConnection.HTTP_OK){
                            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            StringBuilder sb= new StringBuilder();
                            String line;
                            while((line= br.readLine())!=null){
                                sb.append(line);
                            }
                            br.close();

                            JSONArray jsonArray = new JSONArray(sb.toString());
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject obj = jsonArray.getJSONObject(i);

                                String serverId = obj.getString("_id");
                                String name = obj.getString("name");
                                String description = obj.optString("description", "");
                                String location = obj.getString("location");
                                String eventTime = obj.getString("eventTime");
                                String category = obj.getString("category");
                                boolean promoted = obj.optBoolean("promoted", false);
                                int capacity = obj.optInt("capacity", 0);
                                int attendees = obj.optInt("numberOfAttendees", 0);
                                double avgRating = obj.optDouble("avgRating", 0.0);
                                int ratingsCount = obj.optInt("numberOfRatings", 0);
                                boolean isExclusive = obj.optBoolean("isExclusive",false);//Menjao ovde
                                String ExpirationTime = obj.optString("expirationTime","");//Menjao ovde

                                int promotedInt = promoted ? 1 : 0;

                                dbHelper.syncEventFromServer(
                                        serverId, name, description, location, eventTime, category,
                                        promotedInt, capacity, attendees, avgRating, ratingsCount,isExclusive,ExpirationTime // Menjano
                                );

                            }
                            if(getActivity()!=null){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (kategorijaParam == null) {
                                            eventAdapter.setEvents(dbHelper.getAllEvents());
                                        } else {

                                            eventAdapter.setEvents(dbHelper.getEventsByCategory(kategorijaParam));
                                        }
                                    }
                                });
                            }
                        }else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), R.string.server_conn_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }

                }
            };
            new Thread(getZadatak).start();
        }
        @Override
        public void onResume() {
            super.onResume();
            preuzmiDogadjajeSaServera(null);

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