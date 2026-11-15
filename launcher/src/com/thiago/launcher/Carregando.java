package com.thiago.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

/**
 * "CARREGANDO..." animado (anel de blocos girando e pontinhos, com as letras de pixel da logo).
 * Os 8 quadros vem lado a lado em assets/carregando.png (gerado pelo art_src/gera_animacoes.py).
 * Desenha no tamanho que couber na View, centrado, com ate duas linhas de texto embaixo.
 * So anima enquanto esta visivel.
 */
public class Carregando extends View {
    private static Bitmap tira;          // uma so para o app inteiro (cada View so desenha um pedaco)
    private static final int QUADROS = 8, MS = 100;
    private int quadro;
    private float escala = 1f;
    private String linha1, linha2;
    private final Paint p = new Paint(Paint.FILTER_BITMAP_FLAG), t1 = new Paint(Paint.ANTI_ALIAS_FLAG), t2;
    private final Rect src = new Rect(), dst = new Rect();

    public Carregando(Context c) {
        super(c);
        if (tira == null) {
            try {
                java.io.InputStream in = c.getAssets().open("carregando.png");
                tira = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Throwable e) { tira = null; }
        }
        p.setFilterBitmap(false);        // pixel art: sem borrar ao aumentar
        t1.setColor(0xFFFFFFFF);
        t1.setTypeface(Typeface.DEFAULT_BOLD);
        t1.setTextAlign(Paint.Align.CENTER);
        t1.setShadowLayer(3, 0, 1, 0xC0000000);
        t2 = new Paint(t1);
        t2.setTypeface(Typeface.DEFAULT);
        t2.setColor(0xFFB8C0D8);
    }

    /** Tamanho do desenho: 1 = o tamanho dos quadros (445 x 72 px). */
    public void setEscala(float e) { escala = e; requestLayout(); invalidate(); }

    public void setTexto(String a, String b) { linha1 = a; linha2 = b; invalidate(); }

    private final Runnable passo = new Runnable() {
        public void run() {
            quadro = (quadro + 1) % QUADROS;
            invalidate();
            if (isShown() && getWindowVisibility() == VISIBLE) postDelayed(this, MS);
        }
    };

    @Override
    protected void onVisibilityChanged(View v, int vis) {
        super.onVisibilityChanged(v, vis);
        removeCallbacks(passo);
        if (isShown()) postDelayed(passo, MS);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(passo);
        if (getVisibility() == VISIBLE) postDelayed(passo, MS);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(passo);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas c) {
        if (tira == null) return;
        int qw = tira.getWidth() / QUADROS, qh = tira.getHeight();
        float e = Math.min(escala, Math.min(getWidth() * 0.9f / qw, getHeight() * 0.9f / qh));
        int w = Math.round(qw * e), h = Math.round(qh * e);
        float tx = getHeight() * 0.045f + 10;
        t1.setTextSize(tx);
        t2.setTextSize(tx * 0.8f);
        int extra = (linha1 != null ? Math.round(tx * 1.9f) : 0) + (linha2 != null ? Math.round(tx * 1.4f) : 0);
        int x = (getWidth() - w) / 2, y = (getHeight() - h - extra) / 2;
        src.set(quadro * qw, 0, quadro * qw + qw, qh);
        dst.set(x, y, x + w, y + h);
        c.drawBitmap(tira, src, dst, p);
        float ly = y + h + tx * 1.7f;
        if (linha1 != null) { c.drawText(linha1, getWidth() / 2f, ly, t1); ly += tx * 1.4f; }
        if (linha2 != null) c.drawText(linha2, getWidth() / 2f, ly, t2);
    }
}
