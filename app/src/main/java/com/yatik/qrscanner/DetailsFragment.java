package com.yatik.qrscanner;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

public class DetailsFragment extends Fragment {

    private List<Barcode> mBarcodes;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public DetailsFragment(List<Barcode> barcodes) {
        this.mBarcodes = barcodes;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView typeIcon = view.findViewById(R.id.type_icon);
        TextView typeText = view.findViewById(R.id.type_text);
        TextView decodedText = view.findViewById(R.id.decoded_text);
        ImageButton copyButton = view.findViewById(R.id.copy_button);
        ImageButton shareButton = view.findViewById(R.id.share_button);
        ImageButton wifiButton = view.findViewById(R.id.wifi_button);
        ImageButton launchButton = view.findViewById(R.id.launch_button);

        Barcode barcode = mBarcodes.get(0);

//        Rect bounds = barcode.getBoundingBox();
//        Point[] corners = barcode.getCornerPoints();

        String rawValue = barcode.getRawValue();

        int valueType = barcode.getValueType();
        switch (valueType) {
            case Barcode.TYPE_WIFI:
                String ssid = barcode.getWifi().getSsid();
                String password = barcode.getWifi().getPassword();
                int type = barcode.getWifi().getEncryptionType();
                String encryptionType;
                if (type == Barcode.WiFi.TYPE_OPEN){
                    encryptionType = "Open";
                } else if (type == Barcode.WiFi.TYPE_WPA){
                    encryptionType = "WPA";
                } else {
                    encryptionType = "WEP";
                }
                typeIcon.setImageResource(R.drawable.outline_wifi_24);
                typeText.setText("Wifi");
                decodedText.setText(String.format("SSID:%s\n\nPassword: %s\n\nType: %s", ssid, password, encryptionType));
                launchButton.setVisibility(View.GONE);
                wifiButton.setVisibility(View.VISIBLE);
                wifiButton.setOnClickListener(this::onClick);

                break;

            case Barcode.TYPE_URL:
                String title = barcode.getUrl().getTitle();
                String url = barcode.getUrl().getUrl();
                typeIcon.setImageResource(R.drawable.outline_url_24);
                typeText.setText("Url");
                decodedText.setText(String.format("Title: %s\n\nUrl: %s", title, url));
                launchButton.setOnClickListener(view1 -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))));
                break;

            case Barcode.TYPE_TEXT:
                String text = barcode.getDisplayValue();
                typeIcon.setImageResource(R.drawable.outline_text_icon);
                typeText.setText("Text");
                decodedText.setText(text);
                assert text != null;
                if (text.startsWith("upi://pay")){
                    payViaUPI(text);
                    launchButton.setOnClickListener(view1 -> payViaUPI(text));
                    break;
                }
                launchButton.setOnClickListener(view1 -> shareData(text));
                break;

            case Barcode.TYPE_PHONE:
                String tel = barcode.getPhone().getNumber();
                typeIcon.setImageResource(R.drawable.outline_call_24);
                typeText.setText("Phone");
                decodedText.setText(String.format("Phone. No: %s",tel));
                launchButton.setOnClickListener(view1 -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel, null))));
                break;

            case Barcode.TYPE_GEO:
                double latitude = barcode.getGeoPoint().getLat();
                double longitude = barcode.getGeoPoint().getLng();
                typeIcon.setImageResource(R.drawable.outline_location_24);
                typeText.setText("Location");
                decodedText.setText(String.format("Latitude: %s\n\nLongitude: %s", latitude, longitude));
                launchButton.setOnClickListener(view1 -> {
                    Uri gmmIntentUri = Uri.parse(String.format("geo:%s,%s", latitude, longitude));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                });
                break;

            case Barcode.TYPE_SMS:
                String phoneNumber = barcode.getSms().getPhoneNumber();
                String message = barcode.getSms().getMessage();
                typeIcon.setImageResource(R.drawable.outline_sms_24);
                typeText.setText("SMS");
                decodedText.setText(String.format("Phone. No: %s\n\nMessage: %s", phoneNumber, message));
                launchButton.setOnClickListener(view1 -> shareData(message));

            default:
                typeIcon.setImageResource(R.drawable.outline_text_icon);
                typeText.setText("Raw");
                decodedText.setText(rawValue);
                launchButton.setOnClickListener(view1 -> shareData(rawValue));
        }

        copyButton.setOnClickListener(view1 -> {
            copyData((String) decodedText.getText());
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });
        shareButton.setOnClickListener(view1 -> {
            shareData((String) decodedText.getText());
        });
    }

    private void copyData(String text){
        ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("copied", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    private void shareData(String text){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void payViaUPI(String ID){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ID));
        Intent payIntent = Intent.createChooser(intent, "Pay with:");
        startActivity(payIntent);
    }

    private void onClick(View view1) {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }
}