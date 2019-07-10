package com.example.vpunay.qrsample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import jp.wasabeef.blurry.Blurry;

public class Scanner extends Fragment {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private CameraSourcePreview mPreview;
    private RelativeLayout constraintLayout;
    private GraphicOverlay graphicOverlay;
    private Bitmap bitmap;
    private Bitmap rotatedBitmap;
    private Context context;
    private boolean safeToTakePicture;
    //the handler for sample loading for prototyping purposes
    private Handler handler;

    public Scanner() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void goBackScanner() {
        onDestroy();
        constraintLayout.setBackground(null);
        Blurry.delete(constraintLayout);
        constraintLayout.setVisibility(View.INVISIBLE);
        setUpResources();
        startCamera();
        barCodeDetection();
        mPreview.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        mPreview = view.findViewById(R.id.preview);
        constraintLayout = view.findViewById(R.id.innerlayout);
        graphicOverlay = view.findViewById(R.id.overlay);
        setUpResources();
        startCamera();
        barCodeDetection();
        return view;
    }

    private void setUpResources(){
        barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(context, barcodeDetector)
                .setRequestedPreviewSize(getResources().getInteger(R.integer.smart_login_preview_height),
                        getResources().getInteger(R.integer.smart_login_preview_width))
                .setAutoFocusEnabled(true)
                .build();
    }

    //function to ask permission and start camera
    private void startCamera() {
        try {
            //Permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 1);
                }
            }else{
                mPreview.start(cameraSource, graphicOverlay);
                safeToTakePicture = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //function to start barcode detection
    private void barCodeDetection() {
        barcodeDetector.setProcessor(new Detector.Processor() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes != null && barcodes.size() != 0) {
                    for (int i = 0; i < barcodes.size(); i++) {
                        final Barcode barcode = barcodes.get(barcodes.keyAt(i));
                        if (barcode != null) {
                            //check if Qr code is inside overlay
                            final RectF rect = new RectF(barcode.getBoundingBox());
                            //translate qr position with overlay position x and y
                            rect.left = graphicOverlay.translateX(rect.left);
                            rect.top = graphicOverlay.translateY(rect.top);
                            rect.right = graphicOverlay.translateX(rect.right);
                            rect.bottom = graphicOverlay.translateY(rect.bottom);
                            //check if qr code is inside scanner border
                            if (ScannerOverlay.isQrCodeInsideOverlay(rect)) {
                                //start operations
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (safeToTakePicture) {
                                            startOperationsAfterQrIsScanned(barcodes.valueAt(0).displayValue);
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Interface to communicate with activity to update UI and start operations when qr is scanned
     */
    public interface OnQrScan {
        void startLoading(final String barcode);

        void loadIsDone();
    }

    private void startOperationsAfterQrIsScanned(final String barcode) {
        //take picture to make the scanned image as the background
        cameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                //call activity to set up views and validate qr code when a qr is scanned
                final OnQrScan activity = (OnQrScan) getActivity();
                if (activity != null) {
                    activity.startLoading(barcode);
                }
                //returned bitmap (it returns a landscape)
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                final Matrix rotateMatrix = new Matrix();
                //get value for rotation based on device
                //Kitkat and Oreo needs 360 rotation
                final int rotateValue = (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT
                        && android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) || android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                        ? getResources().getInteger(R.integer.smart_login_rotate_bitmap)
                        : getResources().getInteger(R.integer.smart_login_rotate_bitmap_otherdevices);
                //rotate image
                rotateMatrix.postRotate(rotateValue);
                //rotated image. bitmap returned by takePicture function returns landscape
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(),
                        rotateMatrix, false);
                //make the CamerSourcePreview invisible to avoid conflict from blurred background
                mPreview.setVisibility(View.INVISIBLE);
                constraintLayout.setVisibility(View.VISIBLE);
                //make cameraSourcePreview stop
                mPreview.stop();
                final BitmapDrawable bitmapDrawable = new BitmapDrawable(rotatedBitmap);
                //set the scanned image as background
                constraintLayout.setBackground(bitmapDrawable);
                //blur the background
                setUpBlur(constraintLayout);
                release();
                //sample loading
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Call activity when loading is done to update UI
                        if (activity != null) {
                            activity.loadIsDone();
                        }
                    }
                    //sample 3 seconds loading time
                }, 3000);
                safeToTakePicture = true;
            }
        });
        safeToTakePicture = false;

    }

    //to stop sample loading operations when cancel is clicked for prototyping purposes
    public void stopHandler(){
        handler.removeCallbacksAndMessages(null);
    }

    private void release() {
        mPreview.release();
        barcodeDetector.release();
        try {
            cameraSource.release();
        } catch (NullPointerException ignored) { }
    }

    private void setUpBlur(final ViewGroup viewGroup) {
        if (viewGroup != null) {
            //Make the layout blur
            Blurry.with(getActivity()).onto(viewGroup);
            //Make the layout transparent
            viewGroup.setAlpha(0.5f);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearBitmaps();
        release();
    }

    private void clearBitmaps() {
        //To prevent OutOfMemoryError
        if (bitmap != null) {
            bitmap.recycle();
        }
        if (rotatedBitmap != null) {
            rotatedBitmap.recycle();
        }
        //Help Garbage Collect
        rotatedBitmap = null;
        bitmap = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1: {
                if(grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED){
                    clearBitmaps();
                    release();
                    requireActivity().onBackPressed();
                }else{
                    startCamera();
                }
            }
        }
    }
}
