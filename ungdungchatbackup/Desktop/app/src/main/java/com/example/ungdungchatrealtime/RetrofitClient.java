package com.example.ungdungchatrealtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            // 1. Cấu hình Gson linh hoạt để tránh lỗi ép kiểu (đặc biệt là Timestamp)
            Gson gson = new GsonBuilder()
                    .setLenient() // Cho phép đọc JSON không nghiêm ngặt
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Khớp định dạng DateTime của C#
                    .create();

            // 2. Tạo OkHttpClient với đầy đủ Header
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("ngrok-skip-browser-warning", "69420")
                                .header("User-Agent", "android-app")
                                .header("Accept", "application/json; charset=utf-8")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            // 3. Xây dựng Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://hilario-unelongated-horacio.ngrok-free.dev/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Dùng Gson đã cấu hình
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}