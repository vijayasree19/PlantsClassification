package com.example.plantsclassification;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class UploadImage extends AppCompatActivity {

   private static final String MODEL_PATH = "model.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;
    LocationHandler locationHandler;
   private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    TextView tv;
    TextView map;
     ImageView profileImageView;
    FloatingActionButton pickImage;
    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_PHOTO = 2;
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    Bitmap thumbnail;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        locationHandler = new LocationHandler(this);
        profileImageView = findViewById(R.id.profile);
        pickImage = findViewById(R.id.pick);
        tv = findViewById(R.id.tv);
        map=findViewById(R.id.maps);
        map.setVisibility(View.INVISIBLE);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.pick:
                        new AlertDialog.Builder(UploadImage.this)
                                .setTitle(R.string.action_pic)
                                .setItems(R.array.uploadImages, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                Intent photopicker = new Intent(Intent.ACTION_PICK);
                                                photopicker.setType("image/*");
                                                startActivityForResult(photopicker, PICK_IMAGE);
                                                break;

                                            case 1:
                                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                startActivityForResult(intent, CAPTURE_PHOTO);
                                                break;
                                            case 2:
                                                profileImageView.setImageResource(R.drawable.ic_account_box_black_24dp);
                                                break;
                                        }
                                    }
                                }).show();

                }}

        });

        if (ContextCompat.checkSelfPermission(UploadImage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            profileImageView.setEnabled(false);
            ActivityCompat.requestPermissions(UploadImage.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            profileImageView.setEnabled(true);
        }
        initTensorFlowAndLoadModel();


    }
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                profileImageView.setEnabled(true);
            }
        }
    }

    public void setProgressBar() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Please wait...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressBarStatus < 100){
                    progressBarStatus += 30;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    progressBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressBarStatus);
                        }
                    });
                }
                if (progressBarStatus >= 100) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.dismiss();
                }

            }
        }).start();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_sort:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                final Uri imageUri = data.getData();
                try {
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    setProgressBar();
                    //set profile picture form gallery
                    Bitmap bitmap;
                    bitmap = Bitmap.createScaledBitmap(selectedImage, INPUT_SIZE, INPUT_SIZE, false);

                    profileImageView.setImageBitmap(bitmap);

                    final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                    tv.setText(results.toString());
                    map.setVisibility(View.VISIBLE);
                    // profileImageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        } else if (requestCode == CAPTURE_PHOTO) {
            if (resultCode == RESULT_OK) {
                onCaptureImageResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        thumbnail = (Bitmap) data.getExtras().get("data");

        //set Progress Bar
        setProgressBar();
        Bitmap bitmap;
        bitmap = Bitmap.createScaledBitmap(thumbnail, INPUT_SIZE, INPUT_SIZE, false);

        profileImageView.setImageBitmap(bitmap);

        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

        tv.setText(results.toString());

        //set profile picture form camera
        //profileImageView.setMaxWidth(200);
        //profileImageView.setImageBitmap(thumbnail);

    }
    public void viewOnMap(View view)
    {String[] places = {"Current location","Hyderabad","chennai","Banglore","Bhanur","Vasavi college of engineering"};
        double [] lat = new double[places.length];
        double [] lon = new double[places.length];
        for(int i=1;i<places.length;i++)
        {
            locationHandler.getLatLongFromAddress(places[i],this);
            lat[i] = locationHandler.getLatitude();
            lon[i]=locationHandler.getLongitude();
        }
        Bundle bundle = new Bundle();
        bundle.putDoubleArray("latitudes", lat);
        bundle.putDoubleArray("longitudes", lon);
        bundle.putStringArray("places",places);
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtras(bundle);
        startActivity(i);
    }

}