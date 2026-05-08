package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private EditText etEventName, etEventDesc, etEventLocation, etEventDateTime, etCapacity;
    private Spinner spinnerCategory;
    private CheckBox cbPromoted;
    private Button btnCreateEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        etEventName = findViewById(R.id.etEventName);
        etEventDesc = findViewById(R.id.etEventDesc);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventDateTime = findViewById(R.id.etEventDateTime);
        etCapacity = findViewById(R.id.etCapacity);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbPromoted = findViewById(R.id.cbPromoted);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);


        String[] categoriesArray = getResources().getStringArray(R.array.create_event_categories);
        List<String> categoryList = new ArrayList<>();
        categoryList.add(getString(R.string.spinner_hint)); //Dodajem samo izberite kategoriju na prvo mesto
        categoryList.addAll(Arrays.asList(categoriesArray)); // Dodajemo moguce opcije

        //prosledjujem adapteru kako spiner treba da izgleda(mucio me dark theme pa sam ovako resio)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, categoryList);

        //takodje sam prosledio da i padajuci meni koristi isti dizajn
        adapter.setDropDownViewResource(R.layout.item_spinner);

        spinnerCategory.setAdapter(adapter);

        // logika za prikazivanje edittext-a za kapacitet kada je cekiran check box i kada nije
        cbPromoted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etCapacity.setVisibility(View.VISIBLE);
            } else {
                etCapacity.setVisibility(View.GONE);
                etCapacity.setText(""); //postavljam prazan tekst
            }
        });

        // postavio onclicklistner na create dugme
        btnCreateEvent.setOnClickListener(v -> {
            String name = etEventName.getText().toString().trim();
            String desc = etEventDesc.getText().toString().trim();
            String location = etEventLocation.getText().toString().trim();
            String dateTime = etEventDateTime.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            boolean isPromoted = cbPromoted.isChecked();

            //Uslov da moraju biti uneti ime,lokacija i datum(kasnije sam dodao i za proveru formata datuma al neka i ovog)
            if (name.isEmpty() || location.isEmpty() || dateTime.isEmpty()) {
                Toast.makeText(this, R.string.toast_required_fields, Toast.LENGTH_SHORT).show();
                return; //kraj izvrsavanja ako nije dobro uneto
            }

            // provera formata datuma
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            sdf.setLenient(false); // zbog ove linije mora neki realan datum a ne samo dobar format(ne moze 31 februar)
            try {
                sdf.parse(dateTime); // Pokusava da pretvori tekst u datum
            } catch (ParseException e) {
                //ako nije uhvatio tekst iskace toast
                Toast.makeText(this, R.string.toast_invalid_date_format, Toast.LENGTH_SHORT).show();
                return; // kraj
            }

            // Provera da li je korisnik izabrao validnu opciju
            if (category.equals(getString(R.string.spinner_hint))) {
                Toast.makeText(this, R.string.toast_select_category, Toast.LENGTH_SHORT).show();
                return; // kraj
            }

            //Provera kapaciteta
            int capacity = 0;
            if (isPromoted) {
                String capacityStr = etCapacity.getText().toString().trim();
                try {
                    capacity = Integer.parseInt(capacityStr);
                    if (capacity <= 0 || capacity > 1000000) {//ako bude vise od milion onda anlaki
                        throw new NumberFormatException(); //bacam gresku
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.toast_invalid_capacity, Toast.LENGTH_SHORT).show();
                    return; //kraj
                }
            }

            //Kreiranje dogadjaja preko event factory kada su svi podaci uspesno preuzeti u dobrom formatu
            Event newEvent;
            if (isPromoted) {
                newEvent = EventFactory.createPromotedEvent(name, desc, location, dateTime, category, R.drawable.default_picture, capacity);
            } else {
                newEvent = EventFactory.createRegularEvent(name, desc, location, dateTime, category, R.drawable.default_picture);
            }

            //Samo dodam u glavnu listu i to je to
            AppData.allEvents.add(newEvent);

            // potvrda da je uspesno napravljen ivent
            Toast.makeText(this, R.string.toast_event_created, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}