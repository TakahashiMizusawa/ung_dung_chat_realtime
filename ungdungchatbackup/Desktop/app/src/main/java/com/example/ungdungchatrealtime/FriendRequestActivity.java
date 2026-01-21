package com.example.ungdungchatrealtime;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRequestActivity extends AppCompatActivity {
    private RecyclerView rvRequests;
    private FriendRequestAdapter adapter;
    private List<ChatUser> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        // Ánh xạ View
        rvRequests = findViewById(R.id.rvRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách trống để tránh NullPointerException
        requestList = new ArrayList<>();

        loadRequests();
    }

    public void loadRequests() {
        // Lấy userId của chính mình từ SharedPreferences
        int myId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);

        if (myId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().getPendingRequests(myId).enqueue(new Callback<List<ChatUser>>() {
            @Override
            public void onResponse(Call<List<ChatUser>> call, Response<List<ChatUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList = response.body();

                    // Thiết lập Adapter và truyền vào Activity để Adapter có thể gọi ngược lại hàm loadRequests
                    adapter = new FriendRequestAdapter(requestList, FriendRequestActivity.this);
                    rvRequests.setAdapter(adapter);
                } else {
                    Log.e("API_ERROR", "Response không thành công");
                }
            }

            @Override
            public void onFailure(Call<List<ChatUser>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(FriendRequestActivity.this, "Lỗi kết nối dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm bổ trợ để Adapter gọi khi xử lý xong một lời mời
    public void refreshData() {
        loadRequests();
    }
}