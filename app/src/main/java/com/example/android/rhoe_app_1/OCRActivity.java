package com.example.android.rhoe_app_1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.app_v12.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class OCRActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView;
    TextView textView1;
    TextView textView2;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    String result, possiblePlate, possibleCountry;
    Button ConfirmOCRButton, CancelOCRButton;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        cameraView = (SurfaceView) findViewById(R.id.surface_view1);
        textView = (TextView) findViewById(R.id.tvOCRresult);
        textView1 = (TextView) findViewById(R.id.tvTrue);
        textView2 = (TextView) findViewById(R.id.tvCountry);
        ConfirmOCRButton = (Button) findViewById(R.id.btnConfirmOCR);
        CancelOCRButton = (Button) findViewById(R.id.btnCancelOCR);

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("OCR1", "Detector dependencies are not yet available");
        } else {

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(720, 1280)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            //1280, 1024
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(OCRActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0)
                    {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder(7);
                                for(int i =0;i<items.size();++i)
                                {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");

                                }
                                textView.setText(stringBuilder.toString());
                                constructPlate_GR();
                                if (stringBuilder.toString().contains("AL")){
                                    constructPlate_AL();
                                    possibleCountry = "AL";
                                }
                                if (stringBuilder.toString().contains("MK")){
                                    constructPlate_SK();
                                    possibleCountry = "FYROM";
                                }
                                if (stringBuilder.toString().contains("BG")){
                                    constructPlate_BG();
                                    possibleCountry = "BG";
                                }
                                if (stringBuilder.toString().contains("SRB")){
                                    constructPlate_SRB();
                                    possibleCountry = "SRB";
                                }
                                if (stringBuilder.toString().contains("RKS")){
                                    constructPlate_RKS();
                                    possibleCountry = "RKS";
                                }
                                if (stringBuilder.toString().contains("CY")){
                                    constructPlate_CY();
                                    possibleCountry = "CY";
                                }

                            }
                        });
                    }
                }
            });
        }

        ConfirmOCRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle conOCR = new Bundle();
                conOCR.putBoolean("ConditionOCR", false);
                Bundle OCRResultB = new Bundle();
                OCRResultB.putString("OCR", possiblePlate);
                Bundle OCRResultCountryB = new Bundle();
                OCRResultCountryB.putString("OCRC", possibleCountry);
                Intent intent = new Intent(OCRActivity.this, FineCompleteActivity.class);
                intent.putExtras(conOCR);
                intent.putExtras(OCRResultB);
                intent.putExtras(OCRResultCountryB);
                startActivity(intent);
            }
        });
        CancelOCRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle conOCR = new Bundle();
                conOCR.putBoolean("ConditionOCR", false);
                Intent intent = new Intent(OCRActivity.this, FineCompleteActivity.class);
                intent.putExtras(conOCR);
                startActivity(intent);
            }
        });


    }

    public void constructPlate_GR() {
        result=textView.getText().toString()+"TEST";
        for (int i=0; i<result.length()-4; i++) {
            //AAA-0000 or AAA 0000
            if(Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isLetter(result.charAt(i+2)) &&
                    ((result.subSequence(i+3,i+4).equals("-")) || (result.subSequence(i+3,i+4).equals(" "))) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6)) &&
                    Character.isDigit(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i+3) + "-" + result.subSequence(i+4, i+8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("GR-Greece");
                possibleCountry = "GR";
                break;
            }

            //AAA0000
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isLetter(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6))
                    )
            {
                possiblePlate = result.subSequence(i, i+3) + "-" + result.subSequence(i+3, i+7);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("GR-Greece");
                possibleCountry = "GR";
                break;
            }
        }
    }

    public void constructPlate_AL() {
        result=textView.getText().toString()+"TEST";
        for (int i=0; i<result.length()-4; i++) {
            //AA 000 AA or AA-000-AA
            if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    ((result.subSequence(i+6,i+7).equals("-")) || (result.subSequence(i+6,i+7).equals(" "))) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
                    )
            {
                possiblePlate = result.subSequence(i, i+2) + " " + result.subSequence(i+3, i+6) + " " + result.subSequence(i+7, i+9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Albania");
                break;
            }

            //AA000AA or AA000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isLetter(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 5) + " " + result.subSequence(i + 5, i + 7);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Albania");
                break;
            }

            //AA 000AA or AA-000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 3, i + 6) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Albania");
                break;
            }

            //AA000 AA or AA000-AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    ((result.subSequence(i+5,i+6).equals("-")) || (result.subSequence(i+5,i+6).equals(" "))) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 5) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Albania");
                break;
            }
        }
    }

    public void constructPlate_SK() {
        result=textView.getText().toString()+"TEST";
        for (int i=0; i<result.length()-4; i++) {
            //AA 0000 AA or AA-0000-AA
            if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6)) &&
                    ((result.subSequence(i+7,i+8).equals("-")) || (result.subSequence(i+7,i+8).equals(" "))) &&
                    Character.isLetter(result.charAt(i+8)) &&
                    Character.isLetter(result.charAt(i+9))
                    )
            {
                possiblePlate = result.subSequence(i, i+2) + " " + result.subSequence(i+3, i+7) + " " + result.subSequence(i+8, i+10);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("F.Y.R.o.M.");
                break;
            }

            //AA0000AA or AA0000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 6) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("F.Y.R.o.M.");
                break;
            }

            //AA 0000AA or AA-0000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 3, i + 7) + " " + result.subSequence(i + 7, i + 9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("F.Y.R.o.M.");
                break;
            }

            //AA0000 AA or AA0000-AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    ((result.subSequence(i+6,i+7).equals("-")) || (result.subSequence(i+6,i+7).equals(" "))) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 6) + " " + result.subSequence(i + 7, i + 9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("F.Y.R.o.M.");
                break;
            }

        }
    }

    public void constructPlate_BG() {
        result=textView.getText().toString().toUpperCase()+"TEST";
        for (int i=0; i<result.length()-4; i++) {
            //AA 0000 AA or AA-0000-AA
            if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6)) &&
                    ((result.subSequence(i+7,i+8).equals("-")) || (result.subSequence(i+7,i+8).equals(" "))) &&
                    Character.isLetter(result.charAt(i+8)) &&
                    Character.isLetter(result.charAt(i+9))
            ))
            {
                possiblePlate = result.subSequence(i, i+2) + " " + result.subSequence(i+3, i+7) + " " + result.subSequence(i+8, i+10);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

            //AA0000AA or AA0000AA
            else if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
            ))
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 6) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

            //AA 0000AA or AA-0000AA
            else if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
            ))
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 3, i + 7) + " " + result.subSequence(i + 7, i + 9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

            //AA0000 AA or AA0000-AA
            else if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    ((result.subSequence(i+6,i+7).equals("-")) || (result.subSequence(i+6,i+7).equals(" "))) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
            ))
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 6) + " " + result.subSequence(i + 7, i + 9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

            //A 0000 AA or A-0000-AA
            if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    ((result.subSequence(i+1,i+2).equals("-")) || (result.subSequence(i+1,i+2).equals(" "))) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    ((result.subSequence(i+6,i+7).equals("-")) || (result.subSequence(i+6,i+7).equals(" "))) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
            ))
            {
                possiblePlate = result.subSequence(i, i+1) + " " + result.subSequence(i+2, i+6) + " " + result.subSequence(i+7, i+9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

            //AA0000AA or AA0000AA
            else if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    Character.isDigit(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isLetter(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6))
            ))
            {
                possiblePlate = result.subSequence(i, i + 1) + " " + result.subSequence(i + 1, i + 5) + " " + result.subSequence(i + 5, i + 7);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

            //AA 0000AA or AA-0000AA
            else if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    ((result.subSequence(i+1,i+2).equals("-")) || (result.subSequence(i+1,i+2).equals(" "))) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
            ))
            {
                possiblePlate = result.subSequence(i, i + 1) + " " + result.subSequence(i + 2, i + 6) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

            //AA0000 AA or AA0000-AA
            else if ((!result.subSequence(i,i+1).equals("BG")) && (Character.isLetter(result.charAt(i)) &&
                    Character.isDigit(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    ((result.subSequence(i+5,i+6).equals("-")) || (result.subSequence(i+5,i+6).equals(" "))) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
            ))
            {
                possiblePlate = result.subSequence(i, i + 1) + " " + result.subSequence(i + 1, i + 5) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Bulgaria");
                break;
            }

        }
    }

    public void constructPlate_SRB() {
        result=textView.getText().toString()+"TEST";
        for (int i=0; i<result.length()-4; i++) {
            //AA 0000 AA or AA-0000-AA
            if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6)) &&
                    ((result.subSequence(i+7,i+8).equals("-")) || (result.subSequence(i+7,i+8).equals(" "))) &&
                    Character.isLetter(result.charAt(i+8)) &&
                    Character.isLetter(result.charAt(i+9))
                    )
            {
                possiblePlate = result.subSequence(i, i+2) + " " + result.subSequence(i+3, i+7) + "-" + result.subSequence(i+8, i+10);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

            //AA0000AA or AA0000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 6) + "-" + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

            //AA 0000AA or AA-0000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 3, i + 7) + "-" + result.subSequence(i + 7, i + 9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

            //AA0000 AA or AA0000-AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    ((result.subSequence(i+6,i+7).equals("-")) || (result.subSequence(i+6,i+7).equals(" "))) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 6) + "-" + result.subSequence(i + 7, i + 9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

            //AA 000 AA or AA-000-AA
            if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    ((result.subSequence(i+6,i+7).equals("-")) || (result.subSequence(i+6,i+7).equals(" "))) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
                    )
            {
                possiblePlate = result.subSequence(i, i+2) + " " + result.subSequence(i+3, i+6) + "-" + result.subSequence(i+7, i+9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

            //AA000AA or AA000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isLetter(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 5) + "-" + result.subSequence(i + 5, i + 7);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

            //AA 000AA or AA-000AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 3, i + 6) + "-" + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

            //AA000 AA or AA000-AA
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    ((result.subSequence(i+5,i+6).equals("-")) || (result.subSequence(i+5,i+6).equals(" "))) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 5) + "-" + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Serbia");
                break;
            }

        }
    }

    public void constructPlate_RKS() {
        result=textView.getText().toString()+"TEST";
        for (int i=0; i<result.length()-4; i++) {
            //00 000 AA or 00-000-AA
            if (Character.isDigit(result.charAt(i)) &&
                    Character.isDigit(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    ((result.subSequence(i+6,i+7).equals("-")) || (result.subSequence(i+6,i+7).equals(" "))) &&
                    Character.isLetter(result.charAt(i+7)) &&
                    Character.isLetter(result.charAt(i+8))
                    )
            {
                possiblePlate = result.subSequence(i, i+2) + " " + result.subSequence(i+3, i+6) + " " + result.subSequence(i+7, i+9);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Kosovo");
                break;
            }

            //00000AA
            else if (Character.isDigit(result.charAt(i)) &&
                    Character.isDigit(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isLetter(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 5) + " " + result.subSequence(i + 5, i + 7);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Kosovo");
                break;
            }

            //00 000AA or 00-000AA
            else if (Character.isDigit(result.charAt(i)) &&
                    Character.isDigit(result.charAt(i+1)) &&
                    ((result.subSequence(i+2,i+3).equals("-")) || (result.subSequence(i+2,i+3).equals(" "))) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 3, i + 6) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Kosovo");
                break;
            }

            //00000 AA or 00000-AA
            else if (Character.isDigit(result.charAt(i)) &&
                    Character.isDigit(result.charAt(i+1)) &&
                    Character.isDigit(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    ((result.subSequence(i+5,i+6).equals("-")) || (result.subSequence(i+5,i+6).equals(" "))) &&
                    Character.isLetter(result.charAt(i+6)) &&
                    Character.isLetter(result.charAt(i+7))
                    )
            {
                possiblePlate = result.subSequence(i, i + 2) + " " + result.subSequence(i + 2, i + 5) + " " + result.subSequence(i + 6, i + 8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Kosovo");
                break;
            }
        }
    }

    public void constructPlate_CY() {
        result=textView.getText().toString()+"TEST";
        for (int i=0; i<result.length()-4; i++) {
            //AAA-000 or AAA 000
            if(Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isLetter(result.charAt(i+2)) &&
                    ((result.subSequence(i+3,i+4).equals("-")) || (result.subSequence(i+3,i+4).equals(" "))) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6))
                    )
            {
                possiblePlate = result.subSequence(i, i+3) + " " + result.subSequence(i+4, i+8);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Cyprus");
                break;
            }

            //AAA0000
            else if (Character.isLetter(result.charAt(i)) &&
                    Character.isLetter(result.charAt(i+1)) &&
                    Character.isLetter(result.charAt(i+2)) &&
                    Character.isDigit(result.charAt(i+3)) &&
                    Character.isDigit(result.charAt(i+4)) &&
                    Character.isDigit(result.charAt(i+5)) &&
                    Character.isDigit(result.charAt(i+6))
                    )
            {
                possiblePlate = result.subSequence(i, i+3) + " " + result.subSequence(i+3, i+7);
                possiblePlate = possiblePlate.toUpperCase();
                textView1.setText(possiblePlate);
                textView2.setText("Cyprus");
                break;
            }
        }
    }
}