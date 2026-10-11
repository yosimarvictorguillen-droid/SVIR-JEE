package com.svir.jee.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Limita los intentos fallidos de verificacion (por ejemplo al vincular los pedidos de un invitado), para que no se
 * pueda adivinar un numero de pedido probando uno tras otro. Maximo 5 fallos cada 15 minutos por clave.
 */
public final class LimiteIntentos {

    private static final int MAXIMO = 5;
    private static final long VENTANA_MS = 15 * 60 * 1000L;
    private static final Map<String, long[]> INTENTOS = new HashMap<>(); // clave -> {fallos, inicio de la ventana}

    private LimiteIntentos() {
    }

    public static synchronized boolean bloqueado(String clave) {
        long[] datos = INTENTOS.get(clave);
        if (datos == null) {
            return false;
        }
        if (System.currentTimeMillis() - datos[1] > VENTANA_MS) {
            INTENTOS.remove(clave);
            return false;
        }
        return datos[0] >= MAXIMO;
    }

    public static synchronized void fallo(String clave) {
        long ahora = System.currentTimeMillis();
        long[] datos = INTENTOS.get(clave);
        if (datos == null || ahora - datos[1] > VENTANA_MS) {
            INTENTOS.put(clave, new long[]{1, ahora});
        } else {
            datos[0]++;
        }
    }

    public static synchronized void limpiar(String clave) {
        INTENTOS.remove(clave);
    }
}
