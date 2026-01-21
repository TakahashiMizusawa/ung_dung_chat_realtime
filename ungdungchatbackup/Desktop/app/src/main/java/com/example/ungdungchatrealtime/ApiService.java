package com.example.ungdungchatrealtime;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // --- AUTH CONTROLLER ---
    @Headers("Content-Type: application/json; charset=UTF-8")
    @POST("api/Auth/register")
    Call<ResponseBody> register(@Body User user);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @POST("api/Auth/login")
    Call<User> login(@Body LoginRequest loginRequest);

    @GET("api/Auth/get-security-question/{username}")
    Call<SecurityQuestionResponse> getSecurityQuestion(@Path("username") String username);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @POST("api/Auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest request);

    @Multipart
    @PUT("api/Auth/update-avatar/{id}")
    Call<ResponseBody> updateAvatar(@Path("id") int userId, @Part MultipartBody.Part file);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @PUT("api/Auth/update-profile/{id}")
    Call<ResponseBody> updateProfile(@Path("id") int userId, @Body User user);

    @DELETE("api/Auth/delete-account/{id}")
    Call<ResponseBody> deleteAccount(@Path("id") int userId, @Query("password") String password);


    // --- FRIEND CONTROLLER ---
    @GET("api/Friend/search")
    Call<List<ChatUser>> searchUser(@Query("username") String username);

    @GET("api/Friend/requests/{userId}")
    Call<List<ChatUser>> getPendingRequests(@Path("userId") int userId);

    @POST("api/Friend/sendRequest")
    Call<ResponseBody> sendFriendRequest(@Query("senderId") int senderId, @Query("receiverId") int receiverId);

    @POST("api/Friend/respondRequest")
    Call<ResponseBody> respondToRequest(@Query("senderId") int senderId, @Query("receiverId") int receiverId, @Query("status") int status);

    @GET("api/Friend/list/{userId}")
    Call<List<ChatUser>> getFriendList(@Path("userId") int userId);

    @DELETE("api/Friend/deleteFriend")
    Call<ResponseBody> deleteFriend(@Query("userId") int userId, @Query("friendId") int friendId);


    // --- MESSAGES CONTROLLER ---
    @Headers("Accept: application/json; charset=UTF-8")
    @GET("api/Messages/{userId}/{friendId}")
    Call<List<MessageModel>> getChatHistory(@Path("userId") int userId, @Path("friendId") int friendId);

    // MỚI: API upload ảnh và tài liệu trong hội thoại
    @Multipart
    @POST("api/Messages/upload-attachment")
    Call<UploadResponse> uploadAttachment(@Part MultipartBody.Part file);
}