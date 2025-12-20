package com.example.maskan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<PropertyRating> commentsList;

    public CommentsAdapter(List<PropertyRating> commentsList) {
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        PropertyRating rating = commentsList.get(position);

        // 1. نص التعليق
        if (rating.getComment() != null && !rating.getComment().isEmpty()) {
            holder.tvCommentText.setText(rating.getComment());
        } else {
            holder.tvCommentText.setText("لا يوجد تعليق");
        }

        // 2. النجوم
        holder.ratingBarComment.setRating(rating.getRating());

        // 3. التاريخ - تحويل إلى وقت نسبي (مثل: "قبل ساعة")
        if (rating.getCreatedAt() != null) {
            String relativeTime = getRelativeTime(rating.getCreatedAt());
            holder.tvCommentDate.setText(relativeTime);
        } else {
            holder.tvCommentDate.setText("--");
        }

        // 4. اسم المستخدم (يمكنك جلب اسم المستخدم الفعلي من قاعدة البيانات)
        // حالياً نستخدم اسم افتراضي
        holder.tvUserName.setText("مستخدم");

        // 5. صورة المستخدم (تبقى الافتراضية)
    }

    @Override
    public int getItemCount() {
        return commentsList != null ? commentsList.size() : 0;
    }

    // دالة لتحويل التاريخ إلى وقت نسبي
    private String getRelativeTime(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date past = format.parse(dateString);
            Date now = new Date();

            long diff = now.getTime() - past.getTime();

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes < 60) {
                return "قبل " + minutes + " دقيقة";
            }

            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            if (hours < 24) {
                return "قبل " + hours + " ساعة";
            }

            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days < 30) {
                return "قبل " + days + " يوم";
            }

            long months = days / 30;
            if (months < 12) {
                return "قبل " + months + " شهر";
            }

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(past);

        } catch (Exception e) {
            return dateString; // إذا فشل التحويل، نعيد النص الأصلي
        }
    }

    // ViewHolder يتطابق مع تصميمك
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvCommentDate, tvCommentText;
        RatingBar ratingBarComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar); // إذا غيرت ID في XML
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            ratingBarComment = itemView.findViewById(R.id.ratingBarComment);
        }
    }
}