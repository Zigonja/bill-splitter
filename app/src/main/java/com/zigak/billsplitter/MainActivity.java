package com.zigak.billsplitter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.notbytes.barcode_reader.BarcodeReaderActivity;

public class MainActivity extends AppCompatActivity {
    private static final int BARCODE_READER_ACTIVITY_REQUEST = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout qrCodeButton      = findViewById(R.id.qrCodeButton);
        LinearLayout manualEntryButton = findViewById(R.id.manualEntryButton);

        manualEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(MainActivity.this, ManualEntry.class);
                startActivity(launchIntent);
            }
        });

        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent launchIntent = BarcodeReaderActivity
                        .getLaunchIntent(MainActivity.this, true, false);
                startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "Napaka pri skeniranju QR kode", Toast.LENGTH_SHORT).show();

            return;
        }

        if (requestCode == BARCODE_READER_ACTIVITY_REQUEST && data != null) {
            Barcode qrCode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);

            Intent qrCodeSplitterIntent = new Intent(this, QRSplitterActivity.class);
            qrCodeSplitterIntent.putExtra("qrData", qrCode.rawValue);

            startActivity(qrCodeSplitterIntent);
        }
    }
}
