package com.example.ungdungchatrealtime;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MessageModel> messages;
    private int myId;
    private OnReactionListener reactionListener;

    private final String BASE_URL = "https://hilario-unelongated-horacio.ngrok-free.dev/";

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public interface OnReactionListener {
        void onReactionSelected(int messageId, String emoji);
    }

    public MessageAdapter(List<MessageModel> messages, int myId, OnReactionListener listener) {
        this.messages = messages;
        this.myId = myId;
        this.reactionListener = listener;
    }

    // Cập nhật reaction khi nhận từ server
    public void updateReaction(int messageId, String emoji) {
        if (messages == null) return;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId() == messageId) {
                messages.get(i).setReaction(emoji);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId() == myId ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        if (viewType == TYPE_SENT) {
            return new SentHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_message_sent, p, false));
        } else {
            return new ReceivedHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_message_received, p, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        MessageModel msg = messages.get(pos);
        Context ctx = h.itemView.getContext();

        if (h instanceof SentHolder) {
            SentHolder s = (SentHolder) h;

            configureMessage(ctx, s.tv, s.img, s.layoutFile, s.fileName, s.shimmer, s.cardImage, msg.getContent());
            handleReactionUI(s.cardReaction, s.tvReaction, msg);

            // -------- LONG CLICK FOR REACTION --------
            s.itemView.setOnLongClickListener(v -> {
                showReactionPopup(v, msg);
                return true;
            });

        } else {
            ReceivedHolder r = (ReceivedHolder) h;

            configureMessage(ctx, r.tv, r.img, r.layoutFile, r.fileName, r.shimmer, r.cardImage, msg.getContent());
            handleReactionUI(r.cardReaction, r.tvReaction, msg);

            // -------- LONG CLICK FOR REACTION --------
            r.itemView.setOnLongClickListener(v -> {
                showReactionPopup(v, msg);
                return true;
            });
        }
    }

    // ======================================================================
    //  HIỂN THỊ POPUP REACTION
    // ======================================================================
    private void showReactionPopup(View anchorView, MessageModel msg) {

        View popupView = LayoutInflater.from(anchorView.getContext())
                .inflate(R.layout.layout_reaction_menu, null);

        PopupWindow popup = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popup.setElevation(20f);
        popup.setOutsideTouchable(true);

        // căn popup ngay trên bong bóng
        popup.showAsDropDown(anchorView, -anchorView.getWidth() / 4, -anchorView.getHeight() - 40);

        // click biểu tượng
        View.OnClickListener click = v -> {
            String emoji = ((TextView) v).getText().toString();

            msg.setReaction(emoji);
            notifyItemChanged(messages.indexOf(msg));

            if (reactionListener != null) {
                reactionListener.onReactionSelected(msg.getId(), emoji);
            }

            popup.dismiss();
        };

        popupView.findViewById(R.id.re_like).setOnClickListener(click);
        popupView.findViewById(R.id.re_love).setOnClickListener(click);
        popupView.findViewById(R.id.re_haha).setOnClickListener(click);
        popupView.findViewById(R.id.re_wow).setOnClickListener(click);
        popupView.findViewById(R.id.re_sad).setOnClickListener(click);
        popupView.findViewById(R.id.re_fire).setOnClickListener(click);
    }

    // ======================================================================
    //  XỬ LÝ NỘI DUNG TIN NHẮN (text, ảnh, file)
    // ======================================================================
    private void configureMessage(
            Context ctx,
            TextView tv,
            ImageView img,
            View layoutFile,
            TextView tvFileName,
            ShimmerFrameLayout shimmer,
            View cardImage,
            String content
    ) {
        tv.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        layoutFile.setVisibility(View.GONE);
        cardImage.setVisibility(View.GONE);
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);

        if (content == null || content.trim().isEmpty()) return;

        if (!content.toLowerCase().contains("uploads/")) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(content);
            return;
        }

        String path = content.replace("\\", "/");
        if (path.startsWith("/")) path = path.substring(1);

        String fullUrl = BASE_URL + path;
        String filename = path.substring(path.lastIndexOf("/") + 1);

        if (isImageFile(path)) {

            cardImage.setVisibility(View.VISIBLE);
            shimmer.setVisibility(View.VISIBLE);
            shimmer.startShimmer();

            Picasso.get()
                    .load(fullUrl)
                    .fit()
                    .centerCrop()
                    .into(img, new Callback() {
                        @Override
                        public void onSuccess() {
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                            img.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                            tv.setVisibility(View.VISIBLE);
                            tv.setText("Lỗi không tải được ảnh");
                        }
                    });

            return;
        }

        layoutFile.setVisibility(View.VISIBLE);
        tvFileName.setText(filename);

        layoutFile.setOnClickListener(v -> {
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(fullUrl));
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) dm.enqueue(req);
        });
    }

    private boolean isImageFile(String p) {
        p = p.toLowerCase();
        return p.endsWith(".jpg") || p.endsWith(".jpeg") || p.endsWith(".png") || p.endsWith(".webp");
    }

    // ======================================================================
    //  HIỂN THỊ REACTION
    // ======================================================================
    private void handleReactionUI(View card, TextView tv, MessageModel msg) {
        if (msg.getReaction() != null && !msg.getReaction().isEmpty()) {
            card.setVisibility(View.VISIBLE);
            tv.setText(msg.getReaction());
        } else {
            card.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    // ======================================================================
    //  VIEW HOLDER SENT
    // ======================================================================
    static class SentHolder extends RecyclerView.ViewHolder {
        TextView tv, tvReaction, fileName;
        ImageView img;
        View layoutFile, cardReaction, cardImage;
        ShimmerFrameLayout shimmer;

        SentHolder(View v) {
            super(v);
            tv = v.findViewById(R.id.tvMessageSent);
            img = v.findViewById(R.id.imgMessageSent);
            layoutFile = v.findViewById(R.id.layoutFileSent);
            fileName = v.findViewById(R.id.tvFileNameSent);
            cardReaction = v.findViewById(R.id.cardReactionSent);
            tvReaction = v.findViewById(R.id.tvReactionSent);
            cardImage = v.findViewById(R.id.cardImageSent);
            shimmer = v.findViewById(R.id.shimmerLayoutImageSent);
        }
    }

    // ======================================================================
    //  VIEW HOLDER RECEIVED
    // ======================================================================
    static class ReceivedHolder extends RecyclerView.ViewHolder {
        TextView tv, tvReaction, fileName;
        ImageView img;
        View layoutFile, cardReaction, cardImage;
        ShimmerFrameLayout shimmer;

        ReceivedHolder(View v) {
            super(v);
            tv = v.findViewById(R.id.tvMessageReceived);
            img = v.findViewById(R.id.imgMessageReceived);
            layoutFile = v.findViewById(R.id.layoutFileReceived);
            fileName = v.findViewById(R.id.tvFileNameReceived);
            cardReaction = v.findViewById(R.id.cardReactionReceived);
            tvReaction = v.findViewById(R.id.tvReactionReceived);
            cardImage = v.findViewById(R.id.cardImageReceived);
            shimmer = v.findViewById(R.id.shimmerLayoutImageReceived);
        }
    }
}
