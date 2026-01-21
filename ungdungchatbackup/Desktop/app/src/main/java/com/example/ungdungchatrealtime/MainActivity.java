package com.example.ungdungchatrealtime;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.squareup.picasso.Picasso;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnMenu, btnSearch, btnNotification;
    private View viewRedDot, mainContentView;
    private CircleImageView imgAvatarHeader;
    private TextView tvUserNameHeader;
    private RecyclerView rvChatList;
    private ChatAdapter chatAdapter;
    private List<ChatUser> chatUserList;
    private HubConnection hubConnection;
    private int myId;

    private final String BASE_URL = "https://hilario-unelongated-horacio.ngrok-free.dev/";
    private static final String CHANNEL_ID = "CHAT_REALTIME_NOTIFY";

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    uploadImageToServer(result.getData().getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);

        // --- 1. KIỂM TRA MÃ PIN ---
        boolean needPin = getIntent().getBooleanExtra("NEED_PIN", false);
        if (needPin) {
            showPinDialog();
        }

        startChatService();
        checkNotificationPermission();
        initViews();
        createNotificationChannel();
        setupRecyclerView();
        setupEvents();
        setupNavigationAnimation();
        setupSignalR(); // Đã cập nhật cơ chế kết nối
    }

    private void setupSignalR() {
        if (myId == -1) return;

        // CẬP NHẬT: Kết nối kèm userId qua Query String để khớp với Backend
        hubConnection = HubConnectionBuilder.create(BASE_URL + "chatHub?userId=" + myId).build();

        hubConnection.on("ReceiveMessage", (senderId, message, messageId) -> {
            runOnUiThread(() -> {
                String displayContent = message.startsWith("uploads/chat/") ? "[Hình ảnh/Tài liệu]" : message;
                for (ChatUser user : chatUserList) {
                    if (user.id == senderId) {
                        user.hasNewMessage = true;
                        user.lastMessage = displayContent;
                        // Chỉ hiển thị thông báo nếu app đang không trong cuộc hội thoại với người đó
                        showStylishNotification("Tin nhắn mới", user.fullName + ": " + displayContent, "MESSAGE");
                        break;
                    }
                }
                chatAdapter.notifyDataSetChanged();
            });
        }, Integer.class, String.class, Integer.class);

        hubConnection.on("ReceiveFriendRequest", (senderName) -> {
            runOnUiThread(() -> {
                viewRedDot.setVisibility(View.VISIBLE);
                showStylishNotification("Lời mời kết bạn 🤝", senderName + " gửi lời mời!", "FRIEND");
            });
        }, String.class);

        hubConnection.start().subscribe(
                () -> Log.i("SignalR_Main", "Kết nối thành công!"),
                t -> Log.e("SignalR_Main", "Lỗi kết nối: " + t.getMessage())
        );
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---
    private void showPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        View v = getLayoutInflater().inflate(R.layout.dialog_pin_code, null);
        builder.setView(v).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                finishAffinity();
                return true;
            }
            return false;
        });
        EditText edPinInput = v.findViewById(R.id.edPinInput);
        Button btnConfirm = v.findViewById(R.id.btnConfirmPin);
        String savedPin = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("pinCode", "");
        btnConfirm.setOnClickListener(view -> {
            String input = edPinInput.getText().toString();
            if (input.equals(savedPin)) {
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Mã PIN sai!", Toast.LENGTH_SHORT).show();
                edPinInput.setText("");
            }
        });
        dialog.show();
    }

    private void startChatService() {
        Intent serviceIntent = new Intent(this, ChatService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnMenu = findViewById(R.id.btnMenu);
        btnSearch = findViewById(R.id.btnSearch);
        btnNotification = findViewById(R.id.btnNotification);
        viewRedDot = findViewById(R.id.viewRedDot);
        rvChatList = findViewById(R.id.rvChatList);
        mainContentView = findViewById(R.id.main_content_layout);
        View headerView = navigationView.getHeaderView(0);
        imgAvatarHeader = headerView.findViewById(R.id.imgAvatarHeader);
        tvUserNameHeader = headerView.findViewById(R.id.tvUserNameHeader);
        imgAvatarHeader.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Thông báo Chat", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void showStylishNotification(String title, String content, String type) {
        Intent intent = new Intent(this, type.equals("FRIEND") ? FriendRequestActivity.class : MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        try {
            NotificationManagerCompat.from(this).notify(new Random().nextInt(), builder.build());
        } catch (SecurityException e) { e.printStackTrace(); }
    }

    private void setupRecyclerView() {
        chatUserList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatUserList, this, user -> showDeleteFriendDialog(user));
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(chatAdapter);
    }

    private void showDeleteFriendDialog(ChatUser user) {
        new AlertDialog.Builder(this).setTitle("Xóa bạn bè").setMessage("Xóa " + user.fullName + "?")
                .setPositiveButton("Xóa", (d, w) -> performDeleteFriend(user.id)).setNegativeButton("Hủy", null).show();
    }

    private void performDeleteFriend(int friendId) {
        RetrofitClient.getApiService().deleteFriend(myId, friendId).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) loadFriendList();
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private void setupEvents() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnSearch.setOnClickListener(v -> showFindFriendDialog());
        btnNotification.setOnClickListener(v -> {
            viewRedDot.setVisibility(View.GONE);
            startActivity(new Intent(this, FriendRequestActivity.class));
        });
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) startActivity(new Intent(this, EditProfileActivity.class));
            else if (id == R.id.nav_logout) performLogout();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void showFindFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_find_friend, null);
        builder.setView(v);
        EditText ed = v.findViewById(R.id.edFindUsername);
        ImageView btn = v.findViewById(R.id.btnConfirmAdd);
        LinearLayout res = v.findViewById(R.id.layoutResult);
        TextView name = v.findViewById(R.id.tvFoundName);
        AlertDialog dialog = builder.create();
        btn.setOnClickListener(view -> {
            RetrofitClient.getApiService().searchUser(ed.getText().toString()).enqueue(new Callback<List<ChatUser>>() {
                @Override public void onResponse(Call<List<ChatUser>> call, Response<List<ChatUser>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        ChatUser u = response.body().get(0);
                        res.setVisibility(View.VISIBLE);
                        name.setText(u.fullName);
                        res.setOnClickListener(v1 -> {
                            sendFriendRequest(u.id);
                            dialog.dismiss();
                        });
                    }
                }
                @Override public void onFailure(Call<List<ChatUser>> call, Throwable t) {}
            });
        });
        dialog.show();
    }

    private void sendFriendRequest(int rId) {
        String myName = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("fullName", "Ai đó");
        RetrofitClient.getApiService().sendFriendRequest(myId, rId).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                    hubConnection.send("SendFriendRequest", rId, myName);
                    Toast.makeText(MainActivity.this, "Đã gửi lời mời!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private void loadUserData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        tvUserNameHeader.setText(pref.getString("fullName", ""));
        String avt = pref.getString("avatar", "");
        if (!avt.isEmpty()) Picasso.get().load(BASE_URL + avt).into(imgAvatarHeader);
    }

    private void loadFriendList() {
        RetrofitClient.getApiService().getFriendList(myId).enqueue(new Callback<List<ChatUser>>() {
            @Override public void onResponse(Call<List<ChatUser>> call, Response<List<ChatUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatUserList.clear(); chatUserList.addAll(response.body());
                    chatAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<ChatUser>> call, Throwable t) {}
        });
    }

    private void uploadImageToServer(Uri uri) {
        try {
            File f = new File(getCacheDir(), "temp.jpg");
            InputStream is = getContentResolver().openInputStream(uri);
            OutputStream os = new FileOutputStream(f);
            byte[] buf = new byte[1024]; int len;
            while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
            os.close(); is.close();
            RequestBody rb = RequestBody.create(MediaType.parse("image/*"), f);
            MultipartBody.Part b = MultipartBody.Part.createFormData("file", f.getName(), rb);
            RetrofitClient.getApiService().updateAvatar(myId, b).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show();
                        loadUserData();
                    }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void performLogout() {
        stopService(new Intent(this, ChatService.class));
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, dangnhap.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void setupNavigationAnimation() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override public void onDrawerSlide(View v, float o) { mainContentView.setTranslationX(v.getWidth() * o); }
        });
    }

    @Override protected void onResume() { super.onResume(); loadUserData(); loadFriendList(); }
    @Override protected void onDestroy() { super.onDestroy(); if (hubConnection != null) hubConnection.stop(); }
}