package Sinisa.Stevanovic.eventsApp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CheckBox;
import Sinisa.Stevanovic.eventsApp.PasswordHasher;
import Sinisa.Stevanovic.eventsApp.DBHelper;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class LoginActivity extends AppCompatActivity {

    private LinearLayout Buttons, LoginForm, RegisterForm;
    private Button btnInitialLogin, btnInitialRegister, btnSubmitLogin, btnSubmitRegister;
    private EditText LoginUsername, LoginPassword;
    private EditText etRegUsername, etRegEmail, etRegPassword;
    private CheckBox cbRegIsAdmin;
    private DBHelper dbHelper;

    @Override
    public void onBackPressed() {
        // Ako je vidljiva forma za logovanje, sakrij je i vrati početne dugmiće
        if (LoginForm.getVisibility() == View.VISIBLE) {
            LoginForm.setVisibility(View.GONE);
            Buttons.setVisibility(View.VISIBLE);
        }
        // Ako je vidljiva forma za registraciju, sakrij je i vrati početne dugmiće
        else if (RegisterForm.getVisibility() == View.VISIBLE) {
            RegisterForm.setVisibility(View.GONE);
            Buttons.setVisibility(View.VISIBLE);
        }
        // Ako smo već na početnom ekranu , ponašaj se normalno
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

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
        cbRegIsAdmin = findViewById(R.id.cbRegIsAdmin);

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

            if (!username.isEmpty() && !password.isEmpty()){
                String userEmail = dbHelper.checkUserLogin(username, password);

                if (userEmail != null) {
                    prelazNaEventsActivity(username, userEmail);
                } else {
                    Toast.makeText(this, R.string.toast_wrong_credentials, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.toast_empty_fields, Toast.LENGTH_SHORT).show();
            }

        });

        btnSubmitRegister.setOnClickListener(v -> {
            String username = etRegUsername.getText().toString();
            String email = etRegEmail.getText().toString();
            String password = etRegPassword.getText().toString();
            boolean isAdmin = cbRegIsAdmin.isChecked();


            if (!username.isEmpty() && !email.isEmpty() && password.length() >= 8) {
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                    return;
                }
                Runnable regZadatak = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try{
                            URL url = new URL("http://192.168.0.16:3000/users");
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");
                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setDoOutput(true);
                            //Formiranje Json objekta
                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("username",username);
                            jsonBody.put("password",password);
                            jsonBody.put("email",email);
                            jsonBody.put("isAdmin",isAdmin);

                            OutputStream os = urlConnection.getOutputStream();
                            os.write(jsonBody.toString().getBytes("UTF-8"));
                            os.flush();
                            os.close();

                            int responseCode = urlConnection.getResponseCode();

                            if(responseCode == HttpURLConnection.HTTP_OK){
                                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while((line = br.readLine())!=null){
                                    sb.append(line);
                                }
                                JSONObject registrovaniKorisnik = new JSONObject(sb.toString());
                                String serverId = registrovaniKorisnik.getString("_id");

                                long newRowId = dbHelper.registerUser(serverId, username, email, password);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (newRowId != -1) {
                                            Toast.makeText(LoginActivity.this, R.string.successfully_register, Toast.LENGTH_SHORT).show();
                                            prelazNaEventsActivity(username, email);
                                        } else {
                                            Toast.makeText(LoginActivity.this, R.string.copy, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else if (responseCode == 409) { // HTTP_CONFLICT
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, R.string.no_user, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        }catch (Exception e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, R.string.server_conn_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                    }
                };
                /*long newRowId = dbHelper.registerUser(username, email, password);

                if (newRowId != -1) {
                    Toast.makeText(this, R.string.successfully_register, Toast.LENGTH_SHORT).show();
                    prelazNaEventsActivity(username, email);
                } else {
                    Toast.makeText(this, R.string.copy, Toast.LENGTH_LONG).show();
                }*/
                new Thread(regZadatak).start();

            } else if (password.length() < 8) {
                Toast.makeText(this, R.string.lenght_pass, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });
    }

        private void prelazNaEventsActivity(String username, String email) {
            // Otvaram shared preferences pod nazivom User Session
            android.content.SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = sp.edit();
            //Upisuje username ulogovanog ili registrovanog korisnika pod kljucem LOGGED_IN_USER
            editor.putString("LOGGED_IN_USER", username);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, EventsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("USERNAME", username);
            bundle.putString("EMAIL", email);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }