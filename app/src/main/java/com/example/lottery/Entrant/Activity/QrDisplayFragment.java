package com.example.lottery.Entrant.Activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lottery.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QrDisplayFragment extends Fragment {

    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_qr_display, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        ImageView ivQrCode = view.findViewById(R.id.ivQrCode);

        if (eventId != null && !eventId.trim().isEmpty()) {
            String qrPayload = "EVENT_ID:" + eventId.trim();
            generateQrCode(qrPayload, ivQrCode);
        } else {
            Toast.makeText(getContext(), "Event ID missing", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void generateQrCode(String data, ImageView imageView) {
        int width = 500;
        int height = 500;

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            Toast.makeText(getContext(), "QR Generation Failed", Toast.LENGTH_SHORT).show();
        }
    }
}