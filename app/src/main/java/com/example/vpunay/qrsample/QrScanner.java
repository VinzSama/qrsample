package com.example.vpunay.qrsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class QrScanner extends AppCompatActivity implements Scanner.OnQrScan{
    private TextView barcodeValue;
    private ImageView imageView;
    private Button button;
    private ProgressBar pgsBar;
    private ScannerOverlay scannerOverlay;
    private String qrCode;
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
        barcodeValue = findViewById(R.id.barcodeScannerLabel);
        button = findViewById(R.id.qrbutton);
        imageView = findViewById(R.id.imageView);
        pgsBar = findViewById(R.id.pBar);
        scannerOverlay = findViewById(R.id.scannerOverlay);
        setUpActionBar();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpActionBar() {
        final Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_button);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setWhenLoadingIsDone(){
        pgsBar.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.check);
        barcodeValue.setText(getString(R.string.smart_login_success_scan));
        button.setText(getString(R.string.smart_login_OK));
    }

    public void setWhenStartLoading(final String qrCode){
        scannerOverlay.setVisibility(View.INVISIBLE);
        barcodeValue.setText(getString(R.string.smart_login_loading_label));
        button.setVisibility(View.VISIBLE);
        pgsBar.setVisibility(View.VISIBLE);
        qrCodeOperations(qrCode);
    }

    @Override
    public void startLoading(final String qrCode) {
        setWhenStartLoading(qrCode);
    }

    public void qrCodeOperations(final String qrCode){
        //Temporary
        this.qrCode = qrCode;
    }

    @Override
    public void loadIsDone() {
        setWhenLoadingIsDone();
    }
}
