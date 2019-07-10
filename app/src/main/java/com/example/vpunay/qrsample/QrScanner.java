package com.example.vpunay.qrsample;

import android.support.v4.app.Fragment;
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
    private TextView barcodeScannerLabel;
    private ImageView imageView;
    private Button button;
    private ProgressBar pgsBar;
    private ScannerOverlay scannerOverlay;
    private Status status = Status.LOADING;
    private String qrCode;

    enum Status {
        SUCCESS, FAIL, LOADING
    }

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
        barcodeScannerLabel = findViewById(R.id.barcodeScannerLabel);
        button = findViewById(R.id.qrbutton);
        imageView = findViewById(R.id.imageView);
        pgsBar = findViewById(R.id.pBar);
        scannerOverlay = findViewById(R.id.scannerOverlay);
        setUpActionBar();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status == Status.SUCCESS){
                    onBackPressed();
                }else if(status == Status.LOADING){
                    pgsBar.setVisibility(View.INVISIBLE);
                    callFragment();
                }else{
                    callFragment();
                }
            }
        });
    }

    private void callFragment(){
        //call current fragment
        final Fragment scannerFragment = this.getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
        final Scanner scanner = (Scanner) scannerFragment;
        if(scanner != null){
            scanner.goBackScanner();
            scanner.stopHandler();
            changeUiWhenBackToScanner();
        }
        status = Status.LOADING;
    }

    private void changeUiWhenBackToScanner(){
        button.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        barcodeScannerLabel.setText(getString(R.string.smart_login_label_below_square));
        scannerOverlay.setVisibility(View.VISIBLE);
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
        status = (validateQrCode(qrCode)) ? Status.SUCCESS : Status.FAIL;
        final int drawable = status == Status.SUCCESS ? R.drawable.check : R.drawable.round_warning;
        final String labelText = status == Status.SUCCESS ? getString(R.string.smart_login_success_scan)
                : getString(R.string.smart_login_fail_scan);
        final String buttonText = status == Status.SUCCESS ? getString(R.string.smart_login_OK)
                : getString(R.string.smart_login_back);
        imageView.setImageResource(drawable);
        barcodeScannerLabel.setText(labelText);
        button.setText(buttonText);
    }

    public void setWhenStartLoading(final String qrCode){
        this.qrCode = qrCode;
        scannerOverlay.setVisibility(View.INVISIBLE);
        barcodeScannerLabel.setText(getString(R.string.smart_login_loading_label));
        button.setVisibility(View.VISIBLE);
        button.setText(getString(R.string.smart_login_button_cancel));
        pgsBar.setVisibility(View.VISIBLE);
    }

    //sample validation
    //if length of the data in the qr is 5 and above, it should be a success. (for prototype purposes)
    private boolean validateQrCode(final String qrCode){
        return qrCode.length() >= 5;
    }

    @Override
    public void startLoading(final String qrCode) {
        setWhenStartLoading(qrCode);
    }

    @Override
    public void loadIsDone() {
        setWhenLoadingIsDone();
    }

}
