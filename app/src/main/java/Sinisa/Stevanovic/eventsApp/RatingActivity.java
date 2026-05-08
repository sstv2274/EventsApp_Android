package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RatingActivity extends AppCompatActivity {

    private TextView tvRatingEventName;
    private ImageView[] stars = new ImageView[5]; //Niz zvezdica za rejting
    private Button btnConfirmRating;
    private int selectedRating = 0;
    private Event eventToRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        tvRatingEventName = findViewById(R.id.tvRatingEventName);
        btnConfirmRating = findViewById(R.id.btnConfirmRating);


        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        //Preuzeo sam imena dogadjaja preko intenta
        String eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName != null) {
            tvRatingEventName.setText(eventName);
            eventToRate = AppData.findByName(eventName);
        }

        //Stavlja onClickListener na zvezdice
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i; //Broj najvece zvezde
            stars[i].setOnClickListener(v -> updateStarUI(starIndex + 1));
        }

        //Ocenjivanje(pritisak na zvezdu)
        btnConfirmRating.setOnClickListener(v -> {
            if (selectedRating == 0) {
                //toast poruka kada ne izaberemo nijednu zvezdicu
                Toast.makeText(this, R.string.toast_select_rating, Toast.LENGTH_SHORT).show();
            } else {
                if (eventToRate != null) {
                    //Dodavanje nove ocene
                    //!!// Ostavio sam da moze vise ocena da se doda zbog testa ukupnog rejtinga
                    //!!//U kasnijem razvoju cu namestiti da moze samo jednom ocena da se doda i zatim kada udje u rejting aktiviti
                    //!!//Sijace autoamtski broj zvezdica sa kojom smo ocenili ivent
                    eventToRate.addRating(selectedRating);
                }
                //toast poruka za uspesno izabran rejting
                Toast.makeText(this, R.string.toast_rating_saved, Toast.LENGTH_SHORT).show();
                finish(); //gasenje rating aktivitija i vraca me na prethodni aktiviti(attending)
            }
        });
    }

    //Da ne bih ceo ovaj kod pisao u ocenjivanju, napisao sam ga ispod kao metodu
    private void updateStarUI(int rating) {
        selectedRating = rating; //Ovde cuvam izabran broj zvezdica(rejting)

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                //Sve zvezdice do kliknute ukljucujuci i kliknutu postaju zute
                stars[i].setImageResource(R.drawable.star_filled);
            } else {
                //A ostalo postavljam na sive
                stars[i].setImageResource(R.drawable.star_empty);
            }
        }
    }
}