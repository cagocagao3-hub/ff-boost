package com.sualoja.ffboost;

import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class FpsOverlayService extends Service {
    private WindowManager wm;
    private View overlayView;
    private boolean isBoostActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Criando uma view que desenha por cima do jogo
        overlayView = new View(this) {
            private final Paint paint = new Paint();
            private final ColorMatrix cm = new ColorMatrix();
            
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (isBoostActive) {
                    // Filtro de contraste para destacar inimigos (estilo massinha)
                    cm.setSaturation(1.5f); // Aumenta a saturação
                    // Reduz o brilho levemente para dar "consistência" de FPS
                    cm.postConcat(new ColorMatrix(new float[] {
                        1.2f, 0, 0, 0, -20,
                        0, 1.2f, 0, 0, -20,
                        0, 0, 1.2f, 0, -20,
                        0, 0, 0, 1, 0
                    }));
                    
                    paint.setColorFilter(new ColorMatrixColorFilter(cm));
                    canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                }
            }
        };

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        
        wm.addView(overlayView, params);
        
        // Lógica para ativar/desativar o filtro (ex: um botão flutuante que chama isBoostActive = true)
        // Para simplificar, já iniciamos ativado.
        isBoostActive = true; 
        overlayView.invalidate();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
