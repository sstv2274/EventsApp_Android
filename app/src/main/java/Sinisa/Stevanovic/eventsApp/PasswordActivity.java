package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword;
    private Button btnSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();

            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                Toast.makeText(PasswordActivity.this, R.string.toast_password_changed, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(PasswordActivity.this, R.string.toast_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });
    }
}