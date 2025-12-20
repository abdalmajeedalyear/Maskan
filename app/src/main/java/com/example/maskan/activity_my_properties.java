package com.example.maskan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class activity_my_properties extends AppCompatActivity {

    private RecyclerView rvMyProperties;
    private EditText etSearch;
    private ImageButton btnBack, btnFilter;
    private TextView tvPropertiesCount;
    private LinearLayout layoutEmpty;
    private Button btnAddFirstProperty;

    private DatabaseHelper databaseHelper;
    private PropertyAdapter propertyAdapter;
    private List<Property> allProperties = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_properties);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }



        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);

        setupRecyclerView();
        setupClickListeners();
        loadProperties();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // إعداد شريط التنقل السفلي
        setupBottomNavigation();
    }

    private void initializeViews() {
        rvMyProperties = findViewById(R.id.rvMyProperties);
        etSearch = findViewById(R.id.etSearch);
        //btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter);
        tvPropertiesCount = findViewById(R.id.tvPropertiesCount);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnAddFirstProperty = findViewById(R.id.btnAddFirstProperty);
    }

    private void setupRecyclerView() {
            propertyAdapter = new PropertyAdapter(allProperties);
        rvMyProperties.setLayoutManager(new LinearLayoutManager(this));
        rvMyProperties.setAdapter(propertyAdapter);

        // ✅ مستمع النقر على العقار
        propertyAdapter.setOnItemClickListener(new PropertyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Property property) {
                openPropertyDetails(property);
            }
        });

        // ✅ مستمع النقر على الأزرار
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
                showDeleteConfirmation(property);
            }
        });
    }

    // ✅ دالة فتح تفاصيل العقار
    private void openPropertyDetails(Property property) {
        Intent intent = new Intent(this, activity_property_details.class);
        intent.putExtra("property_id", property.getId());
        intent.putExtra("property_title", property.getTitle());
        intent.putExtra("property_location", property.getLocation());
        intent.putExtra("property_price", property.getPrice());
        intent.putExtra("property_bedrooms", property.getBedrooms());
        intent.putExtra("property_bathrooms", property.getBathrooms());
        intent.putExtra("property_type", property.getType());
        startActivity(intent);
    }

// ✅ باقي الدوال (showRatingDialog, shareProperty, etc.) تبقى كما هي


    private void showRatingDialog(Property property) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("تقييم العقار");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);

        builder.setPositiveButton("تقييم", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (rating > 0) {
                // حفظ التقييم في قاعدة البيانات
                boolean success = databaseHelper.addPropertyRating(property.getId(), rating, comment);
                if (success) {
                    Toast.makeText(this, "شكراً لتقييمك!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "خطأ في حفظ التقييم", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void shareProperty(Property property) {
        String shareText = property.getTitle() + "\n" +
                "السعر: " + property.getPrice() + " ر.س\n" +
                "الموقع: " + property.getLocation() + "\n" +
                "عبر تطبيق مسكن";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "عقار للبيع/إيجار");

        startActivity(Intent.createChooser(shareIntent, "مشاركة العقار"));

        // تسجيل المشاركة
        databaseHelper.logPropertyShare(property.getId());
    }

    private void contactPropertyOwner(Property property) {
        // ✅ سجل بيانات العقار للمراقبة
        android.util.Log.d("MyProperties", "الاتصال بالعقار: " + property.getId() + " - " + property.getTitle());
        android.util.Log.d("MyProperties", "رقم الهاتف من القائمة: " + property.getContactPhone());

        // ✅ الحصول على رقم الهاتف من العقار مباشرة
        String phoneNumber = property.getContactPhone();

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            // ✅ إذا كان الرقم فارغاً، جلب البيانات الكاملة من قاعدة البيانات
            Property fullProperty = databaseHelper.getPropertyById(property.getId());
            if (fullProperty != null && fullProperty.getContactPhone() != null) {
                phoneNumber = fullProperty.getContactPhone();
                android.util.Log.d("MyProperties", "رقم الهاتف من قاعدة البيانات: " + phoneNumber);
            }
        }

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            // ✅ تنظيف وتحضير رقم الهاتف
            phoneNumber = phoneNumber.trim().replaceAll("\\s+", "").replaceAll("[^0-9+]", "");

            // ✅ إضافة رمز الدولة إذا لزم الأمر
            if (!phoneNumber.startsWith("+") && !phoneNumber.startsWith("00")) {
                if (phoneNumber.startsWith("0")) {
                    phoneNumber = "+966" + phoneNumber.substring(1);
                } else {
                    phoneNumber = "+966" + phoneNumber;
                }
            }

            android.util.Log.d("MyProperties", "رقم الهاتف النهائي: " + phoneNumber);

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                databaseHelper.logPropertyContact(property.getId());
                Toast.makeText(this, "جاري الاتصال بـ: " + phoneNumber, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "لا يوجد تطبيق للاتصال على جهازك", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "رقم الهاتف غير متوفر لهذا العقار", Toast.LENGTH_SHORT).show();
            android.util.Log.e("MyProperties", "رقم الهاتف فارغ للعقار: " + property.getId());
        }
    }

    private void showDeleteConfirmation(Property property) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("حذف العقار")
                .setMessage("هل أنت متأكد من حذف العقار: " + property.getTitle() + "؟")
                .setPositiveButton("حذف", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteProperty(property.getId());
                    if (deleted) {
                        Toast.makeText(this, "تم حذف العقار", Toast.LENGTH_SHORT).show();
                        loadProperties(); // إعادة تحميل القائمة
                    } else {
                        Toast.makeText(this, "خطأ في حذف العقار", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void setupClickListeners() {
        // زر العودة
       /* btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        // زر الفلتر
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterOptions();
            }
        });

        // زر إضافة أول عقار
        btnAddFirstProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_my_properties.this, add_property.class);
                startActivity(intent);
            }
        });

        // شريط البحث
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProperties(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProperties() {
        allProperties = databaseHelper.getAllProperties();

        // ✅ تسجيل للمراقبة - تحقق من البيانات المسترجعة
        android.util.Log.d("MyProperties", "عدد العقارات المسترجعة: " + allProperties.size());

        for (int i = 0; i < allProperties.size(); i++) {
            Property property = allProperties.get(i);
            android.util.Log.d("MyProperties", "العقار " + i + ": " + property.getTitle());
            android.util.Log.d("MyProperties", "   - ID: " + property.getId());
            android.util.Log.d("MyProperties", "   - الهاتف: " + property.getContactPhone());
            android.util.Log.d("MyProperties", "   - الاتصال: " + property.getContactName());
            android.util.Log.d("MyProperties", "   - عدد الصور: " + (property.hasImages() ? property.getImagePaths().size() : 0));
            if (property.hasImages()) {
                android.util.Log.d("MyProperties", "   - أول صورة: " + property.getFirstImagePath());
            }
        }

        updateUI();
    }

    private void updateUI() {
        // ✅ استخدام دالة updateList الجديدة
        propertyAdapter.updateList(allProperties);

        // تحديث العدد
        tvPropertiesCount.setText(allProperties.size() + " عقار");

        // إظهار/إخفاء رسالة عدم وجود عقارات
        if (allProperties.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvMyProperties.setVisibility(View.GONE);
            tvPropertiesCount.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvMyProperties.setVisibility(View.VISIBLE);
            tvPropertiesCount.setVisibility(View.VISIBLE);
        }
    }

    private void filterProperties(String query) {
        List<Property> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allProperties);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Property property : allProperties) {
                if (property.getTitle() != null && property.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        property.getLocation() != null && property.getLocation().toLowerCase().contains(lowerCaseQuery) ||
                        property.getPrice() != null && property.getPrice().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(property);
                }
            }
        }

        // ✅ استخدام دالة updateList الجديدة
        propertyAdapter.updateList(filteredList);
        tvPropertiesCount.setText(filteredList.size() + " عقار");

        // ✅ تسجيل للمراقبة
        android.util.Log.d("MyProperties", "نتائج البحث: " + filteredList.size() + " عقار");
    }


    private void showPropertyDetails(Property property) {
        // ✅ عرض تفاصيل العقار مع معلومات الصور
        String details = "عقار: " + property.getTitle() +
                "\nالموقع: " + property.getLocation() +
                "\nالسعر: " + property.getPrice() + " ر.س" +
                "\nالغرف: " + property.getBedrooms() +
                "\nالحمامات: " + property.getBathrooms() +
                "\nعدد الصور: " + (property.hasImages() ? property.getImagePaths().size() : 0);

        Toast.makeText(this, details, Toast.LENGTH_LONG).show();

        // لاحقاً يمكنك فتح صفحة تفاصيل العقار:
        // Intent intent = new Intent(this, PropertyDetailsActivity.class);
        // intent.putExtra("property_id", property.getId());
        // startActivity(intent);
    }

    private void showFilterOptions() {
        Toast.makeText(this, "خيارات الفلتر - قيد التطوير", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // تحديث البيانات عند العودة للنشاط
        loadProperties();
    }





    //دالة شريط التنقل
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_properties);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    openMainActivity();
                    return true;
                } else if (id == R.id.nav_search) {
                    openSearchActivity();
                    return true;
                } else if (id == R.id.nav_add) {
                    openAddProperty();
                    return true;
                } else if (id == R.id.nav_my_properties) {

                    return true;
                }
                return false;
            }
        });
    }


    private void openSearchActivity() {
        try {
            Intent intent = new Intent(activity_my_properties.this, SearchActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة البحث: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Error opening SearchActivity: " + e.getMessage());
        }
    }
    private void openMainActivity() {
        try {
            Intent intent = new Intent(activity_my_properties.this, MainActivity.class);
            startActivity(intent);
            // لا تستخدم finish() هنا للحفاظ على تجربة المستخدم
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة الرئيسية: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("SearchActivity", "Error opening MainActivity: " + e.getMessage());
        }
    }

    private void openAddProperty() {
        Intent intent = new Intent(activity_my_properties.this, add_property.class);
        startActivity(intent);
    }

    private void openFavoritesActivity() {
        Intent intent = new Intent(activity_my_properties.this, FavoritesActivity.class);
        startActivity(intent);
    }

    private void openMyProperties() {
        // ✅ عرض عقارات المستخدم
        List<Property> myProperties = databaseHelper.getAllProperties();
        if (myProperties.isEmpty()) {
            Toast.makeText(this, "لم تقم بإضافة أي عقارات بعد", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(activity_my_properties.this, activity_my_properties.class);
            startActivity(intent);
        }
    }


}