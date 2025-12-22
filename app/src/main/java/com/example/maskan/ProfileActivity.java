package com.example.maskan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // Tags للـ Log
    private static final String TAG = "PROFILE_ACTIVITY";

    // العناصر UI - تم تغيير CircleImageView إلى ImageView
    private ImageView ivProfile; // تم التغيير
    private TextInputEditText etFullName, etEmail, etPhone;
    private TextView tvPropertyTypes, tvDownloadReason, tvDiscoverySource;
    private TextView tvFavoritesCount, tvPropertiesCount, tvMemberSince, tvLastActive;
    private Button btnChangePhoto, btnSave, btnCancel, btnEditPreferences, btnLogout;

    // المتغيرات
    private DatabaseHelper dbHelper;
    private User currentUser;
    private String profileImagePath = "";
    private boolean isEditing = false;

    // ثوابت
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final String[] DISCOVERY_SOURCES = {
            "صديق", "وسائل التواصل الاجتماعي", "بحث على الإنترنت", "إعلان", "أخرى"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Log.d(TAG, "ProfileActivity بدأت!");

        // التحقق من تسجيل الدخول
        if (!isUserLoggedIn()) {
            goToLogin();
            return;
        }

        // تهيئة العناصر
        initViews();

        // تعيين الأحداث
        setupListeners();

        // تهيئة DatabaseHelper
        dbHelper = DatabaseHelper.getInstance(this);

        // تحميل بيانات المستخدم
        loadUserData();

        // تحميل التفضيلات
        loadUserPreferences();

        // تحميل الإحصائيات
        loadUserStatistics();
    }

    private void initViews() {
        ivProfile = findViewById(R.id.ivProfile); // ImageView بدلاً من CircleImageView
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        tvPropertyTypes = findViewById(R.id.tvPropertyTypes);
        tvDownloadReason = findViewById(R.id.tvDownloadReason);
        tvDiscoverySource = findViewById(R.id.tvDiscoverySource);

        tvFavoritesCount = findViewById(R.id.tvFavoritesCount);
        tvPropertiesCount = findViewById(R.id.tvPropertiesCount);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvLastActive = findViewById(R.id.tvLastActive);

        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnEditPreferences = findViewById(R.id.btnEditPreferences);
        btnLogout = findViewById(R.id.btnLogout);
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

        // تفعيل/تعطيل التعديل عند النقر على الحقول
        etFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing();
            }
        });

        etPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing();
            }
        });
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");

        Log.d(TAG, "جلب بيانات المستخدم: " + userEmail);

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "خطأ في بيانات المستخدم", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        // جلب بيانات المستخدم من قاعدة البيانات
        currentUser = dbHelper.getUserByEmail(userEmail);

        if (currentUser != null) {
            Log.d(TAG, "✅ تم جلب بيانات المستخدم: " + currentUser.toString());

            // تعيين البيانات في الحقول
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone());

            // تحميل صورة الملف الشخصي إذا كانت موجودة
            if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                profileImagePath = currentUser.getProfileImage();
                loadProfileImage();
            }

        } else {
            Log.e(TAG, "❌ المستخدم غير موجود في قاعدة البيانات!");
            Toast.makeText(this, "خطأ في بيانات المستخدم", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserPreferences() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");

        Log.d(TAG, "جلب تفضيلات المستخدم: " + userEmail);

        if (userEmail.isEmpty()) return;

        // جلب التفضيلات من قاعدة البيانات
        Cursor cursor = dbHelper.getUserPreferences(userEmail);

        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, "✅ تم جلب تفضيلات المستخدم");

            try {
                // جلب البيانات من Cursor
                String discoverySource = cursor.getString(cursor.getColumnIndexOrThrow("pref_discovery_source"));
                String downloadReason = cursor.getString(cursor.getColumnIndexOrThrow("pref_download_reason"));
                String propertyTypes = cursor.getString(cursor.getColumnIndexOrThrow("pref_property_types"));

                // عرض البيانات
                displayPreferences(discoverySource, downloadReason, propertyTypes);

            } catch (Exception e) {
                Log.e(TAG, "خطأ في معالجة التفضيلات: " + e.getMessage());

                // محاولة جلب البيانات من SharedPreferences
                loadPreferencesFromSharedPrefs(userEmail);
            }

            cursor.close();

        } else {
            Log.d(TAG, "⚠️ لا توجد تفضيلات في قاعدة البيانات");

            // جلب البيانات من SharedPreferences
            loadPreferencesFromSharedPrefs(userEmail);
        }
    }

    private void loadPreferencesFromSharedPrefs(String userEmail) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String discoverySource = prefs.getString("pref_discovery_" + userEmail, "غير محدد");
        String downloadReason = prefs.getString("pref_reason_" + userEmail, "غير محدد");
        String propertyTypes = prefs.getString("pref_property_types_" + userEmail, "غير محدد");

        Log.d(TAG, "جلب التفضيلات من SharedPreferences");
        displayPreferences(discoverySource, downloadReason, propertyTypes);
    }

    private void displayPreferences(String discoverySource, String downloadReason, String propertyTypes) {
        // تحويل discoverySource إلى نص عربي
        String discoveryText = getDiscoverySourceText(discoverySource);

        // تحويل propertyTypes إلى نص عربي
        String propertyTypesText = getPropertyTypesText(propertyTypes);

        // عرض البيانات
        tvDiscoverySource.setText(discoveryText);
        tvDownloadReason.setText(downloadReason.isEmpty() ? "غير محدد" : downloadReason);
        tvPropertyTypes.setText(propertyTypesText);
    }

    private String getDiscoverySourceText(String source) {
        switch (source) {
            case "friend": return "صديق";
            case "social_media": return "وسائل التواصل الاجتماعي";
            case "search": return "بحث على الإنترنت";
            case "ads": return "إعلان";
            case "other": return "أخرى";
            default: return "غير محدد";
        }
    }

    private String getPropertyTypesText(String types) {
        if (types == null || types.isEmpty()) return "غير محدد";

        switch (types) {
            case "sale": return "عقارات للبيع";
            case "rent": return "عقارات للإيجار";
            case "sale,rent": return "البيع والإيجار";
            default: return types;
        }
    }

    private void loadUserStatistics() {
        if (currentUser == null) return;

        // 1. عدد المفضلات
        int favoritesCount = dbHelper.getFavoriteProperties().size();
        tvFavoritesCount.setText(String.valueOf(favoritesCount));

        // 2. عدد العقارات المضافة (سنتجاهلها الآن)
        tvPropertiesCount.setText("0");

        // 3. تاريخ التسجيل
        if (currentUser.getCreatedAt() != null && !currentUser.getCreatedAt().isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(currentUser.getCreatedAt());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                String year = outputFormat.format(date);
                tvMemberSince.setText(year);
            } catch (Exception e) {
                tvMemberSince.setText("2024");
            }
        } else {
            tvMemberSince.setText("2024");
        }

        // 4. آخر نشاط (اليوم)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        String today = dateFormat.format(new Date());
        tvLastActive.setText(today);
    }

    private void loadProfileImage() {
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            try {
                File imgFile = new File(profileImagePath);
                if (imgFile.exists()) {
                    // طريقة بديلة بدون Picasso
                    ivProfile.setImageBitmap(BitmapFactory.decodeFile(profileImagePath));
                    Log.d(TAG, "✅ تم تحميل صورة الملف الشخصي بدون Picasso");
                }
            } catch (Exception e) {
                Log.e(TAG, "خطأ في تحميل الصورة: " + e.getMessage());
                // استخدام صورة افتراضية من النظام
                ivProfile.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            // استخدام صورة افتراضية من النظام
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
                    // تحويل URI إلى مسار الملف
                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        profileImagePath = cursor.getString(columnIndex);
                        cursor.close();

                        // عرض الصورة المحددة بدون Picasso
                        ivProfile.setImageBitmap(BitmapFactory.decodeFile(profileImagePath));

                        enableEditing();
                        Log.d(TAG, "✅ تم اختيار صورة جديدة: " + profileImagePath);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "خطأ في اختيار الصورة: " + e.getMessage());
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
            Log.d(TAG, "تم تفعيل وضع التعديل");
        }
    }

    private void saveProfileChanges() {
        Log.d(TAG, "====== بدء حفظ التغييرات ======");

        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // التحقق من البيانات
        if (fullName.isEmpty()) {
            etFullName.setError("الرجاء إدخال الاسم الكامل");
            etFullName.requestFocus();
            return;
        }

        // عرض ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري حفظ التغييرات...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // تحديث بيانات المستخدم في قاعدة البيانات
            boolean updated = dbHelper.updateUser(currentUser.getEmail(), fullName, phone, profileImagePath);

            if (updated) {
                Log.d(TAG, "✅ تم تحديث بيانات المستخدم في قاعدة البيانات");

                // تحديث بيانات المستخدم المحلية
                currentUser.setFullName(fullName);
                currentUser.setPhone(phone);
                currentUser.setProfileImage(profileImagePath);

                // تحديث SharedPreferences
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("user_name", fullName);
                editor.putString("user_phone", phone);
                if (profileImagePath != null && !profileImagePath.isEmpty()) {
                    editor.putString("user_profile_image", profileImagePath);
                }
                editor.apply();

                progressDialog.dismiss();
                Toast.makeText(this, "تم حفظ التغييرات بنجاح! ✅", Toast.LENGTH_SHORT).show();

                // إعادة تحميل البيانات
                loadUserData();

                // تعطيل وضع التعديل
                disableEditing();

            } else {
                progressDialog.dismiss();
                Log.e(TAG, "❌ فشل تحديث بيانات المستخدم");
                Toast.makeText(this, "فشل حفظ التغييرات", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "خطأ في حفظ التغييرات: " + e.getMessage());
            Toast.makeText(this, "حدث خطأ أثناء الحفظ", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "====== انتهاء حفظ التغييرات ======");
    }

    private void cancelEditing() {
        // إعادة تحميل البيانات الأصلية
        loadUserData();

        // تعطيل وضع التعديل
        disableEditing();

        Toast.makeText(this, "تم إلغاء التعديلات", Toast.LENGTH_SHORT).show();
    }

    private void disableEditing() {
        isEditing = false;
        etFullName.setEnabled(false);
        etPhone.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        Log.d(TAG, "تم تعطيل وضع التعديل");
    }

    private void editPreferences() {
        // الانتقال إلى QuestionsActivity لتعديل التفضيلات
        Intent intent = new Intent(this, QuestionsActivity.class);
        intent.putExtra("edit_mode", true);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        Log.d(TAG, "جاري تسجيل الخروج...");

        // مسح بيانات الجلسة
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // إغلاق قاعدة البيانات
        if (dbHelper != null) {
            dbHelper.closeDatabase();
        }

        // الانتقال إلى شاشة تسجيل الدخول
        Intent intent = new Intent(this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ تم تسجيل الخروج");
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
        Log.d(TAG, "ProfileActivity resumed");

        // إعادة تحميل البيانات عند العودة
        if (isUserLoggedIn()) {
            loadUserPreferences();
            loadUserStatistics();
        }
    }
}