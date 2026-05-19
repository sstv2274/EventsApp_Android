package Sinisa.Stevanovic.eventsApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RatingActivity extends AppCompatActivity {

    private TextView tvRatingEventName;
    private ImageView[] stars = new ImageView[5];
    private Button btnConfirmRating;
    private int selectedRating = 0;

    private DBHelper dbHelper;
    private int currentUserId;
    private String eventName;

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

        dbHelper = new DBHelper(this);

        // Kupim id korisnika
        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String loggedInUser = sp.getString("LOGGED_IN_USER", "");
        currentUserId = dbHelper.getUserIdByUsername(loggedInUser);

        // Preuzimanje imena dogadjaja preko intenta
        eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName != null) {
            tvRatingEventName.setText(eventName);

            // Proveravam da li postoji ocena i ako postoji palim zvezdice(kako bi korisnik znao da je vec ocenio)
            int postojecaOcena = dbHelper.getUserRatingForEvent(currentUserId, eventName);
            if (postojecaOcena > 0) {
                updateStarUI(postojecaOcena);
            }
        }

        // Postavljanje klika na zvezdice
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i;
            stars[i].setOnClickListener(v -> updateStarUI(starIndex + 1));
        }

        // Potvrda ocenjivanja
        btnConfirmRating.setOnClickListener(v -> {
            if (selectedRating == 0) {
                Toast.makeText(this, R.string.toast_select_rating, Toast.LENGTH_SHORT).show();
            } else {
                //Upis u bazu
                boolean uspeh = dbHelper.handleRating(currentUserId, eventName, selectedRating);

                if (uspeh) {
                    Toast.makeText(this, R.string.toast_rating_saved, Toast.LENGTH_SHORT).show();
                    finish(); // Povratak na AttendingEventsActivity
                } else {
                    Toast.makeText(this, R.string.input_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateStarUI(int rating) {
        selectedRating = rating;

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.star_filled);
            } else {
                stars[i].setImageResource(R.drawable.star_empty);
            }
        }
    }
}