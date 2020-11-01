package com.example.ble_nfc.util;

public class ByteUtils {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    /**
     * Utility class to convert a byte array to a hexadecimal string.
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation. */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    /**
     * Utility class to convert a hexadecimal string to a byte string.
     * Behavior with input strings containing non-hexadecimal characters is undefined.
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input */
    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String IntToHexString(int i) {
        return String.format("%08X", i);
    }

    public static byte[] ByteArrayAdd(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static String GetHeaderFromAPDU(byte[] data) {
        String hexData = ByteArrayToHexString(data);
        return hexData.substring(0, 8);
    }

    public static long GetDataSize(byte[] dat) {
        if (dat.length != 8) return 0;
        return ((long) ((dat[4] & 0xFF) << 24)) + ((long) ((dat[5] & 0xFF) << 16)) + ((long) ((dat[6] & 0xFF) << 8)) + ((long) dat[7] & 0xFF);
    }

    public static String GetDataFromAPDU(byte[] data) {
        String hexData = ByteArrayToHexString(data);
        return hexData.substring(8, data.length * 2);
    }
}
