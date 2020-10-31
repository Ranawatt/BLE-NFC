package com.example.ble_nfc.server;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_nfc.BLE_NFC;
import com.example.ble_nfc.fragment.AdvertiserFragment;
import com.example.ble_nfc.service.AdvertiserService;
import com.example.ble_nfc.util.AES;
import com.example.ble_nfc.R;
import com.example.ble_nfc.client.ClientActivity;
import com.example.ble_nfc.service.HCEManager;
import com.example.ble_nfc.util.Constant;

import static com.example.ble_nfc.util.Utils.READER_FLAGS;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etValue;
    private TextView tvEncrypt;
    private BluetoothAdapter mBluetoothAdapter;
    public HCEManager mHCEManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        setTitle(R.string.advertiser);
        initUi();
        initListener();
    }
    private void initUi() {
        etValue = findViewById(R.id.et_value);
        tvEncrypt = findViewById(R.id.tv_encrypted);
    }
    private void initListener() {
        findViewById(R.id.button_encrypt).setOnClickListener(this);
        findViewById(R.id.button_ble).setOnClickListener(this);
        findViewById(R.id.button_nfc).setOnClickListener(this);
        findViewById(R.id.button_scan).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_encrypt){
            if (etValue.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.encrypt_text_enter, Toast.LENGTH_LONG).show();
                return;
            }
            String originalStr = etValue.getText().toString().trim();
            String encryptedString = AES.encrypt(originalStr, Constant.secretKey) ;
            tvEncrypt.setText(encryptedString);
            Constant.ENCRYPTED_STRING = encryptedString;
        }
        if(v.getId() == R.id.button_ble){
            if (tvEncrypt.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.encrypt_first, Toast.LENGTH_LONG).show();
                return;
            }
            if (!getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()){
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()){
                        setUpAdvertising();
                    }
                }else{
                    // Prompt user to turn on Bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constant.REQUEST_ENABLE_BT);
                }
            }else{
                Toast.makeText(this, R.string.error_bluetooth_not_supported,
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        if (v.getId() == R.id.button_nfc){
            if (tvEncrypt.getText().toString().isEmpty()){
                Toast.makeText(this,R.string.encrypt_first,Toast.LENGTH_LONG).show();
                return;
            }else{
                String data = tvEncrypt.getText().toString();
                sendEncryptedData();
                enableReaderMode();
                if (data.isEmpty())
                    Toast.makeText(this, R.string.encrypt_msg, Toast.LENGTH_SHORT).show();
                mHCEManager.setData(data.getBytes());
            }
        }
        if (v.getId() == R.id.button_scan)
            startActivity(new Intent(this, ClientActivity.class));
    }
    private void setUpAdvertising() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AdvertiserFragment advertiserFragment = new AdvertiserFragment();
        transaction.replace(R.id.advertiser_container, advertiserFragment);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {
                    // Bluetooth is now Enabled, are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                        setUpAdvertising();
                    } else {
                        // Bluetooth Advertisements are not supported.
                        Toast.makeText(this,R.string.bt_ads_not_supported,Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendEncryptedData() {
        mHCEManager = new HCEManager(new HCEManager.HCEListener() {
            @Override
            public void onDataSent() {
                runOnUiThread(()-> {
                    Toast.makeText(getApplicationContext(), "Data sent successfully!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFail(final int errorCode) {
                runOnUiThread(()-> {
                    switch (errorCode) {
                        case HCEManager.HCE_ERROR_NOT_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Error to connect emulated card", Toast.LENGTH_SHORT).show();
                            break;
                        case HCEManager.HCE_ERROR_FORMAT_DATA:
                            Toast.makeText(getApplicationContext(), "Error to format data", Toast.LENGTH_SHORT).show();
                            break;
                        case HCEManager.HCE_ERROR_SEND_DATA:
                            Toast.makeText(getApplicationContext(), "Error to send data", Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        enableReaderMode();
    }

    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    private void enableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null)
            nfc.enableReaderMode(this, mHCEManager, READER_FLAGS, null);
    }

    private void disableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null)
            nfc.disableReaderMode(this);
    }
}