package Sinisa.Stevanovic.eventsApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword;
    private Button btnSavePassword;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
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
                Runnable promenaLozinkeMreza = new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection = null;
                        try {
                            URL url = new URL("http://192.168.0.14:3000/password");
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("PUT");
                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setDoOutput(true);

                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("username", loggedInUsername);
                            jsonBody.put("oldPassword", currentPassword);
                            jsonBody.put("newPassword", newPassword);

                            OutputStream os = urlConnection.getOutputStream();
                            os.write(jsonBody.toString().getBytes("UTF-8"));
                            os.flush();
                            os.close();

                            int responseCode = urlConnection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    sb.append(line);
                                }
                                br.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PasswordActivity.this, R.string.toast_password_changed, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });

                            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == 400) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PasswordActivity.this, R.string.bad_pass, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PasswordActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PasswordActivity.this, R.string.server_conn_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                    }
                };
                new Thread(promenaLozinkeMreza).start();
            } else {
                Toast.makeText(PasswordActivity.this, R.string.no_user, Toast.LENGTH_SHORT).show();
            }
        });
    }
}