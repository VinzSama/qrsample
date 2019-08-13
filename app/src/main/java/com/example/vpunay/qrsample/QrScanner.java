package com.example.vpunay.qrsample;

import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vpunay.qrsample.databinding.ActivityQrScannerBinding;

public class QrScanner extends AppCompatActivity implements Scanner.OnQrScan{
    private TextView barcodeScannerLabel;
    private ScannerOverlay scannerOverlay;
    private Status status = Status.LOADING;
    private String qrCode;
    private UI ui;

    enum Status {
        SUCCESS, FAIL, LOADING
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// app icon in action bar clicked; go home
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityQrScannerBinding qrScannerBinding = DataBindingUtil.setContentView(this, R.layout.activity_qr_scanner);
        ui = new UI();
        ui.setLabel(getString(R.string.smart_login_label_below_square));
        qrScannerBinding.setUi(ui);

        barcodeScannerLabel = findViewById(R.id.barcodeScannerLabel);

        scannerOverlay = findViewById(R.id.scannerOverlay);
        setUpActionBar();

        qrScannerBinding.qrbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status == Status.SUCCESS){
                    onBackPressed();
                }else if(status == Status.LOADING){
                    ui.setLoadingScreenVisible(false);
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
        ui.setButtonVisible(false);
        ui.setShowLogo(true);
        ui.setLabel(getString(R.string.smart_login_label_below_square));
        ui.setShowOverlay(true);
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
        ui.setLoadingScreenVisible(false);
        ui.setShowLogo(true);
        status = (validateQrCode(qrCode)) ? Status.SUCCESS : Status.FAIL;
        final int drawable = status == Status.SUCCESS ? R.drawable.check : R.drawable.round_warning;
        final String labelText = status == Status.SUCCESS ? getString(R.string.smart_login_success_scan)
                : getString(R.string.smart_login_fail_scan);
        final String buttonText = status == Status.SUCCESS ? getString(R.string.smart_login_OK)
                : getString(R.string.smart_login_back);
        ui.setLogo(drawable);
        ui.setLabel(labelText);
        ui.setButtonLabel(buttonText);
    }

    @BindingAdapter("logo")
    public static void setImageResource(final ImageView imageView, final int resource){
        imageView.setImageResource(resource);
    }

    public void setWhenStartLoading(final String qrCode){
        this.qrCode = qrCode;
        ui.setShowOverlay(false);
        ui.setLabel(getString(R.string.smart_login_loading_label));
        ui.setButtonVisible(true);
        ui.setButtonVisible(true);
        ui.setButtonLabel(getString(R.string.smart_login_button_cancel));
        ui.setLoadingScreenVisible(true);
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
