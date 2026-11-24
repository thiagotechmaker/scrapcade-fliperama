package com.thiago.launcher;

import java.io.FileOutputStream;

/**
 * Bips do menu pelo buzzer da placa, do mesmo jeito que o app de fabrica da Boditech: um pacote para o MSP430 pela
 * /dev/s3c2410_serial0 a 115200 (SerialPortActivity.makeDiagPacketData_MISC, comando 130). Nao usa o chip de som,
 * que reinicia o aparelho. Pacote: 82 01 01 04 00 02 <toque 0-3> <volume 1-10> <CRC8> 7E.
 */
final class Bip {
    static final int NAVEGAR = 0, ABRIR = 1, ERRO = 3;
    /** Volume escolhido no painel: 0 desliga. */
    static int volume = 2;

    private static final String PORTA = "/dev/s3c2410_serial0";
    private static boolean portaPronta;
    private static long ultimoNavegar;
    private static final Object trava = new Object();
    private static int pendente = -1;
    private static Thread fio;

    private Bip() { }

    static void navegar() {
        long agora = System.currentTimeMillis();
        if (agora - ultimoNavegar < 90) return;   // segurando o direcional: um bip a cada 90 ms, no maximo
        ultimoNavegar = agora;
        toca(NAVEGAR);
    }

    static void abrir() { toca(ABRIR); }

    static void erro() { toca(ERRO); }

    /** Toca um toque com o volume do painel, sem travar a tela (uma thread so, que pega o ultimo pedido). */
    static void toca(int toque) {
        if (volume <= 0) return;
        synchronized (trava) {
            pendente = toque;
            if (fio == null) {
                fio = new Thread(new Runnable() { public void run() { laco(); } }, "bip");
                fio.setDaemon(true);
                fio.start();
            }
            trava.notify();
        }
    }

    private static void laco() {
        while (true) {
            int t;
            synchronized (trava) {
                while (pendente < 0) {
                    try { trava.wait(); } catch (InterruptedException e) { return; }
                }
                t = pendente;
                pendente = -1;
            }
            envia(t, Math.max(1, Math.min(10, volume)));
        }
    }

    private static void envia(int toque, int vol) {
        try {
            if (!portaPronta) {   // a porta liga a 9600; o MSP430 fala a 115200 (o /bin/stty e do busybox do sistema)
                Process p = Runtime.getRuntime().exec(new String[]{"/bin/stty", "-F", PORTA, "115200", "raw", "-echo"});
                p.waitFor();
                portaPronta = true;
            }
            byte[] b = {(byte) 0x82, 1, 1, 4, 0, 2, (byte) toque, (byte) vol, 0, 0x7E};
            b[8] = crc8(b, 8);
            FileOutputStream o = new FileOutputStream(PORTA);
            try { o.write(b); } finally { o.close(); }
        } catch (Exception e) {
            // sem a porta (outro aparelho, permissao): o menu segue mudo, como antes
            android.util.Log.w("ScrapCadeBip", "bip nao saiu: " + e);
        }
    }

    /** CRC8 do app de fabrica: polinomio 0x07, inicio 0, XOR final 0x55. */
    private static byte crc8(byte[] d, int n) {
        int crc = 0;
        for (int i = 0; i < n; i++) {
            crc ^= d[i] & 0xFF;
            for (int k = 0; k < 8; k++) crc = (crc & 0x80) != 0 ? ((crc << 1) ^ 0x07) & 0xFF : (crc << 1) & 0xFF;
        }
        return (byte) (crc ^ 0x55);
    }
}
