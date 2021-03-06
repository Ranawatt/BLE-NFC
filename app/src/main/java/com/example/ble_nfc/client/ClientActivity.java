 package com.example.ble_nfc.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_nfc.BLE_NFC;
import com.example.ble_nfc.fragment.ScannerFragment;
import com.example.ble_nfc.server.ServerActivity;
import com.example.ble_nfc.util.AES;
import com.example.ble_nfc.R;
import com.example.ble_nfc.util.Constant;

import static com.example.ble_nfc.util.Constant.REQUEST_ENABLE_BT;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener, ScannerFragment.OnReceivedText {

    private TextView tvEncrypt, tvDecrypt;
    private BluetoothAdapter mBluetoothAdapter;

    ServiceReceiver serviceReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        setTitle(R.string.scanner);

        tvEncrypt = findViewById(R.id.text_encrypted);
        tvDecrypt = findViewById(R.id.text_decrypted);
        initListener();
    }

    private void initListener() {
        findViewById(R.id.btnBle).setOnClickListener(this);
        findViewById(R.id.btnNfc).setOnClickListener(this);
        findViewById(R.id.btnAdvertise).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnBle){

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
                        setUpScanning();
                    }
                }else{
                    // Prompt user to turn on Bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }else{
                Toast.makeText(this, R.string.error_bluetooth_not_supported,
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String encryptedString = tvEncrypt.getText().toString();
            String decryptedString = AES.decrypt(encryptedString, Constant.secretKey);
            tvDecrypt.setText(decryptedString);
        }
        if (v.getId() == R.id.btnNfc){
            serviceReceiver = new ServiceReceiver();
            registerReceiver(serviceReceiver, new IntentFilter(Constant.ACTION_SERVICE_STATUS));
        }
        if (v.getId() == R.id.btnAdvertise){
            startActivity(new Intent(this, ServerActivity.class));
        }
    }

    private void setUpScanning() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ScannerFragment scannerFragment = new ScannerFragment();
        // Fragments can't access system services directly, so pass it the BluetoothAdapter
        scannerFragment.setBluetoothAdapter(mBluetoothAdapter);
        transaction.replace(R.id.scanner_container,scannerFragment);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        BLE_NFC.startHCEService("STOP");
        if (serviceReceiver!= null){
            unregisterReceiver(serviceReceiver);
            serviceReceiver = null;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onReceivedText(String receivedText) {
        Log.d("Received Data  ",receivedText);
        tvEncrypt.setText(receivedText);
    }

    private class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            int cmd = intent.getIntExtra("cmd", -1);
            switch (cmd) {
                case 0: // Received AID
                    tvEncrypt.setVisibility(View.GONE);
                    tvDecrypt.setText("");
                    break;
                case 1: // Start receiving
                    tvEncrypt.setVisibility(View.VISIBLE);
                    break;
                case 2: // End Receiving
                    tvEncrypt.setVisibility(View.GONE);
                    String msg = intent.getStringExtra("data");
                    tvDecrypt.setText(msg);
                    break;
                case 3: // Error
                    tvEncrypt.setVisibility(View.GONE);
                    tvDecrypt.setText("Something went wrong. Please try again.");
                    break;
            }
        }
    }
}