package com.example.ungdungchatrealtime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class dangnhap extends AppCompatActivity {
    private EditText edUser, edPass;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- KIỂM TRA TỰ ĐỘNG ĐĂNG NHẬP ---
        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        if (pref.getBoolean("isLoggedIn", false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("NEED_PIN", true); // Yêu cầu nhập PIN khi vào lại app
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.dangnhap);
        initViews();
        setupEvents();
    }

    private void initViews() {
        edUser = findViewById(R.id.edUserLogin);
        edPass = findViewById(R.id.edPassLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword); // Ánh xạ nút quên mật khẩu
    }

    private void setupEvents() {
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, dangky.class)));

        // Chuyển sang màn hình Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, QuenMatKhauActivity.class));
        });

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String u = edUser.getText().toString().trim();
        String p = edPass.getText().toString().trim();
        if (u.isEmpty() || p.isEmpty()) return;

        RetrofitClient.getApiService().login(new LoginRequest(u, p)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putInt("userId", user.id);
                    editor.putString("fullName", user.fullName);
                    editor.putString("avatar", user.avatar);

                    // LƯU MÃ PIN TỪ SERVER TRẢ VỀ
                    editor.putString("pinCode", user.pinCode);

                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Intent intent = new Intent(dangnhap.this, MainActivity.class);
                    intent.putExtra("NEED_PIN", true); // Bắt nhập PIN ngay sau khi đăng nhập
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(dangnhap.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(dangnhap.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}