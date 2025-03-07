package com.alan.clients.util.verify;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HWIDUtils {
    public static String getHWID() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuilder s = new StringBuilder();
        String main = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
        byte[] bytes = main.getBytes("UTF-8");
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256"); // 使用 SHA-256 算法增加安全性
        byte[] hash = messageDigest.digest(bytes);

        //哈希值大神
        String hex = bytesToHex(hash).substring(0, 6);

        //添加前缀 Lavender
        s.append("Lavender");
        s.append(hex);
        return s.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
