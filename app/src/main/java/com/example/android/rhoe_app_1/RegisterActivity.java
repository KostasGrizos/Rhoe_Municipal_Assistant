package com.example.android.rhoe_app_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.app_v12.R;
import com.example.android.rhoe_app_1.FirebaseUsers.UserInfoFirebase;
import com.example.android.rhoe_app_1.Zebra.SettingsHelper;
import com.example.android.rhoe_app_1.Zebra.SignatureArea;
import com.example.android.rhoe_app_1.Zebra.UIHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    //Signature
    private UIHelper helper = new UIHelper(this);
    private SignatureArea signatureArea = null;

    private static final String TAG = "RegisterActivity";

    private Spinner TypeReg;
    private EditText FnameReg;
    private EditText LnameReg;
    private AutoCompleteTextView MunicipalityRegAC;
    private String MIDReg;
    private String[] MunicipalityReg, MIDRegTable;
    private Button FinishReg;
    private Button GoBackReg;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private String MunicipalityText, MIDText, userID, imageB64;
    private Bitmap image;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step_2);

        final Bundle MunicipalityB= this.getIntent().getExtras();
        assert MunicipalityB != null;
        MunicipalityText= MunicipalityB.getString("Municipality");
        final Bundle MIDB = this.getIntent().getExtras();
        assert MIDB != null;
        MIDText= MIDB.getString("MID");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        FirebaseUser user =firebaseAuth.getCurrentUser();
        userID = user.getUid();

        //Signature
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        signatureArea = new SignatureArea(this);
        //LinearLayout SignatureLinearLayout = (LinearLayout) findViewById(R.id.signature_linear_layout);
        //ConstraintLayout registerConstraintLayout = (ConstraintLayout) findViewById(R.id.register_constraint_layout);
        //SignatureLinearLayout.addView(signatureArea, 0 , params);

        //StorageReference storageRef = storage.getReferenceFromUrl("gs://testproject-328af.appspot.com/");
        //final StorageReference SignatureImagesRef = storageRef.child("Signatures/" + userID + ".jpg");


        TypeReg = (Spinner) findViewById(R.id.spAccountTypeReg);
        FnameReg = (EditText) findViewById(R.id.etFirstNameReg);
        LnameReg = (EditText) findViewById(R.id.etLastNameReg);

        MunicipalityRegAC = (AutoCompleteTextView) findViewById(R.id.acMunicipality);
        MunicipalityReg = getResources().getStringArray(R.array.autoComplMunicipality);
        ArrayAdapter<String> adapterMunicipality = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, MunicipalityReg);
        MunicipalityRegAC.setAdapter(adapterMunicipality);
        MIDRegTable = getResources().getStringArray(R.array.autoComplMID);
        ArrayAdapter<String> adapterMID = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, MIDRegTable);

        if (!Objects.equals(MIDText, "0000")) {
            MunicipalityRegAC.setEnabled(false);
            MunicipalityRegAC.setHint(MunicipalityText);
        }

        MunicipalityRegAC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Arrays.asList(MunicipalityReg).indexOf(MunicipalityRegAC.getText().toString()) >= 0) {
                    MIDReg = MIDRegTable[Arrays.asList(MunicipalityReg).indexOf(MunicipalityRegAC.getText().toString())];
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        FinishReg = (Button) findViewById(R.id.btnCreateUserReg);
        GoBackReg = (Button) findViewById(R.id.btnReturnReg);

        FinishReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                image = signatureArea.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = SignatureImagesRef.putBytes(data);
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
                });
                */

                saveUserInformation();
            }
        });

        GoBackReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    //Save
    private void saveUserInformation(){
        String newEntry1 = TypeReg.getSelectedItem().toString();
        String newEntry2 = FnameReg.getText().toString();
        String newEntry3 = LnameReg.getText().toString();
        //String newEntry4 = imageB64;
        String newEntry5, newEntry6;
        if (MIDText != "0000") {
            newEntry5 = MunicipalityText;
            newEntry6 = MIDText;
        } else {
            newEntry5 = MunicipalityRegAC.getText().toString();
            newEntry6 = MIDReg;
        }

        if ((newEntry1.length() != 0) &&
                (newEntry2.length() != 0) &&
                (newEntry3.length() != 0) &&
                (newEntry5.length() != 0) &&
                (newEntry6.length() != 0)) {

            UserInfoFirebase userInformationFirebase = new UserInfoFirebase(newEntry1, newEntry2, newEntry3, "-", newEntry5, newEntry6, "", "", "Vehicle");

            FirebaseUser user =firebaseAuth.getCurrentUser();

            databaseReference.child(user.getUid()).setValue(userInformationFirebase);

            Toast.makeText(this, "Οι πληροφορίες αποθηκεύτηκαν!", Toast.LENGTH_LONG).show();

            firebaseAuth.signOut();
            finish();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        } else {
            Toast.makeText(this, "Πρέπει να συμπληρωθούν όλα τα πεδία", Toast.LENGTH_LONG).show();
        }
    }
}
