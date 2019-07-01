package com.example.vpunay.qrsample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import jp.wasabeef.blurry.Blurry;


public class QrScanner extends AppCompatActivity {
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private TextView barcodeValue;
    private ImageView blur;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    
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

  /*  public Bitmap createBlurBitmap() {
        View viewContainer = findViewById(R.id.surface_view);
        Bitmap bitmap = captureView(viewContainer);
        if (bitmap != null) {
            blurBitmapWithRenderscript(
                    RenderScript.create(this),
                    bitmap);
        }
        return bitmap;
    }*/

    public void blurBitmapWithRenderscript(
            RenderScript rs, Bitmap bitmap2) {
        // this will blur the bitmapOriginal with a radius of 25
        // and save it in bitmapOriginal
        // use this constructor for best performance, because it uses
        // USAGE_SHARED mode which reuses memory
        final Allocation input =
                Allocation.createFromBitmap(rs, bitmap2);
        final Allocation output = Allocation.createTyped(rs,
                input.getType());
        final ScriptIntrinsicBlur script =
                ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        // must be >0 and <= 25
        script.setRadius(25f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap2);
    }

    public Bitmap captureView(View view) {
        //Create a Bitmap with the same dimensions as the View
        Bitmap image = Bitmap.createBitmap(1000,
                1000,
                Bitmap.Config.ARGB_4444); //reduce quality
        //Draw the view inside the Bitmap
        Canvas canvas = new Canvas(image);
        view.draw(canvas);

        //Make it frosty
        Paint paint = new Paint();
        paint.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        ColorFilter filter =
                new LightingColorFilter(0xFFFFFFFF, 0x00222222); // lighten
        //ColorFilter filter =
        //   new LightingColorFilter(0xFF7F7F7F, 0x00000000); // darken
        paint.setColorFilter(filter);
        canvas.drawBitmap(image, 0, 0, paint);
        return image;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
    // cameraView = findViewById(R.id.preview);
        // cameraView = findViewById(R.id.surface_view);
       barcodeValue = findViewById(R.id.barcode_value);
    //    blur = findViewById(R.id.blur);
         mPreview = findViewById(R.id.preview);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "NI SUD PARA PERMISSION NEED DAW? ", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
            return;
        }

       // cameraView = findViewById(R.id.surface_view);
        setUpActionBar();
/*        Bitmap mBlurBitmap = createBlurBitmap();
        blur.setImageBitmap(mBlurBitmap);*/

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        //barcodeDetector.setProcessor();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1200, 1080)
                .setAutoFocusEnabled(true) //you should add this feature, will blur if not implemented
                .build();

        mGraphicOverlay = findViewById(R.id.graphicc);

       try {
          mPreview.start(cameraSource, mGraphicOverlay);
        } catch (IOException e) {
           e.printStackTrace();
       }

/*
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    //noinspection MissingPermission
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });*/

        barcodeDetector.setProcessor(new Detector.Processor() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if ( barcodes != null && barcodes.size() != 0) {
                    barcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            //Update barcode value to TextView
                            barcodeValue.setText(barcodes.valueAt(0).displayValue);
                            Toast.makeText(QrScanner.this, " = "+barcodes.valueAt(0).displayValue, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
       // setUpBlur();
    }

    private void setUpBlur() {
     //   final ConstraintLayout constraintLayout = findViewById(R.id.surface_view);
    //    Blurry.with(QrScanner.this).radius(500).onto((ViewGroup) findViewById(R.id.relative));
    }

    @Override
    protected void onDestroy() {
        CameraSourcePreview cameraSourcePreview;
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }
}
