package com.example.ungdungchatrealtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edtNewFullName, edtCurrentPassword, edtNewPassword;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtNewFullName = findViewById(R.id.edtNewFullName);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        edtNewFullName.setText(pref.getString("fullName", ""));

        btnSaveProfile.setOnClickListener(v -> updateUserProfile());
    }

    private void updateUserProfile() {
        String newName = edtNewFullName.getText().toString().trim();
        String currentPass = edtCurrentPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();

        if (currentPass.isEmpty()) {
            Toast.makeText(this, "Nhập mật khẩu cũ để xác nhận!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = pref.getInt("userId", -1);

        // Tạo object User chứa dữ liệu cập nhật
        User updateData = new User();
        updateData.oldPassword = currentPass; // Lỗi 'cannot find symbol' sẽ hết nếu User.java đã có trường này
        updateData.fullName = newName;

        // Chỉ gửi mật khẩu mới nếu người dùng có nhập
        if (!newPass.isEmpty()) {
            updateData.password = newPass;
        }

        RetrofitClient.getApiService().updateProfile(userId, updateData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Cập nhật lại SharedPreferences để các màn hình khác (MainActivity) không bị null
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("fullName", newName);
                    editor.apply();

                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Mật khẩu xác nhận sai hoặc lỗi server!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}