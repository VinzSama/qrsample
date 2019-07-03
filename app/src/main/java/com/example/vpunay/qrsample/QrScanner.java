package com.example.vpunay.qrsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;;
import android.widget.ProgressBar;
import android.widget.TextView;


public class QrScanner extends AppCompatActivity implements Scanner.OnQrScan{

    private TextView barcodeValue;
    private ImageView imageView;
    private Button button;
    private FrameLayout qrBorder;
    private ProgressBar pgsBar;
    private ScannerOverlay scannerOverlay;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        barcodeValue = findViewById(R.id.barcode_value);
        button = findViewById(R.id.qrbutton);
        qrBorder = findViewById(R.id.qrBorder);
        imageView = findViewById(R.id.imageView);
        pgsBar = findViewById(R.id.pBar);
        scannerOverlay = findViewById(R.id.scannerOverlay);
        setUpActionBar();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
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

    public void setWhenLoadingIsDone(){
        pgsBar.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.check);
        barcodeValue.setText("Successfully logged in to SmartLogin");
        button.setText("OK");
    }

    public void setWhenStartLoading(){
        scannerOverlay.setVisibility(View.INVISIBLE);
        barcodeValue.setText("Please wait for smartlogin");
        button.setVisibility(View.VISIBLE);
        qrBorder.setVisibility(View.INVISIBLE);
        pgsBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void startLoading() {
        setWhenStartLoading();
    }

    @Override
    public void loadIsDone() {
        setWhenLoadingIsDone();
    }
}
