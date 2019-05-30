package com.example.loginv3;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;



public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";

    private static final int SELECT_PICTURE = 100;
    private static final int UPLOAD_PICTURE = 200;


    private TextView mTextView;
    private EditText usernameField;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ImageView mImage;
    private StorageReference storageRef;
    private FirebaseStorage storage;

    private FirebaseUser mUser;

    private static Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //ativar os botões
        findViewById(R.id.updateButton).setOnClickListener(this);
        findViewById(R.id.logoutButton).setOnClickListener(this);
        findViewById(R.id.atualizarButton).setOnClickListener(this);
        findViewById(R.id.errorButton).setOnClickListener(this);

        //ativar o texto
        findViewById(R.id.downloadText).setOnClickListener(this);

        //ativar a imagem
        findViewById(R.id.profileImage).setOnClickListener(this);


        mTextView = findViewById(R.id.welcomeText);
        usernameField = findViewById(R.id.usernameField);
        mImage = findViewById(R.id.profileImage);

        //autenticação
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mTextView.setText("Seja bem vindo de volta " + mAuth.getCurrentUser().getEmail());

        //banco de dados
        mDatabase = FirebaseDatabase.getInstance().getReference();



        //Cloud firestorage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mContext = getApplicationContext();

        //checkFilePermissions();
        handlePermission();
        updateDisplayName();
        updateProfilePicture();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.updateButton){
            updateUserData(mAuth.getCurrentUser().getUid(), usernameField.getText().toString());
            openImageChooser(UPLOAD_PICTURE);
        }
        if (i == R.id.logoutButton){
            mDatabase.child("message").setValue("desloguei");//só um teste para ver se eu consigo criar novos nós
            signOut();
        }
        if (i == R.id.profileImage){
            openImageChooser(SELECT_PICTURE);
        }

        if(i == R.id.atualizarButton){
            refreshScreen();
        }
        if(i == R.id.errorButton){
            generateError();
        }
        if(i == R.id.downloadText){
            generateError();
        }

    }

    private void generateError(){
        //Força a geração de um ArrayIndexOutOfBoundsException
        char error [] = new char[10];
        System.out.print(error[11]);

    }

    private void refreshScreen(){
        updateProfilePicture();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    private void updateUserData(String userId, String name) {
        mDatabase.child("users").child(userId).child("username").setValue(name);
        updateDisplayName();
        updateProfilePicture();

    }


    private void updateProfilePicture(){
        StorageReference pathReference = storageRef.child("images/"+mUser.getUid()+"/profilePic").getParent();

        if(pathReference == null){
            return;
        } else {
            pathReference = storageRef.child("images/"+mUser.getUid()+"/profilePic");
            Glide.with(mContext).load(pathReference).into(mImage);
        }
    }

    private void updateDisplayName(){
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User temp = dataSnapshot.getValue(User.class);
                        String name = temp.username;
                        mTextView.setText("Seja bem vindo de volta Sr(a)" + name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkFilePermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = ProfileActivity.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += ProfileActivity.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    private void handlePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SELECT_PICTURE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            //  Show your own message here
                        } else {
                            showSettingsAlert();
                        }
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }









   // private static final String TAG = "SelectImageActivity";
    /* Choose an image from Gallery */
    void openImageChooser(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getPathFromURI(selectedImageUri);
                            Log.i(TAG, "Image Path : " + path);
                            // Set the image in ImageView
                            findViewById(R.id.profileImage).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.profileImage)).setImageURI(selectedImageUri);
                                }
                            });

                        }

                    }
                    if(requestCode == UPLOAD_PICTURE){
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getRealPathFromURI_API19(mContext, selectedImageUri);
                            Log.i(TAG, "Image Path : " + path);
                            Log.i(TAG, "Image real path : " + String.valueOf(selectedImageUri));

                            //Codar aqui o código para enviar para a CLOUD

                            // File or Blob
                            String manualPath = "/storage/emulated/0/Download/aa.jpeg";

                            //String realPath = Context.getFilesDir();

                            Uri file = Uri.fromFile(new File(path));

                            // Create the file metadata
                           /* metadata = new StorageMetadata.Builder()
                                    .setContentType("image/jpeg")
                                    .build();
                            */

                            // Upload file and metadata to the path 'images/mountains.jpg'
                             UploadTask uploadTask = storageRef.child("images/" + mUser.getUid()+ "/profilePic").putFile(file);

                            // Listen for state changes, errors, and completion of the upload.
                            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    System.out.println("Upload is " + progress + "% done");
                                }
                            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                                    System.out.println("Upload is paused");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Handle successful uploads on complete
                                    // ...
                                }
                            });


                            // Set the image in ImageView
                            findViewById(R.id.profileImage).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.profileImage)).setImageURI(selectedImageUri);
                                }
                            });

                        }
                    }
                }

            }
        }).start();

    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e(TAG, "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }


    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        openAppSettings(ProfileActivity.this);
                    }
                });
        alertDialog.show();
    }

    public static void openAppSettings(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

}






