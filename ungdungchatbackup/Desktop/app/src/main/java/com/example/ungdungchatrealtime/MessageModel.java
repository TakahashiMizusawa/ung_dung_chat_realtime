package com.example.ungdungchatrealtime;

import com.google.gson.annotations.SerializedName;

public class MessageModel {
    @SerializedName(value = "id", alternate = {"Id"})
    private int id;

    @SerializedName(value = "senderId", alternate = {"SenderId"})
    private int senderId;

    @SerializedName(value = "receiverId", alternate = {"ReceiverId"}) // Thêm trường này
    private int receiverId;

    @SerializedName(value = "content", alternate = {"Content"})
    private String content;

    @SerializedName(value = "timestamp", alternate = {"Timestamp"})
    private String timestamp;

    @SerializedName(value = "reaction", alternate = {"Reaction"})
    private String reaction;

    public MessageModel() {
    }

    public MessageModel(int senderId, String content, String timestamp) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.reaction = "";
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getReaction() { return reaction == null ? "" : reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }
}