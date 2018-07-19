package com.example.android.rhoe_app_1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.android.app_v12.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private SurfaceHolder camHolder;
    private boolean previewRunning;
    final Context context = this;
    public static Camera camera = null;
    private RelativeLayout CamView;
    private Bitmap inputBMP = null, bmp, bmp1;
    private ImageView mImage;
    byte[] bitmapdata;
    View rootView;

    private Bitmap image;
    FirebaseStorage storage = FirebaseStorage.getInstance();

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

        final int REQUEST_IMAGE_CAPTURE = 1;

        StorageReference storageRef = storage.getReferenceFromUrl("gs://testproject-328af.appspot.com/");
        final StorageReference CarFineImagesRef = storageRef.child("Test/" + "test" + ".jpg");

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        turnCameraOn();

        TakePhotoButton.setOnClickListener(new View.OnClickListener() {
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
                    rootView = cameraView;
                    rootView.setDrawingCacheEnabled(true);
                    rootView.buildDrawingCache(true);
                    Bitmap bmp = Bitmap.createBitmap(rootView.getDrawingCache());
                    rootView.setDrawingCacheEnabled(false); // clear drawing cache
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    bitmapdata = baos.toByteArray();
                    ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

                    cameraSource.stop();
                    cameraOn = false;
                    TakePhotoButton.setText("ΣΥΝΕΧΕΙΑ");
                    CancelPhotoButton.setText("ΕΠΑΝΑΛΗΨΗ");
                } else {
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
                    Intent intent = new Intent(CarFinePhotoActivity.this, DashboardActivity.class);
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
                    Bundle conOCR = new Bundle();
                    conOCR.putBoolean("ConditionOCR", false);
                    Intent intent = new Intent(CarFinePhotoActivity.this, DashboardActivity.class);
                    intent.putExtras(conOCR);
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
    /*
    private void takePicture() {
        try{
            cameraSource.takePicture(null, new CameraSource.PictureCallback() {

                private File imageFile;
                @Override
                public void onPictureTaken(byte[] bytes) {
                    try {
                        // convert byte array into bitmap
                        Bitmap loadedImage = null;
                        Bitmap rotatedBitmap = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);

                        // rotate Image
                        Matrix rotateMatrix = new Matrix();
                        rotateMatrix.postRotate(rotation);
                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                                loadedImage.getWidth(), loadedImage.getHeight(),
                                rotateMatrix, false);
                        String state = Environment.getExternalStorageState();
                        File folder = null;
                        if (state.contains(Environment.MEDIA_MOUNTED)) {
                            folder = new File(Environment
                                    .getExternalStorageDirectory() + "/Demo");
                        } else {
                            folder = new File(Environment
                                    .getExternalStorageDirectory() + "/Demo");
                        }

                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdirs();
                        }
                        if (success) {
                            java.util.Date date = new java.util.Date();
                            imageFile = new File(folder.getAbsolutePath()
                                    + File.separator
                                    //+ new Timestamp(date.getTime()).toString()
                                    + "Image.jpg");

                            imageFile.createNewFile();
                        } else {
                            Toast.makeText(getBaseContext(), "Image Not saved",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                        // save image into gallery
                        rotatedBitmap = resize(rotatedBitmap, 800, 600);
                        rotatedBitmap.compress(CompressFormat.JPEG, 100, ostream);

                        FileOutputStream fout = new FileOutputStream(imageFile);
                        fout.write(ostream.toByteArray());
                        fout.close();
                        ContentValues values = new ContentValues();

                        values.put(Images.Media.DATE_TAKEN,
                                System.currentTimeMillis());
                        values.put(Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.MediaColumns.DATA,
                                imageFile.getAbsolutePath());

                        CustomCamaraActivity.this.getContentResolver().insert(
                                Images.Media.EXTERNAL_CONTENT_URI, values);

                        setResult(Activity.RESULT_OK); //add this
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception ex){
            txTextoCapturado.setText("Error al capturar fotografia!");
        }
    }
    */
}
