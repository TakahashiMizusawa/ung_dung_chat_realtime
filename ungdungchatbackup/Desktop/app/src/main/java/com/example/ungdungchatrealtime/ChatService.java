package com.example.ungdungchatrealtime;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import java.util.Random;

public class ChatService extends Service {
    private HubConnection hubConnection;
    private static final String CHANNEL_ID = "CHAT_SERVICE_CHANNEL";
    private final String BASE_URL = "https://hilario-unelongated-horacio.ngrok-free.dev/";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        // Chạy Foreground Service để duy trì kết nối Socket ổn định
        startForeground(1, getForegroundNotification());
        setupSignalR();
    }

    private void setupSignalR() {
        int myId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);
        if (myId == -1) return;

        // CẬP NHẬT: Truyền userId qua Query String để khớp với CustomUserIdProvider ở Backend
        hubConnection = HubConnectionBuilder.create(BASE_URL + "chatHub?userId=" + myId).build();

        // Lắng nghe tin nhắn mới
        hubConnection.on("ReceiveMessage", (senderId, message, messageId) -> {
            // Nếu message bắt đầu bằng uploads/, hiển thị là [Hình ảnh] hoặc [Tệp tin]
            String displayContent = message;
            if (message.startsWith("uploads/chat/")) {
                displayContent = "[Hình ảnh/Tài liệu]";
            }
            showChatNotification("Tin nhắn mới", displayContent, "MESSAGE");
        }, Integer.class, String.class, Integer.class);

        // Lắng nghe lời mời kết bạn
        hubConnection.on("ReceiveFriendRequest", (senderName) -> {
            showChatNotification("Lời mời kết bạn 🤝", senderName + " đã gửi một lời mời cho bạn!", "FRIEND");
        }, String.class);

        hubConnection.start().subscribe(
                () -> Log.i("SignalR_Service", "Service đã kết nối thành công!"),
                throwable -> Log.e("SignalR_Service", "Kết nối thất bại: " + throwable.getMessage())
        );
    }

    private void showChatNotification(String title, String content, String type) {
        // Điều hướng người dùng khi nhấn vào thông báo
        Intent intent = new Intent(this, type.equals("FRIEND") ? FriendRequestActivity.class : MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager manager = getSystemService(NotificationManager.class);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        if (manager != null) manager.notify(new Random().nextInt(), notification);
    }

    private Notification getForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Hệ thống DESTINY")
                .setContentText("Đang chạy ngầm để nhận tin nhắn...")
                .setSmallIcon(R.drawable.ic_message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Hệ thống tin nhắn", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (hubConnection != null) hubConnection.stop();
        super.onDestroy();
    }
}