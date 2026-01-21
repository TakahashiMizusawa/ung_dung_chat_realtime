package com.example.ungdungchatrealtime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    private List<ChatUser> requests;
    private Context context;
    private final String BASE_URL = "https://hilario-unelongated-horacio.ngrok-free.dev/";

    public FriendRequestAdapter(List<ChatUser> requests, Context context) {
        this.requests = requests;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatUser user = requests.get(position);
        holder.tvName.setText(user.fullName);

        if (user.avatar != null && !user.avatar.isEmpty()) {
            Picasso.get()
                    .load(BASE_URL + user.avatar)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }

        // Sử dụng holder.getAdapterPosition() để lấy vị trí chính xác tại thời điểm click
        holder.btnAccept.setOnClickListener(v -> handleRequest(user.id, 1, holder));
        holder.btnDecline.setOnClickListener(v -> handleRequest(user.id, 2, holder));
    }

    private void handleRequest(int senderId, int status, ViewHolder holder) {
        int myId = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getInt("userId", -1);
        int currentPos = holder.getAdapterPosition();

        if (currentPos == RecyclerView.NO_POSITION) return;

        RetrofitClient.getApiService().respondToRequest(senderId, myId, status).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Xóa item khỏi danh sách hiển thị
                    requests.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, requests.size());

                    String msg = (status == 1) ? "Đã trở thành bạn bè" : "Đã từ chối";
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Xử lý thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName;
        Button btnAccept, btnDecline;

        public ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgReqAvatar);
            tvName = itemView.findViewById(R.id.tvReqName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}