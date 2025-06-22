package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Esconde a action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Delay para mostrar a splash screen e depois navegar para MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Remove a SplashActivity da pilha de activities
        }, SPLASH_DURATION);
    }

    @Override
    public void onBackPressed() {
        // Desabilita o botão voltar na splash screen
        // super.onBackPressed(); - comentado para prevenir que o usuário volte
    }
}