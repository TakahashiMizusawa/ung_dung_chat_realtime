package com.example.ungdungchatrealtime;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuenMatKhauActivity extends AppCompatActivity {

    private EditText edUserForgot, edSecurityAnswerForgot, edNewPassword;
    private Button btnCheckUser, btnResetPassword;
    private TextView tvSecurityQuestionDisplay;
    private LinearLayout layoutSecurityStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quen_mat_khau);

        initViews();
        setupEvents();
    }

    private void initViews() {
        edUserForgot = findViewById(R.id.edUserForgot);
        btnCheckUser = findViewById(R.id.btnCheckUser);

        layoutSecurityStep = findViewById(R.id.layoutSecurityStep);
        tvSecurityQuestionDisplay = findViewById(R.id.tvSecurityQuestionDisplay);
        edSecurityAnswerForgot = findViewById(R.id.edSecurityAnswerForgot);
        edNewPassword = findViewById(R.id.edNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    private void setupEvents() {
        // Bước 1: Kiểm tra User để lấy câu hỏi bảo mật
        btnCheckUser.setOnClickListener(v -> {
            String username = edUserForgot.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            RetrofitClient.getApiService().getSecurityQuestion(username).enqueue(new Callback<SecurityQuestionResponse>() {
                @Override
                public void onResponse(Call<SecurityQuestionResponse> call, Response<SecurityQuestionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tvSecurityQuestionDisplay.setText("Câu hỏi: " + response.body().getQuestion());
                        layoutSecurityStep.setVisibility(View.VISIBLE);
                        btnCheckUser.setVisibility(View.GONE);
                        edUserForgot.setEnabled(false);
                    } else {
                        Toast.makeText(QuenMatKhauActivity.this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SecurityQuestionResponse> call, Throwable t) {
                    Toast.makeText(QuenMatKhauActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Bước 2: Xác nhận câu trả lời và đặt lại mật khẩu
        btnResetPassword.setOnClickListener(v -> {
            String username = edUserForgot.getText().toString().trim();
            String answer = edSecurityAnswerForgot.getText().toString().trim();
            String newPass = edNewPassword.getText().toString().trim();

            if (answer.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            ResetPasswordRequest request = new ResetPasswordRequest(username, answer, newPass);
            RetrofitClient.getApiService().resetPassword(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(QuenMatKhauActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Quay lại màn hình đăng nhập
                    } else {
                        Toast.makeText(QuenMatKhauActivity.this, "Câu trả lời sai!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(QuenMatKhauActivity.this, "Lỗi server!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}