package com.example.ble_nfc.service;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import com.example.ble_nfc.util.ByteUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Callback class, invoked when an NFC card is scanned while the device is running in reader mode.
 * Reader mode can be invoked by calling NfcAdapter
 */
public class HCEManager implements NfcAdapter.ReaderCallback {

    private static final String TAG = "HceManager";

    private static final String APDU_SELECT_AID = "00A4040000123456789ABCDEF00";

    public static final int HCE_ERROR_NOT_CONNECTED = 1;
    public static final int HCE_ERROR_FORMAT_DATA = 2;
    public static final int HCE_ERROR_SEND_DATA = 3;

    private static final int TIMEOUT = 5000; // NFC tag timing timeout

    private HCEListener mListener;
    private List<byte[]> mCommandArray;

    public interface HCEListener {
        void onDataSent();
        void onFail(int errorCode);
    }

    public HCEManager(HCEListener HCEListener) {
        mListener = HCEListener;
        mCommandArray = new ArrayList<>();
    }

    public void setData(byte[] data) {
        mCommandArray.clear();
        // Preparing data
        if (data == null || data.length == 0) {
            mListener.onFail(HCE_ERROR_FORMAT_DATA);
            return;
        }
        int dataLength = data.length;

        /**
         * 00 00 00 00 | 00 00 XX XX (data length)
         */
        byte[] command = ByteUtils.HexStringToByteArray(ByteUtils.IntToHexString(0) + ByteUtils.IntToHexString(dataLength));
        mCommandArray.add(command);

        int start = 0;
        int chunkSize = 100; // Split by 100
        int count = (int) Math.ceil(dataLength / (double) chunkSize);
        for (int i = 0; i < count; i++) {
            if (start + chunkSize > dataLength) {
                byte[] ret = new byte[dataLength - start];
                System.arraycopy(data, start, ret, 0, dataLength - start);
                mCommandArray.add(ByteUtils.ByteArrayAdd(ByteUtils.HexStringToByteArray(ByteUtils.IntToHexString(i + 1)), ret));
            } else {
                byte[] ret = new byte[chunkSize];
                System.arraycopy(data, start, ret, 0, chunkSize);
                mCommandArray.add(ByteUtils.ByteArrayAdd(ByteUtils.HexStringToByteArray(ByteUtils.IntToHexString(i + 1)), ret));
            }
            start += chunkSize;
        }
    }
    /**
     * Callback when a new tag is discovered by the system.
     * Communication with the card should take place here.
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        LogI("New tag discovered");

        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            LogI("Not IsoDep tag");
            mListener.onFail(HCE_ERROR_NOT_CONNECTED);
            return;
        }
        if (mCommandArray.size() == 0) {
            LogI("Preparing data failed");
            return;
        }

        try {
            // Connect to the remote NFC device
            isoDep.connect();
            isoDep.setTimeout(TIMEOUT);
            // Send SELECT AID command to remote device
            LogI("Connecting AID");
            LogI("Sending: " + APDU_SELECT_AID);
            byte[] result = isoDep.transceive(ByteUtils.HexStringToByteArray(APDU_SELECT_AID));
            LogI("Response: " + ByteUtils.ByteArrayToHexString(result));

            // Check connect result
            if (isInvalidHCEResult(result)) {
                LogI("Connection Failed");
                mListener.onFail(HCE_ERROR_NOT_CONNECTED);
                return;
            }
            // Check data
            if (mCommandArray.size() == 0) {
                LogI("Preparing data failed");
                mListener.onFail(HCE_ERROR_FORMAT_DATA);
                return;
            }
            // Send data
            for (byte[] item : mCommandArray) {
                // Send command to remote device
                result = isoDep.transceive(item);
                // Result
                LogI("Response: " + ByteUtils.ByteArrayToHexString(result));
                if (isInvalidHCEResult(result)) {
                    LogI("Send data failed");
                    mListener.onFail(HCE_ERROR_SEND_DATA);
                    return;
                }
            }

            result = isoDep.transceive(ByteUtils.HexStringToByteArray("FFFFFFFF"));
            if (isInvalidHCEResult(result)) {
                LogI("Send data failed");
                mListener.onFail(HCE_ERROR_SEND_DATA);
                return;
            }
            // Data sent successfully!
            LogI("Data sent successful!");
            mListener.onDataSent();
        } catch (IOException e) {
            e.printStackTrace();
            LogI("Connection Failed");
            mListener.onFail(HCE_ERROR_NOT_CONNECTED);
        }
    }

    private boolean isInvalidHCEResult(byte[] bytes) {
        if (bytes == null || bytes.length != 2) {
            return true;
        }
        return !Arrays.equals(bytes, new byte[]{(byte) 0x90, (byte) 0x00});
    }
    private void LogI(String msg) {
        Log.i(TAG, msg);
    }
}
