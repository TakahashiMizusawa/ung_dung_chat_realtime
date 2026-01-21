package com.example.ungdungchatrealtime;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatUser> chatUsers;
    private Context context;
    private final String BASE_URL = "https://hilario-unelongated-horacio.ngrok-free.dev/";

    // Khai báo Interface xử lý nhấn giữ
    private OnUserLongClickListener longClickListener;

    public interface OnUserLongClickListener {
        void onUserLongClick(ChatUser user);
    }

    public ChatAdapter(List<ChatUser> chatUsers, Context context, OnUserLongClickListener longClickListener) {
        this.chatUsers = chatUsers;
        this.context = context;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatUser user = chatUsers.get(position);
        holder.tvName.setText(user.fullName);
        holder.tvLastMsg.setText(user.lastMessage);

        // Load ảnh đại diện
        if (user.avatar != null && !user.avatar.isEmpty()) {
            Picasso.get().load(BASE_URL + user.avatar)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }

        // --- LOGIC HIỂN THỊ THÔNG BÁO MỚI ---
        if (user.hasNewMessage) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"));
            holder.tvName.setTypeface(null, Typeface.BOLD);
            holder.tvLastMsg.setTypeface(null, Typeface.BOLD);
            holder.tvLastMsg.setTextColor(Color.BLACK);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvName.setTypeface(null, Typeface.NORMAL);
            holder.tvLastMsg.setTypeface(null, Typeface.NORMAL);
            holder.tvLastMsg.setTextColor(Color.GRAY);
        }

        // SỰ KIỆN CLICK (Mở Chat)
        holder.itemView.setOnClickListener(v -> {
            if (user.id > 0) {
                user.hasNewMessage = false;
                notifyItemChanged(position);

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("receiverId", user.id);
                intent.putExtra("receiverName", user.fullName);
                intent.putExtra("receiverAvatar", user.avatar);
                context.startActivity(intent);
            }
        });

        // SỰ KIỆN LONG CLICK (Xóa bạn bè)
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onUserLongClick(user);
                return true; // Trả về true để tiêu thụ sự kiện, không trigger click thường
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return chatUsers != null ? chatUsers.size() : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName, tvLastMsg;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatarChat);
            tvName = itemView.findViewById(R.id.tvChatName);
            tvLastMsg = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}