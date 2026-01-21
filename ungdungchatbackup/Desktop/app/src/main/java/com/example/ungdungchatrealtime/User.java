package com.example.ungdungchatrealtime;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName(value = "id", alternate = {"Id"})
    public int id;

    @SerializedName(value = "username", alternate = {"Username"})
    public String username;

    @SerializedName(value = "password", alternate = {"Password"})
    public String password;

    @SerializedName(value = "oldPassword", alternate = {"OldPassword"})
    public String oldPassword;

    @SerializedName(value = "fullName", alternate = {"FullName"})
    public String fullName;

    @SerializedName(value = "avatar", alternate = {"Avatar"})
    public String avatar;

    // --- THÊM CÁC TRƯỜNG MỚI DƯỚI ĐÂY ---

    @SerializedName(value = "securityQuestion", alternate = {"SecurityQuestion"})
    public String securityQuestion;

    @SerializedName(value = "securityAnswer", alternate = {"SecurityAnswer"})
    public String securityAnswer;

    @SerializedName(value = "pinCode", alternate = {"PinCode"})
    public String pinCode;

    // ------------------------------------

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}