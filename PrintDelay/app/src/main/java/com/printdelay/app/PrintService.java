package com.printdelay.app;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Serviço de acessibilidade que espera o atraso escolhido e então captura a tela
 * com AccessibilityService.takeScreenshot() (Android 11+). O print é salvo em
 * Pictures/Screenshots, então aparece direto na Galeria.
 */
public class PrintService extends AccessibilityService {

    /** Instância ativa enquanto o serviço está ligado nas configurações. */
    public static volatile PrintService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Não precisamos ler nada da tela.
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        handler.removeCallbacksAndMessages(null);
        return super.onUnbind(intent);
    }

    /** Agenda uma captura; se já havia uma pendente, ela é substituída. */
    public void agendar(int segundos) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::capturar, segundos * 1000L);
    }

    private void capturar() {
        takeScreenshot(Display.DEFAULT_DISPLAY, getMainExecutor(), new TakeScreenshotCallback() {
            @Override
            public void onSuccess(ScreenshotResult resultado) {
                // Salva em segundo plano para não travar a interface.
                new Thread(() -> {
                    try {
                        Bitmap hw = Bitmap.wrapHardwareBuffer(resultado.getHardwareBuffer(), resultado.getColorSpace());
                        Bitmap bmp = hw.copy(Bitmap.Config.ARGB_8888, false);
                        resultado.getHardwareBuffer().close();
                        salvar(bmp);
                        avisar("Print salvo na Galeria ✓", true);
                    } catch (Exception e) {
                        avisar("Erro ao salvar o print: " + e.getMessage(), false);
                    }
                }).start();
            }

            @Override
            public void onFailure(int codigo) {
                avisar("Não foi possível capturar a tela (código " + codigo + ")", false);
            }
        });
    }

    private void salvar(Bitmap bmp) throws Exception {
        String nome = "Print_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";

        ContentValues valores = new ContentValues();
        valores.put(MediaStore.Images.Media.DISPLAY_NAME, nome);
        valores.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        valores.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots");
        valores.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores);
        if (uri == null) {
            throw new Exception("MediaStore recusou o arquivo");
        }

        try (OutputStream saida = getContentResolver().openOutputStream(uri)) {
            if (saida == null || !bmp.compress(Bitmap.CompressFormat.PNG, 100, saida)) {
                throw new Exception("falha ao gravar o PNG");
            }
        }

        valores.clear();
        valores.put(MediaStore.Images.Media.IS_PENDING, 0);
        getContentResolver().update(uri, valores, null, null);
    }

    private void avisar(String mensagem, boolean vibrar) {
        handler.post(() -> {
            if (vibrar) {
                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vib != null) {
                    vib.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
        });
    }
}
