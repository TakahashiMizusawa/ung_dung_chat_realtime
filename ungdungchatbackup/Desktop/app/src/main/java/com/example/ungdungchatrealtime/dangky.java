package com.example.ungdungchatrealtime;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class dangky extends AppCompatActivity {
    private EditText edUsername, edFullName, edPassword, edConfirmPassword;
    private EditText edSecurityQuestion, edSecurityAnswer, edPinCode;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangky);

        initViews();

        btnRegister.setOnClickListener(v -> {
            String u = edUsername.getText().toString().trim();
            String f = edFullName.getText().toString().trim();
            String p = edPassword.getText().toString().trim();
            String cp = edConfirmPassword.getText().toString().trim();
            String sq = edSecurityQuestion.getText().toString().trim();
            String sa = edSecurityAnswer.getText().toString().trim();
            String pin = edPinCode.getText().toString().trim();

            if (u.isEmpty() || f.isEmpty() || p.isEmpty() || cp.isEmpty() || sq.isEmpty() || sa.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!p.equals(cp)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pin.length() != 4) {
                Toast.makeText(this, "Mã PIN phải đủ 4 số", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(u, f, p, sq, sa, pin);
        });
    }

    private void initViews() {
        edUsername = findViewById(R.id.edUsername);
        edFullName = findViewById(R.id.edFullName);
        edPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);
        edSecurityQuestion = findViewById(R.id.edSecurityQuestion);
        edSecurityAnswer = findViewById(R.id.edSecurityAnswer);
        edPinCode = findViewById(R.id.edPinCode);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void registerUser(String u, String f, String p, String sq, String sa, String pin) {
        User userObj = new User();
        userObj.username = u;
        userObj.fullName = f;
        userObj.password = p;
        userObj.securityQuestion = sq;
        userObj.securityAnswer = sa;
        userObj.pinCode = pin;

        RetrofitClient.getApiService().register(userObj).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(dangky.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(dangky.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(dangky.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}