package com.example.ungdungchatrealtime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    /**
     * Chuyển đổi Uri thành đối tượng File bằng cách tạo một file tạm (Temporary File).
     * Cách này hoạt động ổn định trên tất cả các phiên bản Android (từ cũ đến mới nhất).
     */
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            // Lấy tên file gốc
            String fileName = getFileName(context, uri);
            if (fileName == null) fileName = "temp_file";

            // Tạo file tạm trong bộ nhớ cache của ứng dụng
            File tempFile = new File(context.getCacheDir(), fileName);
            tempFile.createNewFile();

            // Sao chép dữ liệu từ Uri vào file tạm
            try (FileOutputStream out = new FileOutputStream(tempFile);
                 InputStream in = context.getContentResolver().openInputStream(uri)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }
            return tempFile;
        } catch (Exception e) {
            Log.e("FileUtils", "Lỗi chuyển đổi file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy tên file từ Uri (ví dụ: "hinhanh.jpg" hoặc "tailieu.pdf")
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}