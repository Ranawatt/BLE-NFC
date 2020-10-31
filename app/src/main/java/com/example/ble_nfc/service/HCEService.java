package com.example.ble_nfc.service;


import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.example.ble_nfc.util.ByteUtils;
import com.example.ble_nfc.util.Constant;

import java.nio.charset.StandardCharsets;

public class HCEService extends HostApduService {

    private static final String TAG = "HCEService";

    public static final String APDU_WRITE_DATA_HEADER = "00A40400";
    public static final String APDU_WRITE_START_CMD = "00000000";
    public static final String APDU_WRITE_END_CMD = "FFFFFFFF";

    private final byte[] STATUS_SUCCESS = {
            (byte) 0x90, (byte) 0x00
    };

    private final byte[] STATUS_FAILED = {
            (byte) 0x6F, (byte) 0x00
    };

    private static final int MIN_APDU_LENGTH = 4;

    private static final int APDU_CONNECT = 0;
    private static final int APDU_START_TO_WRITE = 1;
    private static final int APDU_END_TO_WRITE = 2;
    private static final int APDU_ERROR = 3;

    public long dataSize;
    public String dataMessage;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand!");

        String cmd = "";
        if (intent != null) cmd = intent.getStringExtra("cmd");

        if (cmd.equals("STOP")) {
            Log.e(TAG, "onStopSelf!");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (commandApdu == null) {
            return STATUS_FAILED;
        }
        // Invalid APDU command
        if (commandApdu.length < MIN_APDU_LENGTH) {
            return STATUS_FAILED;
        }
        String headers = ByteUtils.GetHeaderFromAPDU(commandApdu);
        // Check INS
        switch (headers) {
            // SELECT AID
            case APDU_WRITE_DATA_HEADER:
                dataSize = 0;
                dataMessage = "";
                sendMessage(APDU_CONNECT);
                return STATUS_SUCCESS;

            // Start to receive data
            case APDU_WRITE_START_CMD:
                Log.e(TAG, ByteUtils.ByteArrayToHexString(commandApdu));
                dataSize = ByteUtils.GetDataSize(commandApdu);
                Log.e(TAG, "Size: " + dataSize);
                if (dataSize > 0) {
                    sendMessage(APDU_START_TO_WRITE);
                    return STATUS_SUCCESS;
                } else {
                    sendMessage(APDU_ERROR);
                    return STATUS_FAILED;
                }
                // End to receive data
            case APDU_WRITE_END_CMD:
                Log.e(TAG, "Received Size: " + dataMessage.length());
                byte[] data = ByteUtils.HexStringToByteArray(dataMessage);
                if (dataSize == data.length) {
                    dataMessage = new String(data, StandardCharsets.UTF_8);
                    sendMessage(APDU_END_TO_WRITE);
                    return STATUS_SUCCESS;
                } else {
                    sendMessage(APDU_ERROR);
                    return STATUS_FAILED;
                }

                // Write data
            default:
                Log.e(TAG, ByteUtils.ByteArrayToHexString(commandApdu));
                Log.e(TAG, ByteUtils.GetDataFromAPDU(commandApdu));
                dataMessage += ByteUtils.GetDataFromAPDU(commandApdu);
                return STATUS_SUCCESS;
        }
    }

    @Override
    public void onDeactivated(int reason) {
        Log.e(TAG, "onDeactivated... Reason: " + reason);
    }

    private void sendMessage(int cmd) {
        Intent intentBroadcast = new Intent(Constant.ACTION_SERVICE_STATUS);
        intentBroadcast.putExtra("cmd", cmd);
        intentBroadcast.putExtra("data", dataMessage);
        sendBroadcast(intentBroadcast);
    }
}
