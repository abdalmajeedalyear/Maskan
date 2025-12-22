package com.example.maskan;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageButton btnMenu, btnNotifications, btnFilter, btnFavorites; // ✅ أضف btnFavorites
    private CardView cardForRent, cardForSale;
    private RecyclerView rvFeatured, rvNearby;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUserStatus();
        setContentView(R.layout.activity_main);

        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);
        setupToolbar();
        setupRecyclerViews();
        setupClickListeners();

        // ✅ اختبار المفضلات (مؤقت - احذفه لاحقاً)
        testFavoritesSystem();

        // إعداد شريط التنقل السفلي
        setupBottomNavigation();
    }

    private void initializeViews() {

        // العثور على جميع العناصر
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        btnMenu = findViewById(R.id.btnMenu);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnFilter = findViewById(R.id.btnFilter);

        cardForRent = findViewById(R.id.cardForRent);
        cardForSale = findViewById(R.id.cardForSale);

        rvFeatured = findViewById(R.id.rvFeatured);
        rvNearby = findViewById(R.id.rvNearby);

        // ✅ إذا لم يكن زر المفضلات موجوداً في XML، سنضيفه برمجياً لاحقاً
        if (btnFavorites == null) {
            // سنتعامل مع هذا في الخطوة التالية
        }
    }


    private void setupPropertyActionListeners(PropertyAdapter adapter) {
        adapter.setOnButtonClickListener(new PropertyAdapter.OnButtonClickListener() {
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
                // في الشاشة الرئيسية لا نسمح بالحذف، فقط في "عقاراتي"
                Toast.makeText(MainActivity.this, "يمكن حذف العقارات فقط من صفحة 'عقاراتي'", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRatingDialog(Property property) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("تقييم العقار: " + property.getTitle());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);

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
        // الحصول على رقم الهاتف من قاعدة البيانات أولاً
        Property fullProperty = databaseHelper.getPropertyById(property.getId());
        if (fullProperty != null && fullProperty.getContactPhone() != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + fullProperty.getContactPhone()));
            startActivity(intent);

            // تسجيل الاتصال
            databaseHelper.logPropertyContact(property.getId());
        } else {
            Toast.makeText(this, "رقم الهاتف غير متوفر لهذا العقار", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupToolbar() {
        // ❌ إزالة أو تعليق هذا السطر:
        // setSupportActionBar(toolbar);

        // ✅ استبدله بهذا:
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // أو إذا كنت تريد إخفاء ال Action Bar تماماً:
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void setupRecyclerViews() {
        // ✅ الآن نستخدم البيانات الحقيقية من قاعدة البيانات
        int totalProperties = databaseHelper.getPropertiesCount();
        Toast.makeText(this, "عدد العقارات: " + totalProperties, Toast.LENGTH_SHORT).show();

        // العقارات المميزة (آخر 3 عقارات مضافة)
        List<Property> featuredProperties = getFeaturedPropertiesFromDB();
            PropertyAdapter featuredAdapter = new PropertyAdapter(featuredProperties);
        rvFeatured.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFeatured.setAdapter(featuredAdapter);

        // ✅ إضافة مستمع الأزرار للعقارات المميزة
        setupPropertyActionListeners(featuredAdapter);

        // العقارات القريبة (جميع العقارات للإيجار)
        List<Property> nearbyProperties = getNearbyPropertiesFromDB();
        PropertyAdapter nearbyAdapter = new PropertyAdapter(nearbyProperties);
        rvNearby.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvNearby.setAdapter(nearbyAdapter);

        // ✅ إضافة مستمع الأزرار للعقارات القريبة
        setupPropertyActionListeners(nearbyAdapter);

        // ✅ إضافة مستمع النقر للعقارات
        setupPropertyClickListeners(featuredAdapter, nearbyAdapter);

        // ✅ عرض رسالة إذا لم توجد عقارات
        if (featuredProperties.isEmpty()) {
            Toast.makeText(this, "لا توجد عقارات مضافة بعد. أضف عقارك الأول!", Toast.LENGTH_LONG).show();
        }
    }

    // ✅ إضافة مستمع النقر للعقارات
    private void setupPropertyClickListeners(PropertyAdapter featuredAdapter, PropertyAdapter nearbyAdapter) {
        featuredAdapter.setOnItemClickListener(new PropertyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Property property) {
                openPropertyDetails(property);
            }
        });

        nearbyAdapter.setOnItemClickListener(new PropertyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Property property) {
                openPropertyDetails(property);
            }
        });
    }

    // ✅ دالة فتح تفاصيل العقار
    private void openPropertyDetails(Property property) {
        Intent intent = new Intent(MainActivity.this, activity_property_details.class);

        // ✅ تمرير property_id بدلاً من الاعتماد على العنوان فقط
        intent.putExtra("property_id", property.getId());
        intent.putExtra("property_title", property.getTitle());
        intent.putExtra("property_location", property.getLocation());
        intent.putExtra("property_price", property.getPrice());
        intent.putExtra("property_bedrooms", property.getBedrooms());
        intent.putExtra("property_bathrooms", property.getBathrooms());
        intent.putExtra("property_type", property.getType());

        startActivity(intent);
    }

    // ✅ جلب العقارات المميزة من قاعدة البيانات
    private List<Property> getFeaturedPropertiesFromDB() {
        List<Property> allProperties = databaseHelper.getAllProperties();

        // إذا كانت هناك عقارات، نعرض آخر 3 عقارات كمميزة
        if (allProperties.size() > 3) {
            return allProperties.subList(0, Math.min(3, allProperties.size()));
        } else {
            return allProperties;
        }
    }

    // ✅ جلب العقارات القريبة من قاعدة البيانات (جميع العقارات للإيجار)
    private List<Property> getNearbyPropertiesFromDB() {
        return databaseHelper.getPropertiesByType("للإيجار");
    }

    private void setupClickListeners() {
        // زر القائمة الجانبية
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // زر الإشعارات
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "الإشعارات", Toast.LENGTH_SHORT).show();
            }
        });

        // زر الفلتر
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdvancedFilterDialog();
            }
        });

        // ✅ زر المفضلات - إذا كان الزر موجوداً
        if (btnFavorites != null) {
            btnFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFavoritesActivity();
                }
            });
        }

        // زر إضافة عقار


        // قسم للإيجار - الانتقال لواجهة عقارات الإيجار
        cardForRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RentPropertiesActivity.class);
                startActivity(intent);
            }
        });

        // قسم للبيع - الانتقال لواجهة عقارات البيع
        cardForSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SalePropertiesActivity.class);
                startActivity(intent);
            }
        });




        // القائمة الجانبية
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    openProfile();
                } else if (id == R.id.nav_my_properties) {
                    openMyProperties();
                } else if (id == R.id.nav_favorites) {
                    openFavoritesActivity(); // ✅ استخدام الدالة الجديدة
                } else if (id == R.id.nav_settings) {
                    openSettings();
                } else if (id == R.id.nav_help) {
                    openHelp();
                } else if (id == R.id.nav_logout) {
                    logout();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }



    private void openProfile() {
        Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(intent);
        Toast.makeText(this, "فتح الملف الشخصي", Toast.LENGTH_SHORT).show();
    }

    private void openMyProperties() {
        // ✅ عرض عقارات المستخدم
        List<Property> myProperties = databaseHelper.getAllProperties();
        if (myProperties.isEmpty()) {
            Toast.makeText(this, "لم تقم بإضافة أي عقارات بعد", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, activity_my_properties.class);
            startActivity(intent);
        }
    }

    // ❌ إزالة الدالة القديمة
    // private void openFavorites() { ... }

    private void openSettings() {
        Toast.makeText(this, "الإعدادات", Toast.LENGTH_SHORT).show();
    }

    private void openHelp() {
        Toast.makeText(this, "المساعدة", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Intent intent = new Intent(MainActivity.this,login.class);
        startActivity(intent);
        finish();
    }

    // ✅ إضافة هذه الدالة لتحديث البيانات عند العودة للتطبيق
    @Override
    protected void onResume() {
        super.onResume();
        // تحديث القوائم عند العودة للتطبيق
        setupRecyclerViews();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    // ✅ دالة محسنة للفلترة حسب نوع العرض
    private List<Property> getPropertiesByOfferType(String offerType) {
        List<Property> allProperties = databaseHelper.getAllProperties();
        List<Property> filteredProperties = new ArrayList<>();

        for (Property property : allProperties) {
            if (property.getType() != null && property.getType().contains(offerType)) {
                filteredProperties.add(property);
            }
        }

        return filteredProperties;
    }

    // ✅ دالة مساعدة للفلترة حسب نوع العقار
    private List<Property> filterByPropertyType(List<Property> properties, String propertyType) {
        List<Property> filteredProperties = new ArrayList<>();

        for (Property property : properties) {
            if (property.getTitle() != null && property.getTitle().toLowerCase().contains(propertyType.toLowerCase())) {
                filteredProperties.add(property);
            }
        }

        return filteredProperties;
    }

    // ✅ دالة لعرض الفلترة المتقدمة
    private void showAdvancedFilterDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("فلترة متقدمة");

        String[] filterOptions = {
                "جميع العقارات",
                "شقق للإيجار",
                "شقق للبيع",
                "فلل للإيجار",
                "فلل للبيع",
                "أراضي للبيع",
                "مكاتب للإيجار",
                "محلات تجارية"
        };

        builder.setItems(filterOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                applyAdvancedFilter(filterOptions[which]);
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    // ✅ دالة لتطبيق الفلترة المتقدمة
    private void applyAdvancedFilter(String filter) {
        List<Property> filteredList = new ArrayList<>();
        List<Property> allProperties = databaseHelper.getAllProperties();

        switch (filter) {
            case "شقق للإيجار":
                filteredList = getPropertiesByOfferType("إيجار");
                filteredList = filterByPropertyType(filteredList, "شقة");
                break;
            case "شقق للبيع":
                filteredList = getPropertiesByOfferType("بيع");
                filteredList = filterByPropertyType(filteredList, "شقة");
                break;
            case "فلل للإيجار":
                filteredList = getPropertiesByOfferType("إيجار");
                filteredList = filterByPropertyType(filteredList, "فيلا");
                break;
            case "فلل للبيع":
                filteredList = getPropertiesByOfferType("بيع");
                filteredList = filterByPropertyType(filteredList, "فيلا");
                break;
            case "أراضي للبيع":
                filteredList = getPropertiesByOfferType("بيع");
                filteredList = filterByPropertyType(filteredList, "أرض");
                break;
            case "مكاتب للإيجار":
                filteredList = getPropertiesByOfferType("إيجار");
                filteredList = filterByPropertyType(filteredList, "مكتب");
                break;
            case "محلات تجارية":
                filteredList = filterByPropertyType(allProperties, "محل");
                break;
            default:
                filteredList = allProperties;
        }

        // تحديث الواجهة
        updateRecyclerView(filteredList);
        Toast.makeText(this, "عرض: " + filter, Toast.LENGTH_SHORT).show();
    }

    // ✅ دالة لتحديث الـ RecyclerView
    private void updateRecyclerView(List<Property> properties) {
        // تحديث العقارات المميزة
        List<Property> featuredList = properties.subList(0, Math.min(3, properties.size()));
        PropertyAdapter featuredAdapter = new PropertyAdapter(featuredList);
        rvFeatured.setAdapter(featuredAdapter);

        // ✅ إضافة مستمع الأزرار
        setupPropertyActionListeners(featuredAdapter);

        // تحديث العقارات القريبة (أول 6 عقارات)
        List<Property> nearbyList = properties.subList(0, Math.min(6, properties.size()));
        PropertyAdapter nearbyAdapter = new PropertyAdapter(nearbyList);
        rvNearby.setAdapter(nearbyAdapter);

        // ✅ إضافة مستمع الأزرار
        setupPropertyActionListeners(nearbyAdapter);

        // ✅ إضافة مستمع النقر مرة أخرى
        setupPropertyClickListeners(featuredAdapter, nearbyAdapter);
    }

    // ✅ دالة اختبار المفضلات (مؤقتة - احذفها بعد التأكد من عمل النظام)
    private void testFavoritesSystem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // انتظر قليلاً لتهدئة قاعدة البيانات
                    Thread.sleep(1000);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // اختبار جلب العقارات
                            List<Property> allProperties = databaseHelper.getAllProperties();
                            android.util.Log.d("FavoritesTest", "عدد العقارات: " + allProperties.size());

                            // اختبار المفضلات
                            List<Property> favorites = databaseHelper.getFavoriteProperties();
                            android.util.Log.d("FavoritesTest", "عدد المفضلات: " + favorites.size());

                            if (!allProperties.isEmpty()) {
                                // اختبار إضافة مفضلة
                                int testId = allProperties.get(0).getId();
                                boolean isFavorite = databaseHelper.isPropertyInFavorites(testId);
                                android.util.Log.d("FavoritesTest", "العقار " + testId + " في المفضلات: " + isFavorite);
                            }
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("FavoritesTest", "خطأ في الاختبار: " + e.getMessage());
                }
            }
        }).start();
    }










    private void checkUserStatus() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        Log.d("MAIN_DEBUG", "isLoggedIn: " + isLoggedIn);

        if (!isLoggedIn) {
            Log.d("MAIN_DEBUG", "غير مسجل دخول - الذهاب إلى Login");
            goToActivity(login.class);
            return;
        }

        String userEmail = prefs.getString("user_email", "");
        Log.d("MAIN_DEBUG", "userEmail: " + userEmail);

        // ✅ إزالة forceQuestions (التعليق المؤقت)
        // boolean forceQuestions = true; // ⚠️ غير هذا إلى false

        // ✅ الكود الصحيح:
        boolean questionsCompleted = prefs.getBoolean("questions_completed_" + userEmail, false);
        Log.d("MAIN_DEBUG", "questions_completed key: questions_completed_" + userEmail);
        Log.d("MAIN_DEBUG", "questionsCompleted: " + questionsCompleted);

        if (!questionsCompleted) {
            Log.d("MAIN_DEBUG", "لم يكمل الأسئلة - الذهاب إلى QuestionsActivity");
            goToActivity(QuestionsActivity.class);
            return;
        }

        Log.d("MAIN_DEBUG", "كل شيء جيد - تحميل MainActivity");
        loadMainUI();
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        finish();
    }

    private void loadMainUI() {
        // تحميل التخطيط الرئيسي
        setContentView(R.layout.activity_main);

        // هنا يمكنك تهيئة باقي عناصر الواجهة
        setupNavigation();
        loadUserData();
    }

    private void setupNavigation() {
        // TODO: إعداد Bottom Navigation أو Navigation Drawer
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "مستخدم");
        String userEmail = prefs.getString("user_email", "");

        Log.d("MainActivity", "مرحباً " + userName + " (" + userEmail + ")");

        // عرض اسم المستخدم في الواجهة إذا كان هناك TextView
        // TextView tvWelcome = findViewById(R.id.tvWelcome);
        // if (tvWelcome != null) {
        //     tvWelcome.setText("مرحباً " + userName);
        // }
    }





    //دالة شريط التنقل
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // نحن بالفعل في الصفحة الرئيسية
                    return true;
                } else if (id == R.id.nav_search) {
                    openSearchActivity();
                    return true;
                } else if (id == R.id.nav_add) {
                    openAddProperty();
                    return true;
                } else if (id == R.id.nav_my_properties) {
                    openMyProperties();
                    return true;
                }
                return false;
            }
        });
    }

    private void openSearchActivity() {
        try {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة البحث: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Error opening SearchActivity: " + e.getMessage());
        }
    }

    private void openAddProperty() {
        Intent intent = new Intent(MainActivity.this, add_property.class);
        startActivity(intent);
    }

    // ✅ دالة فتح واجهة المفضلات
    private void openFavoritesActivity() {
        Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
        startActivity(intent);
    }





}