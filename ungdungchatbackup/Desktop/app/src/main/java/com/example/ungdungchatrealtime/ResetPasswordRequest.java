package com.example.ungdungchatrealtime;

public class ResetPasswordRequest {
    private String username;
    private String answer;
    private String newPassword;

    public ResetPasswordRequest(String username, String answer, String newPassword) {
        this.username = username;
        this.answer = answer;
        this.newPassword = newPassword;
    }
}