package Sinisa.Stevanovic.eventsApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private LinearLayout Buttons, LoginForm, RegisterForm;
    private Button btnInitialLogin, btnInitialRegister, btnSubmitLogin, btnSubmitRegister;
    private EditText LoginUsername, LoginPassword;
    private EditText etRegUsername, etRegEmail, etRegPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Mapiranje UI elemenata
        Buttons = findViewById(R.id.llInitialButtons);
        LoginForm = findViewById(R.id.llLoginForm);
        RegisterForm = findViewById(R.id.llRegisterForm);

        btnInitialLogin = findViewById(R.id.btnInitialLogin);
        btnInitialRegister = findViewById(R.id.btnInitialRegister);
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);

        LoginUsername = findViewById(R.id.etLoginUsername);
        LoginPassword = findViewById(R.id.etLoginPassword);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        //Prebacivanje na Log
        btnInitialLogin.setOnClickListener(v -> {
            Buttons.setVisibility(View.GONE);
            LoginForm.setVisibility(View.VISIBLE);
        });
        //Prebacivanje na reg
        btnInitialRegister.setOnClickListener(v -> {
            Buttons.setVisibility(View.GONE);
            RegisterForm.setVisibility(View.VISIBLE);
        });
        btnSubmitLogin.setOnClickListener(v -> {
            String username = LoginUsername.getText().toString();
            String password = LoginPassword.getText().toString();
            if (username.equals("admin") && password.equals("admin")) {
                prelazNaEventsActivity(username, "");
            } else {
                Toast.makeText(this, R.string.toast_wrong_credentials, Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmitRegister.setOnClickListener(v -> {
            String username = etRegUsername.getText().toString();
            String email = etRegEmail.getText().toString();
            String password = etRegPassword.getText().toString();

            if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                prelazNaEventsActivity(username, email);
            } else {
                Toast.makeText(this, R.string.toast_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Prenošenje podataka uz pomoć Intenta i Bundle objekta
        private void prelazNaEventsActivity(String username, String email) {
            Intent intent = new Intent(LoginActivity.this, EventsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("USERNAME", username);
            bundle.putString("EMAIL", email);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }