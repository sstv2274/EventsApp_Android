package Sinisa.Stevanovic.eventsApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileUsername, tvProfileEmail;
    private Button btnPassword, btnEndSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnPassword = findViewById(R.id.btnPassword);
        btnEndSession = findViewById(R.id.btnEndSession);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String username = bundle.getString("USERNAME");
            String email = bundle.getString("EMAIL");

            if (username != null && !username.isEmpty()) {
                tvProfileUsername.setText(username);
            }
            if (email != null && !email.isEmpty()) {
                tvProfileEmail.setText(email);
            }
        }

        btnPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PasswordActivity.class);
            startActivity(intent);
        });

        btnEndSession.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}