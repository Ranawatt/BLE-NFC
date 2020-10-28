package com.example.ble_nfc.util;

import android.os.ParcelUuid;

public class Constant {
    public static final String ACTION_SERVICE_STATUS = "service_status";


    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("0000b81d-0000-1000-8000-00805f9b34fb");

    public static final int REQUEST_ENABLE_BT = 1;
}
