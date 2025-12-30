package com.example.maskan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    // Tags للـ Log
    private static final String TAG = "PROFILE_ACTIVITY";

    // العناصر UI
    private ImageView ivProfile;
    private TextInputEditText etFullName, etEmail, etPhone;
    private TextView tvPropertyTypes, tvDownloadReason, tvDiscoverySource;
    private TextView tvFavoritesCount, tvPropertiesCount, tvMemberSince, tvLastActive;
    private Button btnChangePhoto, btnSave, btnCancel, btnEditPreferences, btnLogout;
    private List<Property> allProperties = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    // المتغيرات
    private DatabaseHelper dbHelper;
    private User currentUser;
    private String profileImagePath = "";
    private boolean isEditing = false;

    // ثوابت
    private static final int PICK_IMAGE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_profile);
            Log.d(TAG, "✅ onCreate: تم تحميل التخطيط بنجاح");
        } catch (Exception e) {
            Log.e(TAG, "❌ فشل تحميل التخطيط: " + e.getMessage());
            Toast.makeText(this, "خطأ في واجهة المستخدم", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1. التحقق من تسجيل الدخول
        if (!isUserLoggedIn()) {
            Log.w(TAG, "⚠️ المستخدم غير مسجل الدخول");
            goToLogin();
            return;
        }

        // 2. تهيئة العناصر UI
        try {
            initViews();
            Log.d(TAG, "✅ تهيئة العناصر UI تمت بنجاح");
        } catch (Exception e) {
            Log.e(TAG, "❌ فشل تهيئة العناصر UI: " + e.getMessage());
            Toast.makeText(this, "خطأ في العناصر", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. تعيين الأحداث
        setupListeners();
        Log.d(TAG, "✅ تعيين الأحداث تم بنجاح");

        // 4. تهيئة قاعدة البيانات
        try {
            dbHelper = DatabaseHelper.getInstance(this);
            Log.d(TAG, "✅ DatabaseHelper تهيئته بنجاح");
        } catch (Exception e) {
            Log.e(TAG, "❌ فشل تهيئة قاعدة البيانات: " + e.getMessage());
            Toast.makeText(this, "خطأ في قاعدة البيانات", Toast.LENGTH_SHORT).show();
            dbHelper = null;
        }

        // 5. تحميل بيانات المستخدم
        loadUserData();
        return_number_my_property();

    }



    private void initViews() {
        try {
            // الصورة والزر
            ivProfile = findViewById(R.id.ivProfile);
            btnChangePhoto = findViewById(R.id.btnChangePhoto);

            // حقول النص
            etFullName = findViewById(R.id.etFullName);
            etEmail = findViewById(R.id.etEmail);
            etPhone = findViewById(R.id.etPhone);

            // تفضيلات
            tvPropertyTypes = findViewById(R.id.tvPropertyTypes);
            tvDownloadReason = findViewById(R.id.tvDownloadReason);
            tvDiscoverySource = findViewById(R.id.tvDiscoverySource);

            // إحصائيات
            tvFavoritesCount = findViewById(R.id.tvFavoritesCount);
            tvPropertiesCount = findViewById(R.id.tvPropertiesCount);
            tvMemberSince = findViewById(R.id.tvMemberSince);
            tvLastActive = findViewById(R.id.tvLastActive);

            // أزرار
            btnSave = findViewById(R.id.btnSave);
            btnCancel = findViewById(R.id.btnCancel);
            btnEditPreferences = findViewById(R.id.btnEditPreferences);
            btnLogout = findViewById(R.id.btnLogout);

            Log.d(TAG, "✅ جميع العناصر UI تم العثور عليها");
        } catch (Exception e) {
            Log.e(TAG, "❌ عنصر مفقود في التخطيط: " + e.getMessage());
            throw e; // لإيقاف التطبيق لو كان العنصر مهم
        }
    }

    private void setupListeners() {
        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileChanges();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEditing();
            }
        });

        btnEditPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPreferences();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // تفعيل التعديل عند النقر
        View.OnClickListener enableEditListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing();
            }
        };

        etFullName.setOnClickListener(enableEditListener);
        etPhone.setOnClickListener(enableEditListener);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");

        Log.d(TAG, "📧 محاولة جلب بيانات للمستخدم: " + userEmail);

        if (userEmail.isEmpty()) {
            Log.e(TAG, "❌ البريد الإلكتروني فارغ!");
            Toast.makeText(this, "يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        try {
            // جلب بيانات المستخدم من قاعدة البيانات
            if (dbHelper != null) {
                currentUser = dbHelper.getUserByEmail(userEmail);
            }

            if (currentUser != null) {
                Log.d(TAG, "✅ تم جلب بيانات المستخدم من DB");

                // عرض البيانات
                etFullName.setText(currentUser.getFullName());
                etEmail.setText(currentUser.getEmail());

                String phone = currentUser.getPhone();
                etPhone.setText(phone != null ? phone : "");

                // تحميل الصورة إذا كانت موجودة
                String imagePath = currentUser.getProfileImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    profileImagePath = imagePath;
                    loadProfileImage();
                }

            } else {
                Log.w(TAG, "⚠️ المستخدم غير موجود في DB، جاري استخدام SharedPreferences");

                // استخدام البيانات من SharedPreferences كبديل
                String savedName = prefs.getString("user_name", "مستخدم");
                String savedPhone = prefs.getString("user_phone", "");

                etFullName.setText(savedName);
                etEmail.setText(userEmail);
                etPhone.setText(savedPhone);

                // إنشاء مستخدم جديد إذا لزم الأمر
                if (dbHelper != null) {
                    createUserIfNotExists(savedName, userEmail, savedPhone);
                }
            }

            // تحميل التفضيلات والإحصائيات
            loadUserPreferences();
            loadUserStatistics();

        } catch (Exception e) {
            Log.e(TAG, "❌ خطأ في loadUserData: " + e.getMessage());
            Toast.makeText(this, "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
        }
    }

    private void createUserIfNotExists(String fullName, String email, String phone) {
        try {
            if (dbHelper == null) return;

            // تحقق أولاً
            User existingUser = dbHelper.getUserByEmail(email);
            if (existingUser != null) {
                currentUser = existingUser;
                return;
            }

            // إنشاء مستخدم جديد
            // كلمة مرور افتراضية (يجب تغييرها في الإصدار النهائي)
            String defaultPassword = "123456";
            boolean created = dbHelper.addUser(fullName, email, phone, defaultPassword, "");

            if (created) {
                Log.d(TAG, "✅ تم إنشاء مستخدم جديد");
                currentUser = dbHelper.getUserByEmail(email);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ فشل إنشاء مستخدم: " + e.getMessage());
        }
    }
    //______________________________________________________________________________________________

    private void loadUserPreferences() {
        if (currentUser == null) {
            setDefaultPreferences();
            return;
        }

        String userEmail = currentUser.getEmail();
        Log.d(TAG, "🔍 جلب تفضيلات للمستخدم: " + userEmail);

        // الخطوة 1: البحث في قاعدة البيانات أولاً
        boolean foundInDB = loadPreferencesFromDatabase(userEmail);

        // الخطوة 2: إذا لم توجد في قاعدة البيانات، ابحث في SharedPreferences
        if (!foundInDB) {
            Log.d(TAG, "⚠️ لم توجد في قاعدة البيانات، جاري البحث في SharedPreferences");
            loadPreferencesFromSharedPrefs(userEmail);
        }
    }

    private boolean loadPreferencesFromDatabase(String userEmail) {
        if (dbHelper == null) return false;

        Cursor cursor = null;
        try {
            cursor = dbHelper.getUserPreferences(userEmail);

            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "✅ تم العثور على تفضيلات في قاعدة البيانات");

                String discoverySource = "";
                String downloadReason = "";
                String propertyTypes = "";

                // قراءة الأعمدة بشكل آمن
                int discoveryIndex = cursor.getColumnIndex("pref_discovery_source");
                int reasonIndex = cursor.getColumnIndex("pref_download_reason");
                int typesIndex = cursor.getColumnIndex("pref_property_types");

                if (discoveryIndex != -1) discoverySource = cursor.getString(discoveryIndex);
                if (reasonIndex != -1) downloadReason = cursor.getString(reasonIndex);
                if (typesIndex != -1) propertyTypes = cursor.getString(typesIndex);

                // إذا كانت البيانات فارغة، ربما الأعمدة مختلفة
                if (discoverySource.isEmpty() && downloadReason.isEmpty() && propertyTypes.isEmpty()) {
                    // جرب قراءة الأعمدة البديلة
                    discoverySource = getColumnValue(cursor, "discovery_source", "discovery");
                    downloadReason = getColumnValue(cursor, "download_reason", "reason");
                    propertyTypes = getColumnValue(cursor, "property_types", "property_type");
                }

                if (!discoverySource.isEmpty() || !downloadReason.isEmpty() || !propertyTypes.isEmpty()) {
                    displayPreferences(discoverySource, downloadReason, propertyTypes);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            Log.e(TAG, "❌ خطأ في قراءة قاعدة البيانات: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private String getColumnValue(Cursor cursor, String... possibleColumnNames) {
        for (String columnName : possibleColumnNames) {
            int index = cursor.getColumnIndex(columnName);
            if (index != -1) {
                String value = cursor.getString(index);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return "";
    }

    private void loadPreferencesFromSharedPrefs(String userEmail) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        Log.d(TAG, "🔍 البحث في SharedPreferences للمستخدم: " + userEmail);

        // المحاولة 1: مفاتيح عامة (بدون email)
        String discoverySource = prefs.getString("pref_discovery_source", "");
        String downloadReason = prefs.getString("pref_download_reason", "");
        String propertyTypes = prefs.getString("pref_property_types", "");

        // المحاولة 2: مفاتيح مرتبطة بالبريد الإلكتروني
        if (discoverySource.isEmpty()) {
            discoverySource = prefs.getString("pref_discovery_" + userEmail, "");
        }
        if (downloadReason.isEmpty()) {
            downloadReason = prefs.getString("pref_reason_" + userEmail, "");
        }
        if (propertyTypes.isEmpty()) {
            propertyTypes = prefs.getString("pref_property_types_" + userEmail, "");
        }

        // المحاولة 3: مفاتيح بديلة
        if (discoverySource.isEmpty()) {
            discoverySource = prefs.getString("discovery_source", "");
        }
        if (downloadReason.isEmpty()) {
            downloadReason = prefs.getString("download_reason", "");
        }
        if (propertyTypes.isEmpty()) {
            propertyTypes = prefs.getString("property_types", "");
        }

        // المحاولة 4: مفاتيح من QuestionsActivity
        if (discoverySource.isEmpty()) {
            discoverySource = prefs.getString("user_pref_discovery", "");
        }
        if (downloadReason.isEmpty()) {
            downloadReason = prefs.getString("user_pref_reason", "");
        }
        if (propertyTypes.isEmpty()) {
            propertyTypes = prefs.getString("user_pref_types", "");
        }

        Log.d(TAG, "البيانات الموجودة في SharedPreferences:");
        Log.d(TAG, "- discoverySource: " + discoverySource);
        Log.d(TAG, "- downloadReason: " + downloadReason);
        Log.d(TAG, "- propertyTypes: " + propertyTypes);

        if (discoverySource.isEmpty() && downloadReason.isEmpty() && propertyTypes.isEmpty()) {
            Log.d(TAG, "⚠️ لا توجد بيانات في SharedPreferences أيضاً");
            setDefaultPreferences();
        } else {
            displayPreferences(discoverySource, downloadReason, propertyTypes);

            // حفظ في قاعدة البيانات للمرة القادمة
            savePreferencesToDatabase(userEmail, discoverySource, downloadReason, propertyTypes);
        }
    }

    private void savePreferencesToDatabase(String userEmail, String discovery, String reason, String types) {
        if (dbHelper == null) {
            Log.e(TAG, "❌ dbHelper هو null، لا يمكن الحفظ");
            return;
        }

        // تأكد من وجود جدول التفضيلات أولاً
        dbHelper.ensurePreferencesTableExists();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean exists = dbHelper.userPreferencesExist(userEmail);

                    if (exists) {
                        // تحديث السجل الموجود
                        boolean updated = dbHelper.updateUserPreferences(userEmail, discovery, reason, types);
                        if (updated) {
                            Log.d(TAG, "✅ تم تحديث التفضيلات في قاعدة البيانات");
                        } else {
                            Log.e(TAG, "❌ فشل تحديث التفضيلات");
                        }
                    } else {
                        // إنشاء سجل جديد
                        boolean inserted = dbHelper.insertUserPreferences(userEmail, discovery, reason, types);
                        if (inserted) {
                            Log.d(TAG, "✅ تم إدخال التفضيلات في قاعدة البيانات");
                        } else {
                            Log.e(TAG, "❌ فشل إدخال التفضيلات");
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "❌ استثناء في savePreferencesToDatabase: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void displayPreferences(String discoverySource, String downloadReason, String propertyTypes) {
        // تحويل discoverySource إلى نص عربي
        String discoveryText = getDiscoverySourceText(discoverySource);

        // تحويل propertyTypes إلى نص عربي
        String propertyTypesText = getPropertyTypesText(propertyTypes);

        // عرض البيانات
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDiscoverySource.setText(discoveryText);
                tvDownloadReason.setText(downloadReason != null && !downloadReason.isEmpty() ? downloadReason : "غير محدد");
                tvPropertyTypes.setText(propertyTypesText);

                Log.d(TAG, "✅ التفضيلات المعروضة:");
                Log.d(TAG, "- مصدر الاكتشاف: " + discoveryText);
                Log.d(TAG, "- سبب التنزيل: " + downloadReason);
                Log.d(TAG, "- نوع العقارات: " + propertyTypesText);
            }
        });
    }

    private void setDefaultPreferences() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDiscoverySource.setText("غير محدد");
                tvDownloadReason.setText("غير محدد");
                tvPropertyTypes.setText("غير محدد");
                Log.d(TAG, "⚠️ استخدام التفضيلات الافتراضية");
            }
        });
    }

    private String getDiscoverySourceText(String source) {
        if (source == null || source.isEmpty()) {
            return "غير محدد";
        }

        switch (source.toLowerCase()) {
            case "friend":
            case "صديق":
                return "صديق";
            case "social_media":
            case "وسائل التواصل الاجتماعي":
                return "وسائل التواصل الاجتماعي";
            case "search":
            case "بحث على الإنترنت":
                return "بحث على الإنترنت";
            case "ads":
            case "إعلان":
                return "إعلان";
            case "other":
            case "أخرى":
                return "أخرى";
            default:
                return source; // عرض القيمة الأصلية إذا لم تتعرف عليها
        }
    }

    private String getPropertyTypesText(String types) {
        if (types == null || types.isEmpty()) {
            return "غير محدد";
        }

        switch (types.toLowerCase()) {
            case "sale":
            case "عقارات للبيع":
                return "عقارات للبيع";
            case "rent":
            case "عقارات للإيجار":
                return "عقارات للإيجار";
            case "sale,rent":
            case "البيع والإيجار":
                return "البيع والإيجار";
            default:
                return types; // عرض القيمة الأصلية
        }
    }

    //______________________________________________________________________________________________

    private String getArabicText(String value, String type) {
        if (value == null || value.isEmpty()) return "غير محدد";

        if (type.equals("discovery")) {
            switch (value.toLowerCase()) {
                case "friend": return "صديق";
                case "social_media": return "وسائل التواصل";
                case "search": return "بحث على الإنترنت";
                case "ads": return "إعلان";
                case "other": return "أخرى";
                default: return value;
            }
        } else if (type.equals("property")) {
            switch (value.toLowerCase()) {
                case "sale": return "عقارات للبيع";
                case "rent": return "عقارات للإيجار";
                case "sale,rent": return "البيع والإيجار";
                default: return value;
            }
        }

        return value;
    }

    private void loadUserStatistics() {
        if (dbHelper == null || currentUser == null) {
            setDefaultStatistics();
            return;
        }

        String userEmail = currentUser.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            setDefaultStatistics();
            return;
        }

        try {
            // 1. عدد المفضلات
            int favoritesCount = dbHelper.getUserFavoritesCount(userEmail);
            tvFavoritesCount.setText(String.valueOf(favoritesCount));

            // 2. عدد العقارات المضافة
            int propertiesCount = dbHelper.getUserPropertiesCount(userEmail);
            //tvPropertiesCount.setText(String.valueOf(propertiesCount));

            // 3. تاريخ التسجيل
            String createdAt = currentUser.getCreatedAt();
            if (createdAt != null && !createdAt.isEmpty()) {
                try {
                    // مثال: "2024-12-24 15:30:45" → "2024"
                    String year = createdAt.substring(0, 4);
                    tvMemberSince.setText(year);
                } catch (Exception e) {
                    tvMemberSince.setText("2024");
                }
            } else {
                tvMemberSince.setText("2024");
            }

            // 4. آخر نشاط
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            String today = dateFormat.format(new Date());
            tvLastActive.setText(today);

            Log.d(TAG, "✅ تم تحميل الإحصائيات");

        } catch (Exception e) {
            Log.e(TAG, "❌ خطأ في تحميل الإحصائيات: " + e.getMessage());
            setDefaultStatistics();
        }
    }

    private void setDefaultStatistics() {
        tvFavoritesCount.setText("0");
        //tvPropertiesCount.setText("0");
        tvMemberSince.setText("2024");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        String today = dateFormat.format(new Date());
        tvLastActive.setText(today);
    }

    private void loadProfileImage() {
        if (profileImagePath == null || profileImagePath.isEmpty()) {
            ivProfile.setImageResource(android.R.drawable.ic_menu_camera);
            return;
        }

        try {
            File imgFile = new File(profileImagePath);
            if (imgFile.exists()) {
                ivProfile.setImageBitmap(BitmapFactory.decodeFile(profileImagePath));
            } else {
                ivProfile.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ خطأ في تحميل الصورة: " + e.getMessage());
            ivProfile.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                try {
                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        profileImagePath = cursor.getString(columnIndex);
                        cursor.close();

                        // عرض الصورة المحددة
                        ivProfile.setImageBitmap(BitmapFactory.decodeFile(profileImagePath));

                        enableEditing();
                        Log.d(TAG, "✅ تم اختيار صورة جديدة");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ خطأ في اختيار الصورة: " + e.getMessage());
                    Toast.makeText(this, "خطأ في اختيار الصورة", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void enableEditing() {
        if (!isEditing) {
            isEditing = true;
            etFullName.setEnabled(true);
            etPhone.setEnabled(true);
            btnSave.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            Log.d(TAG, "✅ تم تفعيل وضع التعديل");
        }
    }

    private void saveProfileChanges() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("الرجاء إدخال الاسم الكامل");
            etFullName.requestFocus();
            return;
        }

        if (dbHelper == null || currentUser == null) {
            Toast.makeText(this, "خطأ في الاتصال بقاعدة البيانات", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري حفظ التغييرات...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            boolean updated = dbHelper.updateUser(currentUser.getEmail(), fullName, phone, profileImagePath);

            if (updated) {
                currentUser.setFullName(fullName);
                currentUser.setPhone(phone);
                currentUser.setProfileImage(profileImagePath);

                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("user_name", fullName);
                editor.putString("user_phone", phone);
                editor.apply();

                progressDialog.dismiss();
                Toast.makeText(this, "تم حفظ التغييرات بنجاح! ✅", Toast.LENGTH_SHORT).show();

                disableEditing();
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "فشل حفظ التغييرات", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "❌ خطأ في حفظ التغييرات: " + e.getMessage());
            Toast.makeText(this, "حدث خطأ أثناء الحفظ", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelEditing() {
        loadUserData();
        disableEditing();
        Toast.makeText(this, "تم إلغاء التعديلات", Toast.LENGTH_SHORT).show();
    }

    private void disableEditing() {
        isEditing = false;
        etFullName.setEnabled(false);
        etPhone.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }

    private void editPreferences() {
        Intent intent = new Intent(this, QuestionsActivity.class);
        intent.putExtra("edit_mode", true);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        if (dbHelper != null) {
            dbHelper.closeDatabase();
        }

        Intent intent = new Intent(this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getBoolean("is_logged_in", false);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (isUserLoggedIn()) {
            if (dbHelper == null) {
                dbHelper = DatabaseHelper.getInstance(this);
            }
            loadUserPreferences();
            loadUserStatistics();
        }
    }



    private void debugUserPreferences() {
        if (dbHelper == null || currentUser == null) return;

        Log.d(TAG, "======= تشخيص التفضيلات =======");
        Log.d(TAG, "البريد الإلكتروني: " + currentUser.getEmail());

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            // 1. فحص إذا كان الجدول موجوداً
            Cursor tableCursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='user_preferences'",
                    null
            );

            boolean tableExists = tableCursor.getCount() > 0;
            tableCursor.close();

            Log.d(TAG, "هل جدول user_preferences موجود؟ " + tableExists);

            if (tableExists) {
                // 2. فحص جميع السجلات في الجدول
                cursor = db.rawQuery("SELECT * FROM user_preferences", null);
                Log.d(TAG, "عدد السجلات الكلي: " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        // التعديل هنا: استخدام getColumnIndexOrThrow
                        int emailIndex = cursor.getColumnIndex("pref_user_email");
                        if (emailIndex != -1) {
                            String prefEmail = cursor.getString(emailIndex);
                            Log.d(TAG, "سجل موجود للمستخدم: " + prefEmail);
                        } else {
                            Log.d(TAG, "⚠️ عمود pref_user_email غير موجود!");
                        }

                        // سجل جميع الأعمدة
                        String[] columns = cursor.getColumnNames();
                        for (String column : columns) {
                            int colIndex = cursor.getColumnIndex(column);
                            if (colIndex != -1) {
                                String value = cursor.getString(colIndex);
                                Log.d(TAG, "  " + column + ": " + (value != null ? value : "NULL"));
                            }
                        }

                    } while (cursor.moveToNext());
                } else {
                    Log.d(TAG, "⚠️ جدول user_preferences فارغ!");
                }

                // 3. فحص سجل المستخدم الحالي
                cursor = db.rawQuery(
                        "SELECT * FROM user_preferences WHERE pref_user_email = ?",
                        new String[]{currentUser.getEmail()}
                );

                Log.d(TAG, "سجلات المستخدم الحالي في DB: " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    Log.d(TAG, "✅ وجد سجل للمستخدم الحالي!");
                    String[] columns = cursor.getColumnNames();
                    for (String column : columns) {
                        int colIndex = cursor.getColumnIndex(column);
                        if (colIndex != -1) {
                            String value = cursor.getString(colIndex);
                            Log.d(TAG, column + " = " + value);
                        }
                    }
                }

            } else {
                Log.d(TAG, "⚠️ جدول user_preferences غير موجود!");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ خطأ في التشخيص: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        Log.d(TAG, "======= نهاية التشخيص =======");
    }


    private void checkSavedPreferences() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = currentUser != null ? currentUser.getEmail() : "";

        if (userEmail.isEmpty()) {
            userEmail = prefs.getString("user_email", "");
        }

        Log.d(TAG, "======= فحص SharedPreferences =======");

        // افحص جميع المفاتيح المحتملة
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // ابحث عن المفاتيح المتعلقة بالتفضيلات
            if (key.contains("pref") || key.contains("discovery") ||
                    key.contains("reason") || key.contains("property")) {
                Log.d(TAG, "🔑 " + key + " = " + value);
            }
        }

        // تحقق من المفاتيح المحددة
        String[] prefKeys = {
                "pref_discovery_source",
                "pref_download_reason",
                "pref_property_types",
                "user_pref_discovery",
                "user_pref_reason",
                "user_pref_types"
        };

        for (String key : prefKeys) {
            String value = prefs.getString(key, "غير موجود");
            Log.d(TAG, key + ": " + value);
        }

        // تحقق بالمفتاح مع البريد الإلكتروني
        if (!userEmail.isEmpty()) {
            String prefKey1 = "pref_discovery_" + userEmail;
            String prefKey2 = "pref_reason_" + userEmail;
            String prefKey3 = "pref_property_types_" + userEmail;

            Log.d(TAG, prefKey1 + ": " + prefs.getString(prefKey1, "غير موجود"));
            Log.d(TAG, prefKey2 + ": " + prefs.getString(prefKey2, "غير موجود"));
            Log.d(TAG, prefKey3 + ": " + prefs.getString(prefKey3, "غير موجود"));
        }

        Log.d(TAG, "======= نهاية الفحص =======");
    }


    private void return_number_my_property() {
        databaseHelper = DatabaseHelper.getInstance(this);


        allProperties = databaseHelper.getAllProperties();
        tvPropertiesCount.setText(allProperties.size() + "");
    }

}