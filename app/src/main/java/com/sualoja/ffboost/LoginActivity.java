package com.sualoja.ffboost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {
    private EditText inputKey;
    private Button btnLogin;
    private final String API_URL = "https://social-deserve-korean-advisor.trycloudflare.com/api/auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        inputKey = new EditText(this);
        inputKey.setHint("Digite sua Key VIP");
        
        btnLogin = new Button(this);
        btnLogin.setText("Entrar");

        layout.addView(inputKey);
        layout.addView(btnLogin);
        setContentView(layout);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = inputKey.getText().toString().trim();
                if(key.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Digite uma Key", Toast.LENGTH_SHORT).show();
                    return;
                }
                validarKey(key);
            }
        });
    }

    private void validarKey(String key) {
        final String hwid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        btnLogin.setText("Validando...");
        btnLogin.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    String jsonInputString = "{\"key\": \"" + key + "\", \"hwid\": \"" + hwid + "\"}";
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    JSONObject json = new JSONObject(response.toString());
                    final String status = json.getString("status");

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (status.equals("VALID")) {
                                Toast.makeText(LoginActivity.this, "Acesso Liberado!", Toast.LENGTH_SHORT).show();
                                
                                if (!Settings.canDrawOverlays(LoginActivity.this)) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    startActivity(intent);
                                    btnLogin.setText("Aguardando Permissão...");
                                } else {
                                    startService(new Intent(LoginActivity.this, FpsOverlayService.class));
                                    finish();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Key Inválida ou Expirada", Toast.LENGTH_LONG).show();
                                btnLogin.setText("Entrar");
                                btnLogin.setEnabled(true);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                            btnLogin.setText("Entrar");
                            btnLogin.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Se voltou da tela de permissão de overlay, inicia o serviço
        if (Settings.canDrawOverlays(this) && btnLogin.getText().toString().equals("Aguardando Permissão...")) {
            startService(new Intent(this, FpsOverlayService.class));
            finish();
        }
    }
}
