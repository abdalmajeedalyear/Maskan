package com.example.maskan;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
;
import android.widget.Toast;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private Context context;
    private List<Property> properties;
    private OnItemClickListener onItemClickListener;
    private OnButtonClickListener onButtonClickListener;


    public interface OnItemClickListener {
        void onItemClick(Property property);
    }

    public interface OnButtonClickListener {
        void onRateClick(Property property);
        void onShareClick(Property property);
        void onContactClick(Property property);
        void onDeleteClick(Property property);
    }

    public PropertyAdapter(List<Property> properties) {
        this.properties = properties != null ? properties : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.onButtonClickListener = listener;
    }

    public void updateList(List<Property> newProperties) {
        this.properties = newProperties != null ? newProperties : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = properties.get(position);

        // تشخيص
        Log.d("AREA_DEBUG", "العقار: " + property.getTitle() +
                ", المساحة في البيانات: " + property.getArea() +
                ", نوع المساحة: " + (property.getArea() != null ? property.getArea().getClass().getSimpleName() : "null"));


        // 🔍 هذا هو السطر الجديد - أضفه هنا
        Log.d("DATA_CHECK", "عقار " + property.getId() +
                ": offerType=" + property.getOfferType() +
                ", type=" + property.getType());

        // 🔧 تحديث الشارة بناءً على offer_type
        TextView tvTypeBadge = holder.itemView.findViewById(R.id.tvTypeBadge);
        if (tvTypeBadge != null && property.getOfferType() != null) {
            String offerType = property.getOfferType();

            if (offerType.contains("إيجار") || offerType.contains("ايجار")) {
                tvTypeBadge.setText("للايجار");
                tvTypeBadge.setBackgroundColor(Color.parseColor("#2196F3")); // أزرق فاتح لطيف

                tvTypeBadge.setTextColor(Color.WHITE);
            } else if (offerType.contains("بيع")) {
                tvTypeBadge.setText("للبيع");
                tvTypeBadge.setBackgroundColor(Color.parseColor("#FF9800")); // برتقالي
                tvTypeBadge.setTextColor(Color.WHITE);
            } else {
                // إذا كان غير معروف
                tvTypeBadge.setVisibility(View.GONE);
            }
        }


        // 🔧 تحديث المساحة (Area)
        TextView tvArea = holder.itemView.findViewById(R.id.tvArea); // تأكد من الـ ID الصحيح
        if (tvArea != null) {
            String areaValue = property.getArea(); // تأكد أن Property.java له دالة getArea()
            if (areaValue != null && !areaValue.isEmpty()) {
                tvArea.setText(areaValue + " م²");
            } else {
                tvArea.setText("؟"); // أو تتركه فارغاً
            }
        }



        // ⭐ تحديث التقييم - الطريقة الصحيحة
        LinearLayout ratingLayout = holder.itemView.findViewById(R.id.ratingLayout);
        TextView tvRating = holder.itemView.findViewById(R.id.tvRating);

        if (ratingLayout != null && tvRating != null) {
            // ✅ تمرير holder إلى الدالة
            float averageRating = getAverageRatingFromDatabase(holder, property.getId());

            if (averageRating > 0) {
                ratingLayout.setVisibility(View.VISIBLE);
                String ratingText = String.format("%.1f", averageRating);
                tvRating.setText(ratingText);

                // (اختياري) تغيير لون النص حسب التقييم
                if (averageRating >= 4.5) {
                    tvRating.setTextColor(Color.parseColor("#FFD700")); // ذهبي
                } else if (averageRating >= 4.0) {
                    tvRating.setTextColor(Color.parseColor("#FF9800")); // برتقالي
                }
            } else {
                ratingLayout.setVisibility(View.GONE); // إخفاء إذا لا توجد تقييمات
            }
        }
        holder.bind(property, onButtonClickListener);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(property);
                }
                openPropertyDetails(holder.itemView.getContext(), property);
            }
        });
    }

    private void openPropertyDetails(Context context, Property property) {
        Intent intent = new Intent(context, activity_property_details.class);
        intent.putExtra("property_id", property.getId());
        intent.putExtra("property_title", property.getTitle());
        intent.putExtra("property_location", property.getLocation());
        intent.putExtra("property_price", property.getPrice());
        intent.putExtra("property_bedrooms", property.getBedrooms());
        intent.putExtra("property_bathrooms", property.getBathrooms());
        intent.putExtra("property_type", property.getType());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProperty;
        private TextView tvTitle, tvLocation, tvPrice, tvBedrooms, tvBathrooms;
        private com.google.android.material.button.MaterialButton btnRate, btnShare, btnContact, btnDelete, btnFavorite;
        private DatabaseHelper dbHelper;
        private Context context;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.context = itemView.getContext();

            // تهيئة العناصر الأساسية
            ivProperty = itemView.findViewById(R.id.ivProperty);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvBedrooms = itemView.findViewById(R.id.tvBedrooms);
            tvBathrooms = itemView.findViewById(R.id.tvBathrooms);

            // تهيئة الأزرار من نوع MaterialButton
            btnRate = itemView.findViewById(R.id.btnRate);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            // ✅ تهيئة DatabaseHelper بشكل آمن مع try-catch
            initializeDatabaseHelper();
        }

        private void initializeDatabaseHelper() {
            try {
                // ✅ استخدم Singleton pattern
                dbHelper = DatabaseHelper.getInstance(context);
                Log.d("PropertyAdapter", "✅ تم الحصول على DatabaseHelper بنجاح");
            } catch (Exception e) {
                Log.e("PropertyAdapter", "❌ خطأ في الحصول على DatabaseHelper: " + e.getMessage());
                dbHelper = null;
            }
        }

        public void bind(Property property, OnButtonClickListener buttonListener) {
            if (property == null) return;

            Log.d("PropertyAdapter", "=== ربط عقار ID: " + property.getId() + " ===");

            // تعبئة البيانات الأساسية
            tvTitle.setText(property.getTitle() != null ? property.getTitle() : "لا يوجد عنوان");
            tvLocation.setText(property.getLocation() != null ? property.getLocation() : "؟");

            String priceText = property.getPrice() != null ? property.getPrice() + " ر.س" : "؟";
            tvPrice.setText(priceText);

            String bedroomsText = "؟";
            if (property.getBedrooms() != null && !property.getBedrooms().equals("-") && !property.getBedrooms().equals("0")) {
                bedroomsText = property.getBedrooms() + " غرف";
            }

            String bathroomsText = "؟";
            if (property.getBathrooms() != null && !property.getBathrooms().equals("-") && !property.getBathrooms().equals("0")) {
                bathroomsText = property.getBathrooms() + " حمام";
            }

            tvBedrooms.setText(bedroomsText);
            tvBathrooms.setText(bathroomsText);

            // تحميل الصورة
            loadPropertyImage(property);

            // ✅ إعداد زر القلب (المفضلة) - النظام الهجين
            setupFavoriteButton(property);

            // إعداد مستمعين للأزرار الأخرى
            setupButtonListeners(property, buttonListener);

            Log.d("PropertyAdapter", "=== تم ربط العقار بنجاح ===");
        }

        // ✅ النظام الهجين: قاعدة بيانات + SharedPreferences كنسخة احتياطية
        private void setupFavoriteButton(Property property) {
            if (btnFavorite == null) {
                Log.e("PropertyAdapter", "❌ زر القلب غير موجود في التصميم");
                return;
            }

            try {
                Log.d("PropertyAdapter", "--- إعداد زر القلب للعقار ID: " + property.getId() + " ---");

                // التحقق من حالة المفضلة (مختلط)
                boolean isFavorite = checkFavoriteStatus(property.getId());
                Log.d("PropertyAdapter", "حالة المفضلة: " + isFavorite);

                // تحديث مظهر الزر
                updateFavoriteButtonAppearance(isFavorite);

                // إعداد مستمع النقر
                btnFavorite.setOnClickListener(v -> {
                    Log.d("PropertyAdapter", "تم النقر على زر القلب للعقار ID: " + property.getId());

                    try {
                        // الحصول على الحالة الحالية
                        boolean currentFavoriteState = checkFavoriteStatus(property.getId());
                        boolean newFavoriteState = !currentFavoriteState;
                        Log.d("PropertyAdapter", "تغيير الحالة من " + currentFavoriteState + " إلى " + newFavoriteState);

                        if (newFavoriteState) {
                            // محاولة الإضافة إلى قاعدة البيانات أولاً
                            boolean addedToDb = false;
                            if (dbHelper != null) {
                                addedToDb = dbHelper.addToFavorites(property.getId());
                                Log.d("PropertyAdapter", "نتيجة الإضافة إلى قاعدة البيانات: " + addedToDb);
                            }

                            // نسخ احتياطي في SharedPreferences
                            boolean addedToPrefs = saveToSharedPreferences(property.getId(), true);
                            Log.d("PropertyAdapter", "نتيجة الإضافة إلى SharedPreferences: " + addedToPrefs);

                            if (addedToDb || addedToPrefs) {
                                updateFavoriteButtonAppearance(true);
                                showToast("تمت الإضافة إلى المفضلة ❤️");
                                Log.d("PropertyAdapter", "✅ تمت الإضافة بنجاح");
                            } else {
                                showToast("فشل في الإضافة إلى المفضلة");
                                Log.e("PropertyAdapter", "❌ فشل في الإضافة");
                            }
                        } else {
                            // محاولة الإزالة من قاعدة البيانات أولاً
                            boolean removedFromDb = false;
                            if (dbHelper != null) {
                                removedFromDb = dbHelper.removeFromFavorites(property.getId());
                                Log.d("PropertyAdapter", "نتيجة الإزالة من قاعدة البيانات: " + removedFromDb);
                            }

                            // نسخ احتياطي في SharedPreferences
                            boolean removedFromPrefs = saveToSharedPreferences(property.getId(), false);
                            Log.d("PropertyAdapter", "نتيجة الإزالة من SharedPreferences: " + removedFromPrefs);

                            if (removedFromDb || removedFromPrefs) {
                                updateFavoriteButtonAppearance(false);
                                showToast("تمت الإزالة من المفضلة");
                                Log.d("PropertyAdapter", "✅ تمت الإزالة بنجاح");
                            } else {
                                showToast("فشل في الإزالة من المفضلة");
                                Log.e("PropertyAdapter", "❌ فشل في الإزالة");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("PropertyAdapter", "خطأ أثناء النقر: " + e.getMessage());
                        showToast("حدث خطأ: " + e.getMessage());
                    }
                });

                Log.d("PropertyAdapter", "--- تم إعداد زر القلب بنجاح ---");

            } catch (Exception e) {
                Log.e("PropertyAdapter", "خطأ في إعداد زر القلب: " + e.getMessage());
                btnFavorite.setVisibility(View.GONE);
            }
        }

        // ✅ دالة التحقق من حالة المفضلة (مختلطة)
        private boolean checkFavoriteStatus(int propertyId) {
            try {
                // أولاً: التحقق من قاعدة البيانات
                if (dbHelper != null) {
                    boolean fromDb = dbHelper.isPropertyInFavorites(propertyId);
                    Log.d("PropertyAdapter", "التحقق من قاعدة البيانات: " + fromDb);
                    return fromDb;
                }

                // ثانياً: التحقق من SharedPreferences
                boolean fromPrefs = isFavoriteInSharedPreferences(propertyId);
                Log.d("PropertyAdapter", "التحقق من SharedPreferences: " + fromPrefs);
                return fromPrefs;

            } catch (Exception e) {
                Log.e("PropertyAdapter", "خطأ في التحقق من حالة المفضلة: " + e.getMessage());
                return false;
            }
        }

        // ✅ دالة للحفظ في SharedPreferences
        private boolean saveToSharedPreferences(int propertyId, boolean isFavorite) {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences(
                        "favorites_prefs", Context.MODE_PRIVATE);
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("property_" + propertyId, isFavorite);
                boolean result = editor.commit();
                Log.d("PropertyAdapter", "حفظ في SharedPreferences - ID: " + propertyId +
                        ", حالة: " + isFavorite + ", نتيجة: " + result);
                return result;
            } catch (Exception e) {
                Log.e("PropertyAdapter", "خطأ في حفظ SharedPreferences: " + e.getMessage());
                return false;
            }
        }

        // ✅ دالة للقراءة من SharedPreferences
        private boolean isFavoriteInSharedPreferences(int propertyId) {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences(
                        "favorites_prefs", Context.MODE_PRIVATE);
                boolean result = prefs.getBoolean("property_" + propertyId, false);
                Log.d("PropertyAdapter", "قراءة من SharedPreferences - ID: " + propertyId +
                        ", نتيجة: " + result);
                return result;
            } catch (Exception e) {
                Log.e("PropertyAdapter", "خطأ في قراءة SharedPreferences: " + e.getMessage());
                return false;
            }
        }

        // ✅ دالة لتحديث مظهر زر القلب
        private void updateFavoriteButtonAppearance(boolean isFavorite) {
            if (btnFavorite == null) return;

            try {
                Log.d("PropertyAdapter", "تحديث مظهر زر القلب - حالة: " + isFavorite);

                if (isFavorite) {
                    // حالة: مفضل (قلب ممتلئ)
                    try {
                        btnFavorite.setIconResource(R.drawable.ic_heart_filled);
                    } catch (Exception e) {
                        btnFavorite.setIconResource(android.R.drawable.btn_star_big_on);
                    }
                    btnFavorite.setText("مفضل");

                    // تعيين الألوان للمفضلة (وردي/أحمر)
                    btnFavorite.setIconTint(android.content.res.ColorStateList.valueOf(0xFFE91E63));
                    btnFavorite.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFE91E63));
                    btnFavorite.setTextColor(0xFFE91E63);

                    // خلفية شفافة وردية
                    btnFavorite.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0x20E91E63)
                    );

                } else {
                    // حالة: غير مفضل (قلب فارغ)
                    try {
                        btnFavorite.setIconResource(R.drawable.ic_heart_outline);
                    } catch (Exception e) {
                        btnFavorite.setIconResource(android.R.drawable.btn_star_big_off);
                    }
                    btnFavorite.setText("مفضل");

                    // تعيين الألوان لغير المفضلة (رمادي)
                    btnFavorite.setIconTint(android.content.res.ColorStateList.valueOf(0xFF757575));
                    btnFavorite.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFF757575));
                    btnFavorite.setTextColor(0xFF757575);

                    // خلفية بيضاء
                    btnFavorite.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFFFFFFF)
                    );
                }

                Log.d("PropertyAdapter", "✅ تم تحديث مظهر زر القلب");

            } catch (Exception e) {
                Log.e("PropertyAdapter", "خطأ في تحديث مظهر زر القلب: " + e.getMessage());
            }
        }

        // دالة لعرض Toast
        private void showToast(String message) {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("PropertyAdapter", "خطأ في عرض Toast: " + e.getMessage());
            }
        }

        // دالة إعداد مستمعين الأزرار الأخرى
        private void setupButtonListeners(Property property, OnButtonClickListener buttonListener) {
            try {
                if (btnRate != null && buttonListener != null) {
                    btnRate.setOnClickListener(v -> buttonListener.onRateClick(property));
                }

                if (btnShare != null && buttonListener != null) {
                    btnShare.setOnClickListener(v -> buttonListener.onShareClick(property));
                }

                if (btnContact != null && buttonListener != null) {
                    btnContact.setOnClickListener(v -> buttonListener.onContactClick(property));
                }

                if (btnDelete != null && buttonListener != null) {
                    btnDelete.setOnClickListener(v -> buttonListener.onDeleteClick(property));

                    // إظهار زر الحذف فقط في شاشة "عقاراتي"
                    try {
                        boolean isMyProperties = context instanceof activity_my_properties;
                        btnDelete.setVisibility(isMyProperties ? View.VISIBLE : View.GONE);
                        Log.d("PropertyAdapter", "زر الحذف - الظهور: " + (isMyProperties ? "نعم" : "لا"));
                    } catch (Exception e) {
                        btnDelete.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                Log.e("PropertyAdapter", "خطأ في إعداد مستمعين الأزرار: " + e.getMessage());
            }
        }

        // دالة تحميل الصورة
        private void loadPropertyImage(Property property) {
            if (ivProperty == null) return;

            try {
                if (property.hasImages()) {
                    String firstImagePath = property.getFirstImagePath();
                    if (firstImagePath != null && !firstImagePath.isEmpty()) {
                        loadImageFromStorage(firstImagePath);
                        return;
                    }
                }

                // استخدام صورة افتراضية
                ivProperty.setImageResource(R.drawable.ic_placeholder);
            } catch (Exception e) {
                ivProperty.setImageResource(R.drawable.ic_placeholder);
            }
        }

        // دالة تحميل الصورة من التخزين
        private void loadImageFromStorage(String imagePath) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                        ivProperty.setImageBitmap(bitmap);
                    } else {
                        ivProperty.setImageResource(R.drawable.ic_placeholder);
                    }
                } else {
                    ivProperty.setImageResource(R.drawable.ic_placeholder);
                }
            } catch (Exception e) {
                ivProperty.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }


    private float getAverageRatingFromDatabase(PropertyViewHolder holder, int propertyId) {
        try {
            // ✅ الحل: استخدام context من الـ ViewHolder
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(holder.itemView.getContext());

            // اختيار 1: إذا كانت الدالة getAverageRating موجودة في DatabaseHelper
            // return dbHelper.getAverageRating(propertyId);

            // اختيار 2: استعلام مباشر (الأكثر أماناً)
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = null;
            float averageRating = 0.0f;

            try {
                String query = "SELECT AVG(rating) FROM property_ratings WHERE property_id = ?";
                cursor = db.rawQuery(query, new String[]{String.valueOf(propertyId)});

                if (cursor.moveToFirst()) {
                    averageRating = cursor.getFloat(0);
                }

                Log.d("RATING_FETCH", "العقار " + propertyId + " | التقييم: " + averageRating);

            } catch (Exception e) {
                Log.e("RATING_ERROR", "خطأ في الاستعلام: " + e.getMessage());
            } finally {
                if (cursor != null) cursor.close();
            }

            return averageRating;

        } catch (Exception e) {
            Log.e("DB_CONNECTION", "خطأ في الاتصال بقاعدة البيانات: " + e.getMessage());
            return 0.0f;
        }
    }

}