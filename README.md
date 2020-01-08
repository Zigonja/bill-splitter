# bill-splitter

##UPNQR
This is an Android App that is designed to read, modify and create 
**UPNQR Codes** for Slovenian bills and banks.

**UPNQR Codes** are essetially only QR codes that hold markup, that bank apps can read.
Read more about [Slovenian UPNQR Specification](https://www.upn-qr.si/uploads/files/NavodilaZaProgramerjeUPNQR.pdf)
Or more about [UPNQR Overall](https://www.upn-qr.si/)

---

## App Specification
The application consists of **three** activities, one main and two sub activities, it uses a green-ish color theme.
The main activity is actually a navigational activity, that with a click of the butto redirects us
to the desired functionality of the App

Functionallities consist of 
* **First activity** - Creating and sharing a custom UPNQR Code
* **Second activity** - Reading, splitting and sharing a Premade UPNQR Code.

### Reading a custom UPNQR Code
I used a library [barcode-reader](https://github.com/avaneeshkumarmaurya/Barcode-Reader) to help me read QR codes.
`
Here is a code snippet of reading the UPNQR Code:

``Java
qrCodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent launchIntent = BarcodeReaderActivity
                        .getLaunchIntent(MainActivity.this, true, false);
                startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
            }
        });
        
// Later - onActivityResult

if (requestCode == BARCODE_READER_ACTIVITY_REQUEST && data != null) {
            Barcode qrCode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);

            Intent qrCodeSplitterIntent = new Intent(this, QRSplitterActivity.class);
            qrCodeSplitterIntent.putExtra("qrData", qrCode.rawValue);

            startActivity(qrCodeSplitterIntent);
        }
```

### Slitting a custom UPNQR Code
To split the UPNQR code I decode the QR code, get it's **amount** value, witch is on index 8. I then get the value from
splitting input, to know how on many parts I have to split the bill _(Max 99)_

I then use this snippet of code to accomplish splitting:

```Java
        String[] UPNValues = qrData.split("\\r?\\n");

        if (UPNValues.length < 19 || !isNumeric(UPNValues[8])) {
            Toast.makeText(
                this,
                "Nepravilna oblika QR kode",
                Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        int splitNumber = Integer.parseInt(
            splitNumberText.getText().toString().length() > 0 ?
                splitNumberText.getText().toString() :
                "1"
        );

        UPNValues[8] = (Integer.parseInt(UPNValues[8]) / splitNumber) + "";

        String splitQrData = "";

        for (String UPNValue : UPNValues) {
            splitQrData += UPNValue.trim() + '\n';
        }

        QRGEncoder qrgEncoder = new QRGEncoder(
            splitQrData,
            null,
            QRGContents.Type.TEXT,
            500
        );
```

### Sharing an UPNQR code
To share an UPNQR Code I have to first get it from the ImageView and save to to local storage, after that I can use
androids built-in sharing activity.

This is the snippet of code showing you how I accomplished this.

```Java
                Bitmap bitmap = ((BitmapDrawable) qrCodeView.getDrawable()).getBitmap();

                if (bitmap == null) {
                    Toast.makeText(
                        QRSplitterActivity.this,
                        "Nekaj je šlo narobe",
                        Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (ContextCompat.checkSelfPermission(QRSplitterActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {


                        ActivityCompat.requestPermissions(QRSplitterActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                0);

                        ActivityCompat.requestPermissions(QRSplitterActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            0);
                } else {
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
                                QRSplitterActivity.this,
                                "Nekaj je šlo narobe",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
```

### Creating a cusotm UPNQR Code

Here is a code snippet of how an UPNQR Code is generated in my App. The activity also consists of 10 Input fields, defining 
what user will later see in his mobile bank app once he scans the created QR Code

```Java
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
```
