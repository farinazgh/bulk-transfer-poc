package com.scor.bulktransfer.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class ChecksumUtil {
    public static String computeChecksum(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        InputStream fis = new FileInputStream(filePath);

        byte[] byteArray = new byte[1024];
        int bytesCount;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
