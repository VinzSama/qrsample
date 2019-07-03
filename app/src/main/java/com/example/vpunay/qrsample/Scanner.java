package com.example.vpunay.qrsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private GraphicOverlay overlay;
    private ConstraintLayout constraintLayout;

    public Scanner() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        overlay = view.findViewById(R.id.overlay);
        mPreview = view.findViewById(R.id.preview);
        constraintLayout = view.findViewById(R.id.innerlayout);

        barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
                .setRequestedPreviewSize(1200, 1080)
                .setAutoFocusEnabled(true) //you should add this feature, will blur if not implemented
                .build();

        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
            }
            mPreview.start(cameraSource, overlay);
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
                    //Sample loading
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            takePicture(barcodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });

        return view;
    }

    public interface OnQrScan{
         void startLoading();
         void loadIsDone();
    }

    private void takePicture(final String barcode){

        cameraSource.takePicture(null, new CameraSource.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] bytes) {
                ((OnQrScan) getActivity()).startLoading();
                //call activity to set up views when a qr code is scanned
                mPreview.setVisibility(View.INVISIBLE);
                mPreview.stop();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                final Matrix rotateMatrix = new Matrix();
                rotateMatrix.postRotate(90);

                final Bitmap rotatedBitmap  =  Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(),
                        rotateMatrix, false);

                constraintLayout.setBackground(new BitmapDrawable(getResources(), rotatedBitmap));
                setUpBlur(constraintLayout);

                //sample loading
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //call activity to set up views when after loading
                        ((OnQrScan) getActivity()).loadIsDone();
                    }
                }, 3000);
                mPreview.release();
                barcodeDetector.release();
                try {
                    //FIX ME
                    cameraSource.release();
                } catch (NullPointerException e) {

                }
            }
        });
    }

    private void setUpBlur(final ViewGroup viewGroup) {
        if (viewGroup != null) {
            //mPreview.setVisibility(View.INVISIBLE);
            Blurry.with(getContext()).radius(25).onto(viewGroup);
            viewGroup.setAlpha(0.5f);
        }
    }

    @Override
     public void onDestroy() {
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
