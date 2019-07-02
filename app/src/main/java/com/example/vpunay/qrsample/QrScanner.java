package com.example.vpunay.qrsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.wasabeef.blurry.Blurry;


public class QrScanner extends AppCompatActivity {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private TextView barcodeValue;
    private ImageView imageView;
    private CameraSourcePreview mPreview;
    private Button button;
    private FrameLayout qrBorder;
    private ProgressBar pgsBar;
    private ImageView image;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpActionBar() {
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        barcodeValue = findViewById(R.id.barcode_value);
        button = findViewById(R.id.qrbutton);
        qrBorder = findViewById(R.id.qrBorder);
        imageView = findViewById(R.id.imageView);
        pgsBar = findViewById(R.id.pBar);
        image = findViewById(R.id.blur);

        //    blur = findViewById(R.id.blur);
        mPreview = findViewById(R.id.preview);
        mPreview.setDrawingCacheEnabled(true);
        mPreview.buildDrawingCache(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            return;
        }

        setUpActionBar();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1200, 1080)
                .setAutoFocusEnabled(true) //you should add this feature, will blur if not implemented
                .build();

        try {
            mPreview.start(cameraSource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        barcodeDetector.setProcessor(new Detector.Processor() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes != null && barcodes.size() != 0) {
                    barcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            //Update barcode value to TextView
                            barcodeValue.setText("Please wait for smartlogin");
                            Toast.makeText(QrScanner.this, " = " + barcodes.valueAt(0).displayValue, Toast.LENGTH_SHORT).show();
                            button.setVisibility(View.VISIBLE);
                            qrBorder.setVisibility(View.INVISIBLE);

                            Bitmap sd = mPreview.getDrawingCache();
                            mPreview.stop();
                            pgsBar.setVisibility(View.VISIBLE);

                            //Sample loading
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pgsBar.setVisibility(View.INVISIBLE);
                                    imageView.setVisibility(View.VISIBLE);
                                    imageView.setImageResource(R.drawable.check);
                                    barcodeValue.setText("Successfully logged in to SmartLogin");
                                    int totalHeight = mPreview.getHeight();
                                    int totalWidth = mPreview.getChildAt(0).getWidth();
                                    Bitmap b = getBitmapFromView(mPreview, totalHeight, totalWidth);
                                    setUpBlur(b);
                                    button.setText("OK");

                                    mPreview.release();
                                    barcodeDetector.release();
                                    try {
                                        //FIX ME
                                        cameraSource.release();
                                    } catch (NullPointerException e) {

                                    }
                                    mPreview.setBackground(ContextCompat.getDrawable(QrScanner.this, R.drawable.borders));
                                }
                            }, 3000);
                        }
                    });
                }
            }
        });
    }

    private void takeScreenShot() {
        int totalHeight = mPreview.getHeight();
        int totalWidth = mPreview.getChildAt(0).getWidth();

        Bitmap b = getBitmapFromView(mPreview, totalHeight, totalWidth);
        //Save bitmap
        String extr = Environment.getExternalStorageDirectory() + "/Folder/";
        String fileName = "report.jpg";
        File myPath = new File(extr, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(this.getContentResolver(), b, "Screen", "screen");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {

        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    private void setUpBlur(final Bitmap bitmap) {
        if (image != null) {
            //mPreview.setVisibility(View.INVISIBLE);
            Blurry.with(this).radius(300).onto(mPreview);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPreview.release();
        barcodeDetector.release();
        try {
            //FIX ME
            cameraSource.release();
        } catch (NullPointerException e) {

        }
    }
}
