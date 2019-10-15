package com.zigak.billsplitter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRSplitterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrsplitter);

        final TextView purposeCodeText    = findViewById(R.id.purposeCode);
        final TextView paymentPurposeText = findViewById(R.id.purposePayment);
        final TextView dueDateText        = findViewById(R.id.dueDate);
        final TextView IBANText           = findViewById(R.id.IBAN);
        final TextView referenceText      = findViewById(R.id.reference);
        final TextView nameText           = findViewById(R.id.name);
        final TextView addressText        = findViewById(R.id.addressAndNumber);
        final TextView placeText          = findViewById(R.id.place);
        final TextView amountText         = findViewById(R.id.amount);
        final EditText splitNumberText    = findViewById(R.id.splitNumber);
        final LinearLayout qrCodesContainer = findViewById(R.id.qrCodesContainer);
        final String qrData = getIntent().getStringExtra("qrData");

        splitNumberText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String[] UPNValues = qrData.split("\\r?\\n");

                int splitNumber = Integer.parseInt(splitNumberText.getText().toString().length() > 0 ? splitNumberText.getText().toString() : "1");

                UPNValues[8] = (Integer.parseInt(UPNValues[8]) / splitNumber) + "";

                String splitQrData = "";

                for (String UPNValue : UPNValues) {
                    splitQrData += UPNValue + '\n';
                }

                QRGEncoder qrgEncoder = new QRGEncoder(splitQrData, null, QRGContents.Type.TEXT, 500);

                try {
                    ImageView qrCodeView = new ImageView(QRSplitterActivity.this);
                    qrCodeView.setImageBitmap(qrgEncoder.encodeAsBitmap());
                    qrCodeView.setLayoutParams(
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            )
                    );

                    qrCodesContainer.removeAllViews();
                    qrCodesContainer.addView(qrCodeView);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(QRSplitterActivity.this, "Nekaj je šlo narobe!", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        });

        String[] UPNValues = qrData.split("\\r?\\n");

        purposeCodeText.setText(UPNValues[11].length() > 0 ? UPNValues[11] : "/");
        paymentPurposeText.setText(UPNValues[12].length() > 0 ? UPNValues[12] : "/");
        dueDateText.setText(UPNValues[13].length() > 0 ? UPNValues[13] : "/");
        IBANText.setText(UPNValues[14].length() > 0 ? UPNValues[14] : "/");
        referenceText.setText(UPNValues[15].length() > 0 ? UPNValues[15] : "/");
        nameText.setText(UPNValues[16].length() > 0 ? UPNValues[16] : "/");
        addressText.setText(UPNValues[17].length() > 0 ? UPNValues[17] : "/");
        placeText.setText(UPNValues[18].length() > 0 ? UPNValues[18] : "/");
        amountText.setText(String.format("%.2f", (Float.parseFloat(UPNValues[8]) / 100)) + " €");

        int splitNumber = Integer.parseInt(splitNumberText.getText().toString().length() > 0 ? splitNumberText.getText().toString() : "1");

        UPNValues[8] = (Integer.parseInt(UPNValues[8]) / splitNumber) + "";

        String splitQrData = "";

        for (String UPNValue : UPNValues) {
            splitQrData += UPNValue + '\n';
        }

        QRGEncoder qrgEncoder = new QRGEncoder(splitQrData, null, QRGContents.Type.TEXT, 500);

        try {
            ImageView qrCodeView = new ImageView(this);
            qrCodeView.setImageBitmap(qrgEncoder.encodeAsBitmap());
            qrCodeView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );

            qrCodesContainer.addView(qrCodeView);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Nekaj je šlo narobe!", Toast.LENGTH_SHORT).show();

            finish();
        }
    }
}
