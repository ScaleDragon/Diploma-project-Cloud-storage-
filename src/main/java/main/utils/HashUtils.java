package main.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    /**
     * Хеширует пароль алгоритмом SHA-512 с солью.
     * @param login логин
     * @param password пароль
     * @return захешированный пароль, в байтах
     * @throws NoSuchAlgorithmException при внутренней ошибке хеширования пароля
     */
    public static byte[] hash(String login, String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(login.getBytes());
        md.update("-|**|-".getBytes());
        md.update(password.getBytes());
        return md.digest();
    }
}
