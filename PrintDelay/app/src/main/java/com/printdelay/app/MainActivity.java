package com.printdelay.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int MIN_SEGUNDOS = 3;
    private static final String PREFS = "prefs";
    private static final String KEY_ATRASO = "atraso";

    private SeekBar seekDelay;
    private TextView tvDelay;
    private TextView tvStatus;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        seekDelay = findViewById(R.id.seekDelay);
        tvDelay = findViewById(R.id.tvDelay);
        tvStatus = findViewById(R.id.tvStatus);
        Button btnPrint = findViewById(R.id.btnPrint);

        seekDelay.setProgress(prefs.getInt(KEY_ATRASO, 5) - MIN_SEGUNDOS);
        atualizarTextoAtraso();

        seekDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                atualizarTextoAtraso();
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                prefs.edit().putInt(KEY_ATRASO, segundos()).apply();
            }
        });

        btnPrint.setOnClickListener(v -> tirarPrint());
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarStatus();
    }

    private int segundos() {
        return seekDelay.getProgress() + MIN_SEGUNDOS;
    }

    private void atualizarTextoAtraso() {
        tvDelay.setText("Atraso: " + segundos() + " segundos");
    }

    private void atualizarStatus() {
        if (PrintService.instance != null) {
            tvStatus.setText("Serviço ativo ✓");
            tvStatus.setTextColor(0xFF2E7D32);
        } else {
            tvStatus.setText("Serviço de acessibilidade desativado.\nToque em \"Tirar print\" para ativar.");
            tvStatus.setTextColor(0xFFC62828);
        }
    }

    private void tirarPrint() {
        PrintService servico = PrintService.instance;
        if (servico == null) {
            mostrarAjudaAtivacao();
            return;
        }
        int s = segundos();
        prefs.edit().putInt(KEY_ATRASO, s).apply();
        servico.agendar(s);
        Toast.makeText(this, "Print em " + s + " segundos…", Toast.LENGTH_SHORT).show();
        // Volta para o app/tela que estava antes, para você navegar até onde quer.
        moveTaskToBack(true);
    }

    private void mostrarAjudaAtivacao() {
        new AlertDialog.Builder(this)
                .setTitle("Ativar o serviço")
                .setMessage("Para capturar a tela, o Android exige que você ative o serviço de acessibilidade uma única vez.\n\n"
                        + "1. Toque em \"Abrir configurações\"\n"
                        + "2. Procure por \"Print com Atraso\" (pode estar em \"Apps instalados\")\n"
                        + "3. Ative o serviço e confirme\n"
                        + "4. Volte para este app")
                .setPositiveButton("Abrir configurações",
                        (d, w) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
