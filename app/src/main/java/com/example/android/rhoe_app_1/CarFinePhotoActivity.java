package com.example.android.rhoe_app_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.app_v12.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CarFinePhotoActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextRecognizer textRecognizer;
    TextView textView;
    TextView textView1;
    TextView textView2;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Button TakePhotoButton, CancelPhotoButton;
    boolean cameraOn;

    byte[] bitmapdata;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    android.icu.util.Calendar calendar;
    SimpleDateFormat simpleDatePhotoFirebaseFormat;
    String DatePhotoFirebase;

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
        setContentView(R.layout.activity_car_fine_photo);

        cameraView = (SurfaceView) findViewById(R.id.surface_view1);
        textView = (TextView) findViewById(R.id.tvOCRresult);
        textView1 = (TextView) findViewById(R.id.tvTrue);
        textView2 = (TextView) findViewById(R.id.tvCountry);
        TakePhotoButton = (Button) findViewById(R.id.btnTakePhoto);
        CancelPhotoButton = (Button) findViewById(R.id.btnCancelPhoto);

        Bundle bMID = this.getIntent().getExtras();
        final String MID = bMID.getString("ConMID");

        final int REQUEST_IMAGE_CAPTURE = 1;

        final StorageReference storageRef = storage.getReferenceFromUrl("gs://testproject-328af.appspot.com/");

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        turnCameraOn();

        TakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (cameraOn) {
                    /*
                    cameraView.setDrawingCacheEnabled(true); //CamView OR THE NAME OF YOUR LAYOUR
                    cameraView.buildDrawingCache(true);
                    Bitmap bmp = Bitmap.createBitmap(cameraView.getDrawingCache());
                    cameraView.setDrawingCacheEnabled(false); // clear drawing cache
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    bitmapdata = baos.toByteArray();
                    ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);
                    */
                    //rootView = cameraView;

                    /*
                    rootView.setDrawingCacheEnabled(true);
                    rootView.buildDrawingCache(true);
                    Bitmap bmp = Bitmap.createBitmap(rootView.getDrawingCache());
                    rootView.setDrawingCacheEnabled(false); // clear drawing cache
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    bitmapdata = baos.toByteArray();
                    ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);
                    */
                    takePicture();
                } else {
                    getTimestamp();
                    final StorageReference CarFineImagesRef = storageRef.child(MID + "/VehicleFines/" + DatePhotoFirebase + ".jpg");
                    UploadTask uploadTask = CarFineImagesRef.putBytes(bitmapdata);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        }
                    });

                    Bundle conOCR = new Bundle();
                    conOCR.putBoolean("ConditionOCR", true);
                    Bundle bTimestamp = new Bundle();
                    bTimestamp.putString("ConTimestamp", DatePhotoFirebase);
                    Intent intent = new Intent(CarFinePhotoActivity.this, FineCompleteActivity.class);
                    intent.putExtras(conOCR);
                    intent.putExtras(bTimestamp);
                    startActivity(intent);
                }
                /*
                image = cameraView.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = CarFineImagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                });*/

            }
        });
        CancelPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraOn) {
                    Intent intent = new Intent(CarFinePhotoActivity.this, DashboardActivity.class);
                    startActivity(intent);
                } else {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(CarFinePhotoActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                        cameraOn = true;
                        TakePhotoButton.setText("ΛΗΨΗ");
                        CancelPhotoButton.setText("ΑΚΥΡΩΣΗ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void turnCameraOn() {
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

                            ActivityCompat.requestPermissions(CarFinePhotoActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                        cameraOn = true;
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
        }
    }

    private void takePicture() {
        try{
            cameraSource.takePicture(null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                    try {
                        Bitmap loadedImage = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);
                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                        loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                        bitmapdata = ostream.toByteArray();
                        cameraSource.stop();
                        cameraOn = false;
                        TakePhotoButton.setText("ΣΥΝΕΧΕΙΑ");
                        CancelPhotoButton.setText("ΕΠΑΝΑΛΗΨΗ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception ex){
            textView.setText("Σφάλμα κατά την λήψη φωτογραφίας!");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTimestamp() {
        calendar = android.icu.util.Calendar.getInstance(TimeZone.getTimeZone("EET"));

        simpleDatePhotoFirebaseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");
        DatePhotoFirebase = simpleDatePhotoFirebaseFormat.format(calendar.getTime());

    }
}
