package com.example.gateopener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Activity_addUser extends AppCompatActivity {


    private EditText editText;
    private ImageView imageView;
    private String mCurrentPhotoPath;
    private String name;



    private static final int REQUEST_IMAGE_CAPTURE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Button button_picture = (Button) this.findViewById(R.id.button_takePicture);
        this.editText = (EditText)this.findViewById(R.id.editText_UserName);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);

        editText.setHint(R.string.userName_Hint);

        button_picture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                name =  editText.getText().toString();
                dispatchTakePictureIntent();
            }
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this,"A problem happened", Toast.LENGTH_LONG).show();            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri imageUri = FileProvider.getUriForFile(Activity_addUser.this, "com.example.gateopener.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // Path to /data/data/myapp/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File image = File.createTempFile(
                name + "-",  /* prefix */
                ".jpg",         /* suffix */
                directory      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            setImage();

            String fileName = mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/")+1, mCurrentPhotoPath.lastIndexOf("-"));

            this.editText.setText(fileName);
            //Disable the editing
            this.editText.setFocusable(false);
            this.editText.setClickable(false);
            this.editText.setCursorVisible(false);

            Button button_picture = (Button) this.findViewById(R.id.button_takePicture);

            button_picture.setText(R.string.button_save);
            button_picture.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Toast.makeText(Activity_addUser.this,R.string.saved,Toast.LENGTH_LONG).show();
                    Activity_addUser.this.finish();
                }
            });

        }
    }

    public void setImage(){
        File f = new File(mCurrentPhotoPath);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Bitmap rotatedImage = rotateImageIfRequired(mCurrentPhotoPath, bitmap);
            this.imageView.setImageBitmap(rotatedImage);
            saveImage(mCurrentPhotoPath, rotatedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static Bitmap rotateImageIfRequired(String path, Bitmap img) throws IOException{
        ExifInterface ei = new ExifInterface(path);


        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default :
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void saveImage(String path, Bitmap img) {
        OutputStream fOut = null;
        File file = new File(path);
        try {
            fOut = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}