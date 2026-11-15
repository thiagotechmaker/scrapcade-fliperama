package com.thiago.launcher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Perfis de jogador, como no ScrapCade OS: a tela "Quem vai jogar?" ao ligar (quando ha mais de um perfil), criar
 * perfil com nome e cor, e apagar. Cada perfil tem as suas preferencias (favoritos, mais jogados, "Continuar jogando",
 * tema...) num arquivo proprio e os seus saves (saves/perfil-N). O perfil 1 usa o que ja existia.
 * Avatar: a inicial do nome num quadrado colorido (as fontes do Android 2.3 nao tem os simbolos do PC).
 */
final class Perfis {
    interface Escolha { void escolheu(String id); }

    static final int[] CORES = {0xFF32E6A8, 0xFF2B68C8, 0xFFC93636, 0xFFD9822B, 0xFF6C47C9, 0xFF1E9C4A,
            0xFF2E9EC8, 0xFFE0B020, 0xFFE0356B, 0xFF4A4D52, 0xFF8A5A2B, 0xFF19B0B0};
    private static final String[] TECLAS = {"ABCDEFGHIJ", "KLMNOPQRST", "UVWXYZ 012", "3456789<>"};   // < apaga, > pronto
    /** Ja perguntou "Quem vai jogar?" desde que o aparelho ligou (o processo do menu vive ate desligar). */
    static boolean perguntado = false;

    private final Activity a;
    private final float u;
    private final SharedPreferences lista;
    private final Escolha escolha;
    private final FrameLayout root;
    private final LinearLayout cartoes;
    private final TextView titulo, dica;
    private final LinearLayout teclado;
    private int modo = 0;          // 0 fechado, 1 escolher, 2 nome, 3 cor, 4 confirmar apagar
    private int sel = 0, tecla = 0, cor = 0;
    private final StringBuilder nome = new StringBuilder();

    Perfis(Activity a, float u, Escolha escolha) {
        this.a = a;
        this.u = u;
        this.escolha = escolha;
        lista = a.getSharedPreferences("perfis", Activity.MODE_PRIVATE);
        if (lista.getString("ids", "").length() == 0)      // primeira vez: o perfil 1 e quem ja usava o menu
            lista.edit().putString("ids", "1").putString("nome|1", "Jogador").putInt("cor|1", 0).putString("atual", "1").commit();
        root = new FrameLayout(a);
        root.setBackgroundColor(0xF00B1660);
        root.setVisibility(View.GONE);
        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        titulo = texto(18, Color.WHITE, true);
        titulo.setGravity(Gravity.CENTER);
        col.addView(titulo);
        cartoes = new LinearLayout(a);
        cartoes.setOrientation(LinearLayout.HORIZONTAL);
        cartoes.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.topMargin = dp(16);
        col.addView(cartoes, cp);
        teclado = new LinearLayout(a);
        teclado.setOrientation(LinearLayout.VERTICAL);
        teclado.setGravity(Gravity.CENTER_HORIZONTAL);
        col.addView(teclado, cp);
        dica = texto(9, 0xCCFFFFFF, false);
        dica.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dp2.topMargin = dp(14);
        col.addView(dica, dp2);
        root.addView(col, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    }

    View view() { return root; }

    boolean isOpen() { return modo != 0; }

    // ------------------------------------------------------------ dados

    List<String> ids() {
        List<String> l = new ArrayList<String>();
        for (String s : lista.getString("ids", "1").split(",")) if (s.length() > 0) l.add(s);
        return l;
    }

    String atual() { return lista.getString("atual", "1"); }

    String nome(String id) { return lista.getString("nome|" + id, "Jogador"); }

    int corDe(String id) { return CORES[Math.max(0, Math.min(CORES.length - 1, lista.getInt("cor|" + id, 0)))]; }

    /** Arquivo de preferencias do perfil: o 1 usa o de sempre. */
    static String arquivo(String id) { return "1".equals(id) ? "fliperama" : "fliperama_p" + id; }

    /** Pasta de saves do perfil, dentro de /sdcard/saves (o 1 usa a raiz, como no ScrapCade OS). */
    static String savesSub(String id) { return "1".equals(id) ? "" : "perfil-" + id; }

    // ------------------------------------------------------------ tela

    void abrir() {
        modo = 1;
        List<String> l = ids();
        sel = Math.max(0, l.indexOf(atual()));
        root.setVisibility(View.VISIBLE);
        desenhar();
    }

    private void fechar() {
        modo = 0;
        root.setVisibility(View.GONE);
    }

    private void desenhar() {
        cartoes.removeAllViews();
        teclado.removeAllViews();
        List<String> l = ids();
        if (modo == 1 || modo == 4) {
            titulo.setText(modo == 4 ? "Apagar o perfil " + nome(l.get(sel)) + "?" : "Quem vai jogar?");
            for (int i = 0; i <= l.size(); i++) {
                boolean novo = i == l.size();
                cartoes.addView(cartao(novo ? "+" : inicial(nome(l.get(i))), novo ? "Novo perfil" : nome(l.get(i)),
                        novo ? 0xFF4A4D52 : corDe(l.get(i)), i == sel));
            }
            dica.setText(modo == 4 ? "X de novo: apaga (os saves ficam guardados)     B: cancela"
                                   : "A: entrar     X: apagar perfil     B: fechar");
        } else if (modo == 2) {
            titulo.setText("Nome do perfil:  " + nome + "_");
            for (int r = 0; r < TECLAS.length; r++) {
                LinearLayout linha = new LinearLayout(a);
                for (int c = 0; c < TECLAS[r].length(); c++) {
                    char ch = TECLAS[r].charAt(c);
                    String t = ch == '<' ? "apaga" : ch == '>' ? "pronto" : ch == ' ' ? "espaço" : String.valueOf(ch);
                    TextView k = texto(ch == '<' || ch == '>' || ch == ' ' ? 9 : 12, Color.WHITE, true);
                    k.setText(t);
                    k.setGravity(Gravity.CENTER);
                    boolean s = r * 10 + c == tecla;
                    k.setBackgroundColor(s ? 0xFF2FD09A : 0x33FFFFFF);
                    k.setTextColor(s ? 0xFF0B1660 : Color.WHITE);
                    LinearLayout.LayoutParams kp = new LinearLayout.LayoutParams(dp(ch == '<' || ch == '>' || ch == ' ' ? 54 : 30), dp(26));
                    kp.setMargins(dp(2), dp(2), dp(2), dp(2));
                    linha.addView(k, kp);
                }
                teclado.addView(linha);
            }
            dica.setText("A: escreve     B: apaga     START: pronto");
        } else if (modo == 3) {
            titulo.setText("Cor de " + nome);
            cartoes.addView(cartao(inicial(nome.toString()), nome.toString(), CORES[cor], true));
            dica.setText("Esquerda/direita: troca a cor     A: criar perfil     B: voltar ao nome");
        }
    }

    private static String inicial(String n) {
        n = n.trim();
        return n.length() == 0 ? "?" : n.substring(0, 1).toUpperCase();
    }

    private View cartao(String letra, String legenda, int c, boolean escolhido) {
        LinearLayout v = new LinearLayout(a);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView q = texto(30, Color.WHITE, true);
        q.setText(letra);
        q.setGravity(Gravity.CENTER);
        GradientDrawable g = new GradientDrawable();
        g.setColor(c);
        g.setCornerRadius(dp(6));
        if (escolhido) g.setStroke(dp(3), 0xFF2FD09A);
        q.setBackgroundDrawable(g);
        v.addView(q, new LinearLayout.LayoutParams(dp(escolhido ? 78 : 66), dp(escolhido ? 78 : 66)));
        TextView t = texto(10, escolhido ? Color.WHITE : 0xBBFFFFFF, escolhido);
        t.setText(legenda);
        t.setGravity(Gravity.CENTER);
        v.addView(t);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(96), LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(6), 0, dp(6), 0);
        v.setLayoutParams(p);
        return v;
    }

    // ------------------------------------------------------------ botoes

    void key(int k, boolean first) {
        boolean left = k == KeyEvent.KEYCODE_DPAD_LEFT, right = k == KeyEvent.KEYCODE_DPAD_RIGHT;
        boolean up = k == KeyEvent.KEYCODE_DPAD_UP, down = k == KeyEvent.KEYCODE_DPAD_DOWN;
        boolean ok = k == KeyEvent.KEYCODE_BUTTON_A || k == KeyEvent.KEYCODE_ENTER || k == KeyEvent.KEYCODE_DPAD_CENTER;
        boolean back = k == KeyEvent.KEYCODE_BUTTON_B || k == KeyEvent.KEYCODE_BACK;
        boolean x = k == KeyEvent.KEYCODE_BUTTON_X;
        boolean start = k == KeyEvent.KEYCODE_BUTTON_START;
        List<String> l = ids();
        if (modo == 1) {
            if (left) sel = (sel + l.size()) % (l.size() + 1);
            else if (right) sel = (sel + 1) % (l.size() + 1);
            else if (ok && first) {
                if (sel == l.size()) { modo = 2; nome.setLength(0); tecla = 0; }
                else { lista.edit().putString("atual", l.get(sel)).commit(); fechar(); escolha.escolheu(l.get(sel)); return; }
            } else if (x && first && sel < l.size() && l.size() > 1) modo = 4;
            else if (back && first) { fechar(); return; }
        } else if (modo == 4) {
            if (x && first) {
                String id = l.get(sel);
                l.remove(sel);
                StringBuilder b = new StringBuilder();
                for (String s : l) b.append(b.length() > 0 ? "," : "").append(s);
                SharedPreferences.Editor e = lista.edit().putString("ids", b.toString()).remove("nome|" + id).remove("cor|" + id);
                if (id.equals(atual())) e.putString("atual", l.get(0));
                e.commit();
                a.getSharedPreferences(arquivo(id), Activity.MODE_PRIVATE).edit().clear().commit();
                sel = 0;
                modo = 1;
            } else if (back && first) modo = 1;
        } else if (modo == 2) {
            int r = tecla / 10, c = tecla % 10;
            if (left) c = (c + TECLAS[r].length() - 1) % TECLAS[r].length();
            else if (right) c = (c + 1) % TECLAS[r].length();
            else if (up) r = (r + TECLAS.length - 1) % TECLAS.length;
            else if (down) r = (r + 1) % TECLAS.length;
            c = Math.min(c, TECLAS[r].length() - 1);
            tecla = r * 10 + c;
            char ch = TECLAS[r].charAt(c);
            if (ok && first) {
                if (ch == '<') { if (nome.length() > 0) nome.setLength(nome.length() - 1); }
                else if (ch == '>') { if (nome.toString().trim().length() > 0) modo = 3; }
                else if (nome.length() < 12) nome.append(nome.length() == 0 ? ch : Character.toLowerCase(ch));
            } else if (back && first) {
                if (nome.length() > 0) nome.setLength(nome.length() - 1);
                else modo = 1;
            } else if (start && first && nome.toString().trim().length() > 0) modo = 3;
        } else if (modo == 3) {
            if (left) cor = (cor + CORES.length - 1) % CORES.length;
            else if (right) cor = (cor + 1) % CORES.length;
            else if (back && first) modo = 2;
            else if (ok && first) {
                int maior = 1;
                for (String s : l) try { maior = Math.max(maior, Integer.parseInt(s)); } catch (NumberFormatException ignored) { }
                String id = String.valueOf(maior + 1);
                lista.edit().putString("ids", lista.getString("ids", "1") + "," + id).putString("nome|" + id, nome.toString().trim())
                        .putInt("cor|" + id, cor).putString("atual", id).commit();
                fechar();
                escolha.escolheu(id);
                return;
            }
        }
        desenhar();
    }

    // ------------------------------------------------------------ ajudantes

    private int dp(float v) { return (int) (v * u + 0.5f); }

    private TextView texto(float size, int color, boolean bold) {
        TextView t = new TextView(a);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * u);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }
}
