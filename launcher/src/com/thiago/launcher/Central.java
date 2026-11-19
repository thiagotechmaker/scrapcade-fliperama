package com.thiago.launcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Central de downloads e copias (como a do ScrapCade OS): o que esta chegando no cartao, com o progresso de cada um.
 * A copia do pen drive e os envios pela pagina do PC (acima de 2 MB) avisam aqui; o painel "Downloads e copias" le.
 */
final class Central {
    static final int ANDANDO = 0, PRONTO = 1, FALHOU = 2;

    static final class Item {
        int id, estado;
        String titulo, detalhe = "";
        long feito, total, quando;
    }

    private static final List<Item> itens = new ArrayList<Item>();
    private static int proximo = 1;

    private Central() { }

    static synchronized int inicia(String titulo, long total) {
        Item i = new Item();
        i.id = proximo++;
        i.titulo = titulo;
        i.total = total;
        i.quando = System.currentTimeMillis();
        itens.add(0, i);
        while (itens.size() > 8) itens.remove(itens.size() - 1);   // guarda so os ultimos
        return i.id;
    }

    static synchronized void avanca(int id, long feito, String detalhe) {
        Item i = acha(id);
        if (i == null) return;
        i.feito = feito;
        if (detalhe != null) i.detalhe = detalhe;
    }

    static synchronized void termina(int id, boolean ok, String detalhe) {
        Item i = acha(id);
        if (i == null) return;
        i.estado = ok ? PRONTO : FALHOU;
        if (ok) i.feito = i.total;
        if (detalhe != null) i.detalhe = detalhe;
    }

    static synchronized boolean andando() {
        for (Item i : itens) if (i.estado == ANDANDO) return true;
        return false;
    }

    private static Item acha(int id) {
        for (Item i : itens) if (i.id == id) return i;
        return null;
    }

    private static String mb(long b) {
        return b >= 1073741824L ? String.format(java.util.Locale.US, "%.1f GB", b / 1073741824.0)
                : (b / 1048576) + " MB";
    }

    /** Texto do painel: uma barra por item (o painel do fliperama e texto). */
    static synchronized String texto() {
        if (itens.isEmpty())
            return "Nada chegando agora.\n\nAqui aparecem a cópia do pen drive e os jogos enviados pela página do PC, "
                    + "com o progresso de cada um.";
        StringBuilder s = new StringBuilder();
        for (Item i : itens) {
            int pct = i.total > 0 ? (int) Math.min(100, i.feito * 100 / i.total) : (i.estado == PRONTO ? 100 : 0);
            // so caracteres que a fonte do Android 2.3 tem (o ✗ e os blocos da barra viravam quadrado)
            String marca = i.estado == PRONTO ? "√ " : i.estado == FALHOU ? "× " : "» ";
            s.append(marca).append(i.titulo).append('\n');
            if (i.estado == ANDANDO) {
                int n = pct / 5;
                StringBuilder barra = new StringBuilder();
                for (int k = 0; k < 20; k++) barra.append(k < n ? '#' : '·');
                s.append("   [").append(barra).append("]").append("  ").append(pct).append("%   ")
                        .append(mb(i.feito)).append(" de ").append(mb(i.total)).append('\n');
            } else {
                s.append("   ").append(i.estado == PRONTO ? "Pronto" : "Não terminou").append("   ").append(mb(i.total)).append('\n');
            }
            if (i.detalhe.length() > 0) s.append("   ").append(i.detalhe).append('\n');
            s.append('\n');
        }
        return s.toString().trim();
    }
}
