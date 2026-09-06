package com.svir.jee.util;

import org.mindrot.jbcrypt.BCrypt;

/** Utilidades de hash/verificacion de contrasenas con BCrypt. */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt());
    }

    public static boolean verificar(String passwordPlano, String hash) {
        if (passwordPlano == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(passwordPlano, hash);
    }
}
