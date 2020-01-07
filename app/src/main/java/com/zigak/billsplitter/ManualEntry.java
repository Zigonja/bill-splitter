package com.zigak.billsplitter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ManualEntry extends AppCompatActivity {

    EditText purposeCodeText;
    EditText paymentPurposeText;
    EditText dueDateText;
    EditText IBANText;
    EditText referenceText;
    EditText nameText;
    EditText addressText;
    EditText placeText;
    EditText amountText;
    EditText splitNumberText;
    LinearLayout qrCodesContainer;
    ImageView qrCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        Button generateBtn = findViewById(R.id.generateBtn);
        purposeCodeText      = findViewById(R.id.purposeCode);
        paymentPurposeText   = findViewById(R.id.purposePayment);
        dueDateText          = findViewById(R.id.dueDate);
        IBANText             = findViewById(R.id.IBAN);
        referenceText        = findViewById(R.id.reference);
        nameText             = findViewById(R.id.name);
        addressText          = findViewById(R.id.addressAndNumber);
        placeText            = findViewById(R.id.place);
        amountText           = findViewById(R.id.amount);
        splitNumberText      = findViewById(R.id.splitNumber);
        qrCodesContainer     = findViewById(R.id.qrCodesContainer);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ImageView shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = ((BitmapDrawable) qrCodeView.getDrawable()).getBitmap();

                if (bitmap == null) {
                    Toast.makeText(
                            ManualEntry.this,
                            "Nekaj je šlo narobe",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }
                final File dir =
                        new File(Environment.getExternalStorageDirectory(), "qrCodeSplitter");

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                final File img = new File(dir, System.currentTimeMillis() + ".png");

                if (img.exists()) {
                    img.delete();
                }

                try {
                    final OutputStream outStream = new FileOutputStream(img);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(
                            Intent.EXTRA_STREAM,
                            Uri.fromFile(img)
                    );
                    startActivity(Intent.createChooser(share, "Deli kodo"));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                    Toast.makeText(
                            ManualEntry.this,
                            "Nekaj je šlo narobe",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateParams()) {
                    String[] qrData = {
                            "UPNQR",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ((int) (Double.parseDouble(amountText.getText().toString()) * 100)) + "",
                            "",
                            "",
                            purposeCodeText.getText().toString(),
                            paymentPurposeText.getText().toString(),
                            dueDateText.getText().toString(),
                            IBANText.getText().toString(),
                            referenceText.getText().toString(),
                            nameText.getText().toString(),
                            addressText.getText().toString(),
                            placeText.getText().toString(),
                            "204"
                    };

                    String splitQrData = "";

                    for (String UPNValue : qrData) {
                        splitQrData += UPNValue.trim() + '\n';
                    }

                    final QRGEncoder qrgEncoder = new QRGEncoder(
                            splitQrData,
                            null,
                            QRGContents.Type.TEXT,
                            500
                    );

                    Log.d("splitData", splitQrData);

                    try {
                        qrCodeView = new ImageView(ManualEntry.this);
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

                        Toast.makeText(ManualEntry.this, "Nekaj je slo narobe", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean validateParams() {
        if (
                !purposeCodeText.getText().toString().isEmpty() &&
                !paymentPurposeText.getText().toString().isEmpty() &&
                !dueDateText.getText().toString().isEmpty() &&
                !dueDateText.getText().toString().isEmpty() &&
                !IBANText.getText().toString().isEmpty() &&
                !referenceText.getText().toString().isEmpty() &&
                !nameText.getText().toString().isEmpty() &&
                !addressText.getText().toString().isEmpty() &&
                !placeText.getText().toString().isEmpty() &&
                isNumeric(amountText.getText().toString())
        ) {
            return true;
        }

        Toast.makeText(this, "Podatki niso v pravi obliki", Toast.LENGTH_SHORT).show();

        return false;
    }

    public static boolean isNumeric(String strNum) {
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }

        return true;
    }
}
