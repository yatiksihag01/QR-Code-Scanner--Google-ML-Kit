package com.yatik.qrscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.yatik.qrscanner.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private boolean isClickedAllowButton = false;
    private ActivityMainBinding binding;
    boolean doublePressToExit = false;
    private final int CAMERA_REQUEST_CODE = 100;
    ActivityResultLauncher<String> mChoosePhoto;
    private ProcessCameraProvider mCameraProvider;
    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        hideSystemBars();
        mCameraProviderFuture = ProcessCameraProvider.getInstance(this);

        requestCameraPermission();

        binding.selectFromGallery.setOnClickListener(view -> mChoosePhoto.launch("image/*"));

        mChoosePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            InputImage image;
            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build();
            BarcodeScanner scanner = BarcodeScanning.getClient(options);
            try {
                image = InputImage.fromFilePath(this, result);
                scanner.process(image)
                    .addOnSuccessListener(MainActivity.this::processResult)
                    .addOnFailureListener(e -> {
                        // Task failed with an exception
                        Toast.makeText(MainActivity.this, "Failed to scan.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isClickedAllowButton){
            requestCameraPermission();
            isClickedAllowButton = false;
        }
        if (binding.buttonFlashOff.getVisibility() == View.GONE) {
            binding.buttonFlashOn.setVisibility(View.GONE);
            binding.buttonFlashOff.setVisibility(View.VISIBLE);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setUpCamera();
        } else {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpCamera();
                } else {
                    String message = getResources().getString(R.string.permissionDeniedMessageCam);
                    noPermissionDialog(message);
                }
                break;
            //insert code here for other cases/permissions
        }
    }

    private void setUpCamera(){
        mCameraProviderFuture.addListener(() -> {
            try {
                mCameraProvider = mCameraProviderFuture.get();
                processScan(mCameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void noPermissionDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied!")
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(Html.fromHtml(getString(R.string.Cancel)), (((dialog, which) -> dialog.dismiss())))
                .setPositiveButton(Html.fromHtml(getString(R.string.Allow)), ((dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    isClickedAllowButton = true;
                }));

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    void processScan(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            @SuppressLint("UnsafeOptInUsageError") InputImage image =
                    InputImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            scanner.process(image)
                .addOnSuccessListener(MainActivity.this::processResult)
                .addOnFailureListener(e -> {
                    // Task failed with an exception
                    Toast.makeText(MainActivity.this, "Failed to scan.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                })
                .addOnCompleteListener(ContextCompat.getMainExecutor(this), e -> imageProxy.close());
        });

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        mCamera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
        flashControl();

    }

    private void processResult(List<Barcode> barcodes){
        if (!barcodes.isEmpty()) {

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, 125));
            } else{
                vibrator.vibrate(100);
            }

            mCameraProvider.unbindAll();

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_layout, new DetailsFragment(barcodes), null)
                    .setReorderingAllowed(true)
                    .addToBackStack("detailsFrag")
                    .commit();
        }
    }

    private void flashControl(){
        binding.buttonFlashOff.setOnClickListener(view -> {
            if (mCamera.getCameraInfo().hasFlashUnit()){
                mCamera.getCameraControl().enableTorch(true);
                binding.buttonFlashOff.setVisibility(View.GONE);
                binding.buttonFlashOn.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Sorry, Your phone don't have a flashlight", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonFlashOn.setOnClickListener(view -> {
            mCamera.getCameraControl().enableTorch(false);
            binding.buttonFlashOn.setVisibility(View.GONE);
            binding.buttonFlashOff.setVisibility(View.VISIBLE);
        });
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide the status bar
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());

        // Make navigation bar transparent
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
            binding.buttonFlashOn.setVisibility(View.GONE);
            binding.buttonFlashOff.setVisibility(View.VISIBLE);
            setUpCamera();
            return;
        }else if (doublePressToExit) {
            super.onBackPressed();
            return;
        }
        doublePressToExit = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doublePressToExit = false, 2000);
    }

}