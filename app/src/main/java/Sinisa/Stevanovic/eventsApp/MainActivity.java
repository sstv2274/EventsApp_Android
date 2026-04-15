package Sinisa.Stevanovic.eventsApp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    LinearLayout kontejnerDugmica;
    LinearLayout kontejnerLogina;
    LinearLayout kontejnerRegistracije;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kontejnerDugmica = findViewById(R.id.kontejnerPocetnihDugmica);
        kontejnerLogina = findViewById(R.id.kontejnerLogina);
        kontejnerRegistracije = findViewById(R.id.kontejnerRegistracije);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kontejnerDugmica.setVisibility(View.GONE);
                kontejnerLogina.setVisibility(View.VISIBLE);
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kontejnerDugmica.setVisibility(View.GONE);
                kontejnerRegistracije.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (kontejnerLogina.getVisibility() == View.VISIBLE ||
                kontejnerRegistracije.getVisibility() == View.VISIBLE) {

            kontejnerLogina.setVisibility(View.GONE);
            kontejnerRegistracije.setVisibility(View.GONE);
            kontejnerDugmica.setVisibility(View.VISIBLE);

        } else {
            super.onBackPressed();
        }
    }
}