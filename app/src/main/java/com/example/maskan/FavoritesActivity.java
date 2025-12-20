package com.example.maskan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private ImageButton btnBack;
    private LinearLayout layoutEmpty;
    private TextView tvFavoritesCount;

    private DatabaseHelper databaseHelper;
    private PropertyAdapter propertyAdapter;
    private List<Property> favoriteProperties = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);

        setupRecyclerView();
        setupClickListeners();
        loadFavorites();
        setupBottomNavigation();

        // ✅ اختبار النظام
        testFavoritesSystem();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initializeViews() {
        rvFavorites = findViewById(R.id.rvFavorites);
        btnBack = findViewById(R.id.btnBack);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount);

        // ✅ تسجيل تأكد من العناصر
        Log.d("Favorites", "تهيئة العناصر:");
        Log.d("Favorites", "rvFavorites: " + (rvFavorites != null ? "موجود" : "NULL"));
        Log.d("Favorites", "btnBack: " + (btnBack != null ? "موجود" : "NULL"));
        Log.d("Favorites", "layoutEmpty: " + (layoutEmpty != null ? "موجود" : "NULL"));
        Log.d("Favorites", "tvFavoritesCount: " + (tvFavoritesCount != null ? "موجود" : "NULL"));
    }

    private void setupRecyclerView() {
        // ✅ إنشاء adapter مع قائمة العقارات المفضلة
        propertyAdapter = new PropertyAdapter(favoriteProperties);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(propertyAdapter);

        // ✅ مستمع النقر على العقار للانتقال إلى التفاصيل
        propertyAdapter.setOnItemClickListener(new PropertyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Property property) {
                openPropertyDetails(property);
            }
        });

        // ✅ إضافة مستمع للأزرار (التقييم، المشاركة، الاتصال، الحذف)
        propertyAdapter.setOnButtonClickListener(new PropertyAdapter.OnButtonClickListener() {
            @Override
            public void onRateClick(Property property) {
                showRatingDialog(property);
            }

            @Override
            public void onShareClick(Property property) {
                shareProperty(property);
            }

            @Override
            public void onContactClick(Property property) {
                contactPropertyOwner(property);
            }

            @Override
            public void onDeleteClick(Property property) {
                // في صفحة المفضلات، الزر يحذف من المفضلات فقط
                removeFromFavoritesDialog(property);
            }
        });

        Log.d("Favorites", "تم إعداد RecyclerView");
    }

    private void setupClickListeners() {
        // زر العودة
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            Log.d("Favorites", "تم تعيين مستمع زر العودة");
        } else {
            Log.e("Favorites", "زر العودة NULL");
        }
    }

    private void loadFavorites() {
        Log.d("Favorites", "=== جلب العقارات المفضلة ===");

        favoriteProperties.clear();
        List<Property> favorites = databaseHelper.getFavoriteProperties();

        if (favorites != null && !favorites.isEmpty()) {
            favoriteProperties.addAll(favorites);
            Log.d("Favorites", "تم جلب " + favorites.size() + " عقار من المفضلات");

            // ✅ تسجيل كل عقار مفضل للمراقبة
            for (int i = 0; i < favorites.size(); i++) {
                Property property = favorites.get(i);
                Log.d("Favorites", "العقار المفضل " + (i+1) + ": ID=" + property.getId() +
                        ", العنوان=" + property.getTitle() +
                        ", الهاتف=" + property.getContactPhone());
            }
        } else {
            Log.w("Favorites", "لا توجد عقارات مفضلة");
        }

        updateUI();
    }

    private void updateUI() {
        // ✅ تحديث Adapter
        propertyAdapter.updateList(favoriteProperties);

        // ✅ تحديث العداد
        if (tvFavoritesCount != null) {
            tvFavoritesCount.setText(favoriteProperties.size() + " عقار مفضل");
        }

        // ✅ إظهار/إخفاء رسالة عدم وجود مفضلات
        if (favoriteProperties.isEmpty()) {
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.VISIBLE);
            }
            if (rvFavorites != null) {
                rvFavorites.setVisibility(View.GONE);
            }
            Log.d("Favorites", "لا توجد عقارات مفضلة - إظهار رسالة فارغة");
        } else {
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.GONE);
            }
            if (rvFavorites != null) {
                rvFavorites.setVisibility(View.VISIBLE);
            }
            Log.d("Favorites", "عرض " + favoriteProperties.size() + " عقار مفضل");
        }
    }

    // ✅ دالة فتح تفاصيل العقار
    private void openPropertyDetails(Property property) {
        Log.d("Favorites", "فتح تفاصيل العقار: ID=" + property.getId());

        Intent intent = new Intent(this, activity_property_details.class);

        // ✅ تمرير جميع بيانات العقار
        intent.putExtra("property_id", property.getId());
        intent.putExtra("property_title", property.getTitle());
        intent.putExtra("property_location", property.getLocation());
        intent.putExtra("property_price", property.getPrice());
        intent.putExtra("property_bedrooms", property.getBedrooms());
        intent.putExtra("property_bathrooms", property.getBathrooms());
        intent.putExtra("property_type", property.getType());

        // ✅ تمرير بيانات الاتصال إذا كانت متوفرة
        if (property.getContactName() != null) {
            intent.putExtra("property_contact_name", property.getContactName());
        }
        if (property.getContactPhone() != null) {
            intent.putExtra("property_contact_phone", property.getContactPhone());
        }

        startActivity(intent);
    }

    // ✅ دالة عرض dialog التقييم
    private void showRatingDialog(Property property) {
        if (property == null) {
            Toast.makeText(this, "بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تقييم العقار: " + property.getTitle());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        //androidx.appcompat.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);

        builder.setPositiveButton("تقييم", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (rating > 0) {
                boolean success = databaseHelper.addPropertyRating(property.getId(), rating, comment);
                if (success) {
                    Toast.makeText(this, "شكراً لتقييمك!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "خطأ في حفظ التقييم", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "يرجى اختيار تقييم", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    // ✅ دالة مشاركة العقار
    private void shareProperty(Property property) {
        if (property == null) {
            Toast.makeText(this, "بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "🏠 " + property.getTitle() + "\n\n" +
                "📍 " + property.getLocation() + "\n" +
                "💰 السعر: " + property.getPrice() + " ر.س\n" +
                "🛏️ الغرف: " + property.getBedrooms() + "\n" +
                "🚿 الحمامات: " + property.getBathrooms() + "\n\n" +
                "📱 عبر تطبيق مسكن";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "عقار: " + property.getTitle());

        startActivity(Intent.createChooser(shareIntent, "مشاركة العقار"));

        // ✅ تسجيل المشاركة
        databaseHelper.logPropertyShare(property.getId());
    }

    // ✅ دالة الاتصال بمالك العقار
    private void contactPropertyOwner(Property property) {
        if (property == null) {
            Toast.makeText(this, "بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ الحصول على بيانات العقار الكاملة من قاعدة البيانات
        Property fullProperty = databaseHelper.getPropertyById(property.getId());

        if (fullProperty == null) {
            Toast.makeText(this, "بيانات العقار غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = fullProperty.getContactPhone();

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(this, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ تنظيف الرقم
        phoneNumber = phoneNumber.trim().replaceAll("\\s+", "").replaceAll("[^0-9+]", "");

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "رقم الهاتف غير صالح", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ إضافة رمز الدولة
        if (!phoneNumber.startsWith("+") && !phoneNumber.startsWith("00")) {
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+966" + phoneNumber.substring(1);
            } else {
                phoneNumber = "+966" + phoneNumber;
            }
        }

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phoneNumber));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                databaseHelper.logPropertyContact(property.getId());
                Toast.makeText(this, "جاري الاتصال بـ: " + phoneNumber, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "لا يوجد تطبيق للاتصال", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في الاتصال: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Favorites", "خطأ في الاتصال: " + e.getMessage());
        }
    }

    // ✅ دالة إزالة العقار من المفضلات
    private void removeFromFavoritesDialog(Property property) {
        if (property == null) return;

        new AlertDialog.Builder(this)
                .setTitle("إزالة من المفضلة")
                .setMessage("هل تريد إزالة العقار '" + property.getTitle() + "' من المفضلات؟")
                .setPositiveButton("إزالة", (dialog, which) -> {
                    boolean removed = databaseHelper.removeFromFavorites(property.getId());
                    if (removed) {
                        Toast.makeText(this, "تمت الإزالة من المفضلة ❤️", Toast.LENGTH_SHORT).show();
                        loadFavorites(); // إعادة تحميل القائمة
                    } else {
                        Toast.makeText(this, "خطأ في الإزالة", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ تحديث البيانات عند العودة للنشاط
        loadFavorites();
        Log.d("Favorites", "عودة إلى صفحة المفضلات - تحديث البيانات");
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.nav_favorites) {
                        // نحن بالفعل في الصفحة المفضلات
                        return true;
                    } else if (id == R.id.nav_search) {
                        openSearchActivity();
                        return true;
                    } else if (id == R.id.nav_add) {
                        openAddProperty();
                        return true;
                    } else if (id == R.id.nav_home) {
                        openMainActivity();
                        return true;
                    }
                    return false;
                }
            });
            Log.d("Favorites", "تم إعداد شريط التنقل السفلي");
        } else {
            Log.e("Favorites", "شريط التنقل السفلي NULL");
        }
    }

    private void openMainActivity() {
        try {
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة الرئيسية: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Favorites", "Error opening MainActivity: " + e.getMessage());
        }
    }

    private void openAddProperty() {
        try {
            Intent intent = new Intent(FavoritesActivity.this, add_property.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة إضافة عقار: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Favorites", "Error opening AddProperty: " + e.getMessage());
        }
    }

    private void openSearchActivity() {
        try {
            Intent intent = new Intent(FavoritesActivity.this, SearchActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة البحث: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Favorites", "Error opening SearchActivity: " + e.getMessage());
        }
    }

    // ✅ دالة اختبار النظام
    private void testFavoritesSystem() {
        new Thread(() -> {
            try {
                // انتظر قليلاً لتهدئة قاعدة البيانات
                Thread.sleep(500);

                runOnUiThread(() -> {
                    Log.d("FavoritesTest", "=== اختبار نظام المفضلات ===");

                    // اختبار جلب المفضلات
                    List<Property> favorites = databaseHelper.getFavoriteProperties();
                    Log.d("FavoritesTest", "عدد العقارات المفضلة: " + (favorites != null ? favorites.size() : 0));

                    if (favorites != null && !favorites.isEmpty()) {
                        for (Property prop : favorites) {
                            Log.d("FavoritesTest", "   - " + prop.getId() + ": " + prop.getTitle());
                        }
                    } else {
                        Log.d("FavoritesTest", "   - لا توجد عقارات مفضلة");
                    }

                    // تحديث الواجهة
                    updateUI();
                });
            } catch (Exception e) {
                Log.e("FavoritesTest", "خطأ في اختبار النظام: " + e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        Log.d("Favorites", "تم تدمير النشاط");
    }
}