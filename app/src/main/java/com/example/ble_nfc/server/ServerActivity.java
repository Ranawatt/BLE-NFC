package com.example.ble_nfc.server;

import androidx.appcompat.app.AppCompatActivity;

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

import com.example.ble_nfc.util.AES;
import com.example.ble_nfc.R;
import com.example.ble_nfc.client.ClientActivity;
import com.example.ble_nfc.service.HCEManager;

import static com.example.ble_nfc.util.Utils.READER_FLAGS;

public class ServerActivity extends AppCompatActivity implements View.OnClickListener {

    private static String secretKey = "60CA0C1AC45776EF7C42C958F2A009107348D0F3B858F32691B3EABF3DC5B2FF";

    private EditText etValue;
    private TextView tvEncrypt;
    private Button btnEncrypt, btnBle, btnNfc;
//    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    public HCEManager mHCEManager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        etValue = findViewById(R.id.et_value);
        tvEncrypt = findViewById(R.id.tv_encrypted);
        btnBle = findViewById(R.id.button_ble);
        btnNfc = findViewById(R.id.button_nfc);
        btnEncrypt = findViewById(R.id.button_encrypt);
        btnEncrypt.setOnClickListener(this);
        btnBle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_encrypt){
            String originalStr = etValue.getText().toString().trim();
            String encryptedString = AES.encrypt(originalStr, secretKey) ;
            tvEncrypt.setText(encryptedString);
        }
        if(v.getId() == R.id.button_ble){
            mHandler = new Handler();

            if (!getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.error_bluetooth_not_supported,
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            startActivity(new Intent(this, ClientActivity.class));
        }

        if (v.getId() == R.id.button_nfc){
            String data = tvEncrypt.getText().toString();
            enableReaderMode();
            if (data.isEmpty())
                Toast.makeText(this, "Encrypt the message to send", Toast.LENGTH_SHORT).show();
            mHCEManager.setData(data.getBytes());

            sendEncryptedData();
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
        if (nfc != null) {
            nfc.enableReaderMode(this, mHCEManager, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.disableReaderMode(this);
        }
    }
}