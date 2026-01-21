package com.example.ungdungchatrealtime;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    // Views
    private TextView tvReceiverName;
    private CircleImageView imgChatAvatar;
    private EditText edMessage;
    private ImageView btnSend, btnBack, btnEmote; // Đã bỏ btnAttachAll
    private RecyclerView rvMessages;
    private FrameLayout emojiAnimationContainer;

    // Logic
    private MessageAdapter messageAdapter;
    private List<MessageModel> messageList;
    private HubConnection hubConnection;
    private ApiService apiService;

    private int receiverId, myId;
    private String receiverName;
    private final String BASE_URL = "https://hilario-unelongated-horacio.ngrok-free.dev/";
    private static final String CHANNEL_ID = "CHAT_NOTIFY_CHANNEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        createNotificationChannel();
        requestNotificationPermission();

        receiverId = getIntent().getIntExtra("receiverId", -1);
        myId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);
        receiverName = getIntent().getStringExtra("receiverName");
        String avatar = getIntent().getStringExtra("receiverAvatar");

        tvReceiverName.setText(receiverName);
        if (avatar != null && !avatar.isEmpty()) {
            Picasso.get().load(BASE_URL + avatar).into(imgChatAvatar);
        }

        apiService = RetrofitClient.getApiService();
        setupRecyclerView();
        loadChatHistory();
        setupSignalR();

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> handleSendMessage());
        btnEmote.setOnClickListener(v -> showEmojiMenu());

        edMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSendMessage();
                return true;
            }
            return false;
        });
    }

    private void initViews() {
        tvReceiverName = findViewById(R.id.tvReceiverName);
        imgChatAvatar = findViewById(R.id.imgChatAvatar);
        edMessage = findViewById(R.id.edMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBackChat);
        btnEmote = findViewById(R.id.btnEmote);
        rvMessages = findViewById(R.id.rvMessages);
        emojiAnimationContainer = findViewById(R.id.emojiAnimationContainer);
    }

    private void setupSignalR() {
        hubConnection = HubConnectionBuilder.create(BASE_URL + "chatHub?userId=" + myId).build();

        hubConnection.on("ReceiveMessage", (senderId, message, messageId) -> {
            runOnUiThread(() -> {
                if (senderId == receiverId) {
                    addMessageWithId(senderId, message, messageId);
                    showStylishNotification(receiverName, message);
                } else if (senderId == myId) {
                    updateLastMessageId(messageId);
                }
            });
        }, Integer.class, String.class, Integer.class);

        hubConnection.on("ReceiveReaction", (messageId, reactionEmoji) -> {
            runOnUiThread(() -> {
                if (messageAdapter != null) messageAdapter.updateReaction(messageId, reactionEmoji);
                playEmojiAnimation(reactionEmoji);
            });
        }, Integer.class, String.class);

        hubConnection.start().subscribe(
                () -> Log.i("SignalR", "Connected!"),
                throwable -> Log.e("SignalR", "Connection Error: " + throwable.getMessage())
        );
    }

    private void handleSendMessage() {
        String msg = edMessage.getText().toString().trim();
        if (!msg.isEmpty()) {
            if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                hubConnection.send("SendMessage", myId, receiverId, msg);
                addMessageWithId(myId, msg, 0);
                edMessage.setText("");
            }
        }
    }

    private void addMessageWithId(int senderId, String content, int messageId) {
        MessageModel newMessage = new MessageModel(senderId, content, String.valueOf(System.currentTimeMillis()));
        newMessage.setId(messageId);
        messageList.add(newMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private void updateLastMessageId(int realId) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            MessageModel msg = messageList.get(i);
            if (msg.getSenderId() == myId && msg.getId() <= 0) {
                msg.setId(realId);
                messageAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void showEmojiMenu() {
        PopupMenu popup = new PopupMenu(this, btnEmote);
        String[] emojiList = {"😀", "😂", "😍", "👍", "❤️", "😮", "🎉"};
        for (String emoji : emojiList) popup.getMenu().add(emoji);
        popup.setOnMenuItemClickListener(item -> {
            edMessage.append(item.getTitle());
            return true;
        });
        popup.show();
    }

    private void playEmojiAnimation(String emoji) {
        if (emojiAnimationContainer == null) return;
        final TextView textView = new TextView(this);
        textView.setText(emoji);
        textView.setTextSize(34);
        Random random = new Random();
        float startX = emojiAnimationContainer.getWidth() / 2f + (random.nextFloat() * 300 - 150);
        float startY = emojiAnimationContainer.getHeight() - 150;
        textView.setX(startX);
        textView.setY(startY);
        textView.setAlpha(0f);
        emojiAnimationContainer.addView(textView);
        textView.animate()
                .translationY(-emojiAnimationContainer.getHeight() * 0.8f)
                .translationXBy(random.nextFloat() * 400 - 200)
                .alpha(1f).scaleX(2.0f).scaleY(2.0f).setDuration(2500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> emojiAnimationContainer.removeView(textView)).start();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Chat Messages", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void showStylishNotification(String title, String message) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(new Random().nextInt(), builder.build());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, myId, (messageId, emoji) -> {
            if (messageId > 0 && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                hubConnection.send("SendReaction", receiverId, messageId, emoji);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadChatHistory() {
        apiService.getChatHistory(myId, receiverId).enqueue(new Callback<List<MessageModel>>() {
            @Override
            public void onResponse(Call<List<MessageModel>> call, Response<List<MessageModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.addAll(response.body());
                    messageAdapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }
            @Override
            public void onFailure(Call<List<MessageModel>> call, Throwable t) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hubConnection != null) hubConnection.stop();
    }
}