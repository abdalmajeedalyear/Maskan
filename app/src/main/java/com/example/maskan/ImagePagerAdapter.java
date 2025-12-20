package com.example.maskan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private activity_property_details activity;
    private List<String> imagePaths;

    public ImagePagerAdapter(activity_property_details activity, List<String> imagePaths) {
        this.activity = activity;
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_property_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        holder.bind(imagePath);
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPropertyImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPropertyImage = itemView.findViewById(R.id.ivPropertyImage);
        }

        public void bind(String imagePath) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                        ivPropertyImage.setImageBitmap(bitmap);
                    } else {
                        ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
                    }
                } else {
                    ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
                }
            } catch (Exception e) {
                ivPropertyImage.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
}