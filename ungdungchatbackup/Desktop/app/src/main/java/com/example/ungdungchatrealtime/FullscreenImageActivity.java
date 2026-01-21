package com.example.ungdungchatrealtime;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class FullscreenImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        PhotoView photoView = findViewById(R.id.photoView);
        ImageButton btnClose = findViewById(R.id.btnClose);
        ImageButton btnDownload = findViewById(R.id.btnDownloadFull);

        String imageUrl = getIntent().getStringExtra("IMAGE_URL");

        // Load ảnh vào PhotoView
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(photoView);
        }

        // Đóng màn hình
        btnClose.setOnClickListener(v -> finish());

        // Tải ảnh xuống
        btnDownload.setOnClickListener(v -> {
            if (imageUrl != null) {
                String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
                startDownload(imageUrl, fileName);
            }
        });
    }

    private void startDownload(String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(fileName);
        request.setDescription("Đang tải ảnh xuống...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Bắt đầu tải xuống...", Toast.LENGTH_SHORT).show();
        }
    }
}