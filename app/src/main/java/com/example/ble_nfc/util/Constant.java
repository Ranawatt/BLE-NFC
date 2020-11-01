package com.example.ble_nfc.util;

import android.nfc.NfcAdapter;
import android.os.ParcelUuid;

public class Constant {
    public static final String ACTION_SERVICE_STATUS = "service_status";


    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("0000b81d-0000-1000-8000-00805f9b34fb");

    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    public static String secretKey = "60CA0C1AC45776EF7C42C958F2A009107348D0F3B858F32691B3EABF3DC5B2FF";
    public static final int REQUEST_ENABLE_BT = 1;

    public static String ENCRYPTED_STRING = "";
}
