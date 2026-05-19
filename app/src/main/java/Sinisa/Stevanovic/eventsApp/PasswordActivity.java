package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword;
    private Button btnSavePassword;

    private DBHelper dbHelper;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        dbHelper = new DBHelper(this);

        loggedInUsername = getIntent().getStringExtra("USERNAME");

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(PasswordActivity.this, R.string.toast_empty_fields, Toast.LENGTH_SHORT).show();
            }else if (newPassword.length() < 8) {
                Toast.makeText(PasswordActivity.this, R.string.lenght_pass, Toast.LENGTH_SHORT).show();
            }else if (loggedInUsername != null) {

                boolean isChanged = dbHelper.changePassword(loggedInUsername, currentPassword, newPassword);

                if (isChanged) {
                    Toast.makeText(PasswordActivity.this, R.string.toast_password_changed, Toast.LENGTH_SHORT).show();
                    finish(); //vraca korisnika nazad nakon promene sifre
                } else {
                    // Uneta losa stara lozinka
                    Toast.makeText(PasswordActivity.this, R.string.bad_pass, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PasswordActivity.this, R.string.no_user, Toast.LENGTH_SHORT).show();
            }

        });
    }
}