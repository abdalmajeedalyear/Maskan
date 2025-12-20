package com.example.maskan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PropertyRatingAdapter extends RecyclerView.Adapter<PropertyRatingAdapter.CommentViewHolder> {

    private List<PropertyRating> commentList;

    public PropertyRatingAdapter(List<PropertyRating> commentList) {
        this.commentList = commentList;
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
        PropertyRating rating = commentList.get(position);
        holder.bind(rating);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateList(List<PropertyRating> newList) {
        commentList.clear();
        commentList.addAll(newList);
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvCommentDate, tvCommentText;
        private RatingBar ratingBarComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            ratingBarComment = itemView.findViewById(R.id.ratingBarComment);
        }

        public void bind(PropertyRating rating) {
            // تعيين البيانات
            ratingBarComment.setRating(rating.getRating());
            tvCommentText.setText(rating.getComment());

            // اسم مستخدم عشوائي (يمكن استبداله بمستخدم حقيقي لاحقاً)
            String[] userNames = {"أحمد", "محمد", "سارة", "فاطمة", "خالد"};
            String userName = userNames[rating.getId() % userNames.length];
            tvUserName.setText("مستخدم " + userName);

            // تنسيق التاريخ
            tvCommentDate.setText(formatDate(rating.getCreatedAt()));
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateString);

                // حساب الوقت المنقضي
                long diff = System.currentTimeMillis() - date.getTime();
                long minutes = diff / (60 * 1000);
                long hours = minutes / 60;
                long days = hours / 24;

                if (minutes < 60) {
                    return "قبل " + minutes + " دقيقة";
                } else if (hours < 24) {
                    return "قبل " + hours + " ساعة";
                } else if (days < 7) {
                    return "قبل " + days + " يوم";
                } else {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (Exception e) {
                return dateString;
            }
        }
    }
}