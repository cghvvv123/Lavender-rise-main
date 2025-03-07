package com.alan.clients.domcer.utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IOUtils {
    public static byte[] readByteArray(InputStream inputStream) throws IOException {
        byte[] byArray = new byte[IOUtils.readInt(inputStream)];
        inputStream.read(byArray);
        return byArray;
    }

    public static int readInt(InputStream inputStream) throws IOException {
        return ((byte)inputStream.read() & 0xFF) << 24 | ((byte)inputStream.read() & 0xFF) << 16 | ((byte)inputStream.read() & 0xFF) << 8 | (byte)inputStream.read() & 0xFF;
    }

    public static void writeFloat(OutputStream outputStream, float f) throws IOException {
        IOUtils.writeInt(outputStream, Float.floatToIntBits(f));
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        byte[] byArray = new byte[inputStream.available()];
        byte[] byArray2 = new byte[]{};
        int n = 0;
        while ((n = inputStream.read(byArray)) != -1) {
            byte[] byArray3 = new byte[n];
            System.arraycopy(byArray, 0, byArray3, 0, n);
            byArray2 = IOUtils.mergeArray(byArray2, byArray3);
        }
        return byArray2;
    }

    public static String readString(InputStream inputStream) throws IOException {
        int n = IOUtils.readInt(inputStream);
        if (n < 0) {
            return null;
        }
        if (n == 0) {
            return "";
        }
        byte[] byArray = new byte[n];
        inputStream.read(byArray);
        return new String(byArray, StandardCharsets.UTF_8);
    }

    public static byte[] mergeArray(byte[] ... byArray) {
        int n = 0;
        int n2 = 0;
        for (int i = 0; i < byArray.length; ++i) {
            n2 += byArray[i].length;
        }
        byte[] byArray2 = new byte[n2];
        for (int i = 0; i < byArray.length; ++i) {
            int n3 = byArray[i].length;
            if (n3 == 0) continue;
            System.arraycopy(byArray[i], 0, byArray2, n, n3);
            n += n3;
        }
        return byArray2;
    }

    public static float readFloat(InputStream inputStream) throws IOException {
        return Float.intBitsToFloat(IOUtils.readInt(inputStream));
    }

    public static void writeByteArray(OutputStream outputStream, byte[] byArray) throws IOException {
        IOUtils.writeInt(outputStream, byArray.length);
        outputStream.write(byArray);
    }

    public static void writeString(OutputStream outputStream, String string) throws IOException {
        if (string == null) {
            IOUtils.writeInt(outputStream, -1);
        } else if (string.isEmpty()) {
            IOUtils.writeInt(outputStream, 0);
        } else {
            byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
            IOUtils.writeInt(outputStream, byArray.length);
            outputStream.write(byArray);
        }
    }

    public static void writeInt(OutputStream outputStream, int n) throws IOException {
        outputStream.write((byte)(n >> 24));
        outputStream.write((byte)(n >> 16));
        outputStream.write((byte)(n >> 8));
        outputStream.write(n);
    }
}

