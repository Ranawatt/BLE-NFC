package com.example.ble_nfc;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.ble_nfc.service.HCEService;

public class BLE_NFC extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static void startHCEService(String cmd) {
        Intent intent = new Intent(mContext, HCEService.class);
        intent.putExtra("cmd", cmd);
        mContext.startService(intent);
    }
}
