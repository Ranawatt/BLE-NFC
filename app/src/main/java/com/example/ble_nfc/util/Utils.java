package com.example.ble_nfc.util;

import android.nfc.NfcAdapter;

public class Utils {


    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];
        int j = 0;
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[j++] = hexArray[v >>> 4];
            hexChars[j++] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toHex(byte[] bytes, char delimiter) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 3 - 1];
        int j = 0;
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[j++] = hexArray[v >>> 4];
            hexChars[j++] = hexArray[v & 0x0F];
            if (i < (bytes.length - 1)) {
                hexChars[j++] = delimiter;
            }
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String paramString) throws IllegalArgumentException {
        int j = paramString.length();

        if (j % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }

        byte[] arrayOfByte = new byte[j / 2];
        int hiNibble, loNibble;

        for (int i = 0; i < j; i += 2) {
            hiNibble = Character.digit(paramString.charAt(i), 16);
            loNibble = Character.digit(paramString.charAt(i + 1), 16);
            if (hiNibble < 0) {
                throw new IllegalArgumentException("Illegal hex digit at position " + i);
            }
            if (loNibble < 0) {
                throw new IllegalArgumentException("Illegal hex digit at position " + (i + 1));
            }
            arrayOfByte[(i / 2)] = ((byte) ((hiNibble << 4) + loNibble));
        }
        return arrayOfByte;
    }

    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }


}