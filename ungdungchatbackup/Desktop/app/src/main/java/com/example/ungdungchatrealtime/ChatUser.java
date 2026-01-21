package com.example.ungdungchatrealtime;

import com.google.gson.annotations.SerializedName;

public class ChatUser {
    public int id;

    @SerializedName("fullName")
    public String fullName;

    public String lastMessage;
    public String avatar;

    // --- THÊM MỚI ĐỂ XỬ LÝ THÔNG BÁO ---
    public boolean hasNewMessage = false; // Đánh dấu có tin nhắn mới hay không
    public int unreadCount = 0;           // Số lượng tin chưa đọc (tùy chọn)

    public ChatUser() {
    }

    public ChatUser(int id, String fullName, String lastMessage, String avatar) {
        this.id = id;
        this.fullName = fullName;
        this.lastMessage = lastMessage;
        this.avatar = avatar;
        this.hasNewMessage = false;
    }

    public int getId() { return id; }
    public String getUsername() { return fullName; }
}