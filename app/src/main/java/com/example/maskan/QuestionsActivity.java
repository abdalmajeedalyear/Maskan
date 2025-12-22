package com.example.maskan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

public class QuestionsActivity extends AppCompatActivity {

    // العناصر UI
    private RadioGroup rgDiscoverySource;
    private TextInputEditText etDownloadReason, etAdditionalInfo;
    private MaterialCheckBox cbForSale, cbForRent;
    private Button btnSelectLocation, btnSubmitQuestions ;
    private TextView tvSelectedLocation;

    // المتغيرات
    private DatabaseHelper dbHelper;
    private double selectedLatitude = 0;
    private double selectedLongitude = 0;
    private String selectedAddress = "";

    // ثوابت
    private static final int LOCATION_PICK_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        // في initViews():
        Button btnSkipForNow = findViewById(R.id.btnSkipForNow);

// في setupListeners():
        btnSkipForNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipQuestionsForNow();
            }
        });

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
    }

    private void initViews() {
        rgDiscoverySource = findViewById(R.id.rgDiscoverySource);
        etDownloadReason = findViewById(R.id.etDownloadReason);
        etAdditionalInfo = findViewById(R.id.etAdditionalInfo);
        cbForSale = findViewById(R.id.cbForSale);
        cbForRent = findViewById(R.id.cbForRent);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnSubmitQuestions = findViewById(R.id.btnSubmitQuestions);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);

        // تحديد اختيار افتراضي
        rgDiscoverySource.check(R.id.rbFriend);
    }

    private void setupListeners() {
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationPicker();
            }
        });

        btnSubmitQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitQuestions();
            }
        });
    }

    private void openLocationPicker() {
        /* TODO: سيتم إضافة خرائط Google لاحقاً*/
        Toast.makeText(this, "ميزة الخريطة قريباً...", Toast.LENGTH_SHORT).show();

        // مؤقتاً: حفظ موقع افتراضي
        selectedLatitude = 24.7136;  // الرياض
        selectedLongitude = 46.6753;
        selectedAddress = "الرياض، المملكة العربية السعودية";
        tvSelectedLocation.setText(selectedAddress);
    }

    private void submitQuestions() {
        Log.d("QUESTIONS_DEBUG", "بدء حفظ الأسئلة...");

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "خطأ في بيانات المستخدم", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        // جمع البيانات
        String discoverySource = getSelectedDiscoverySource();
        String downloadReason = etDownloadReason.getText().toString().trim();
        String additionalInfo = etAdditionalInfo.getText().toString().trim();

        // التحقق من البيانات المطلوبة
        if (downloadReason.isEmpty()) {
            etDownloadReason.setError("الرجاء إدخال سبب تنزيل التطبيق");
            etDownloadReason.requestFocus();
            return;
        }

        boolean wantsSale = cbForSale.isChecked();
        boolean wantsRent = cbForRent.isChecked();
        String propertyTypes = getPropertyTypesString(wantsSale, wantsRent);

        // ✅ محاولة الحفظ في قاعدة البيانات
        boolean savedToDb = false;
        try {
            savedToDb = dbHelper.saveUserPreferences(
                    userEmail,
                    discoverySource,
                    downloadReason,
                    propertyTypes,
                    selectedLatitude,
                    selectedLongitude,
                    additionalInfo
            );
            Log.d("QUESTIONS_DEBUG", "نتيجة الحفظ في DB: " + savedToDb);
        } catch (Exception e) {
            Log.e("QUESTIONS_DEBUG", "خطأ في قاعدة البيانات: " + e.getMessage());
        }

        // ✅ الحفظ في SharedPreferences دائماً (كبديل/تكملة)
        saveToSharedPreferences(userEmail, discoverySource, downloadReason,
                propertyTypes, additionalInfo);

        // ✅ تحديث حالة إكمال الأسئلة
        markQuestionsAsCompleted(userEmail);

        // ✅ عرض رسالة النجاح
        String message = savedToDb ?
                "تم حفظ معلوماتك بنجاح! ✅" :
                "تم حفظ معلوماتك (بديل)! ✅";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // ✅ الانتقال إلى MainActivity مع العلم أن الأسئلة مكتملة
        goToMainActivity();
    }

    private String getSelectedDiscoverySource() {
        int selectedId = rgDiscoverySource.getCheckedRadioButtonId();

        if (selectedId == R.id.rbFriend) return "friend";
        if (selectedId == R.id.rbSocialMedia) return "social_media";
        if (selectedId == R.id.rbSearch) return "search";
        if (selectedId == R.id.rbAds) return "ads";
        if (selectedId == R.id.rbOtherSource) return "other";

        return "friend"; // افتراضي
    }

    private String getPropertyTypesString(boolean wantsSale, boolean wantsRent) {
        if (wantsSale && wantsRent) {
            return "sale,rent";
        } else if (wantsSale) {
            return "sale";
        } else if (wantsRent) {
            return "rent";
        }
        return "sale,rent"; // افتراضي
    }

    private void markQuestionsAsCompleted(String email) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("questions_completed_" + email, true);

        // حفظ الموقع أيضاً للاستخدام السريع
        if (selectedLatitude != 0 && selectedLongitude != 0) {
            editor.putFloat("user_latitude", (float) selectedLatitude);
            editor.putFloat("user_longitude", (float) selectedLongitude);
        }

        editor.apply();
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

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // تنظيف الموارد إذا لزم الأمر
    }


    private void saveToSharedPreferences(String userEmail, String discoverySource,
                                         String downloadReason, String propertyTypes,
                                         String additionalInfo) {
        try {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("pref_discovery_" + userEmail, discoverySource);
            editor.putString("pref_reason_" + userEmail, downloadReason);
            editor.putString("pref_property_types_" + userEmail, propertyTypes);
            editor.putString("pref_additional_" + userEmail, additionalInfo);
            editor.putBoolean("questions_completed_" + userEmail, true);

            editor.apply();

            Log.d("QUESTIONS_DEBUG", "تم الحفظ في SharedPreferences بنجاح!");
            Toast.makeText(this, "تم الحفظ بنجاح (بديل)! ✅", Toast.LENGTH_SHORT).show();

            // الانتقال إلى الشاشة الرئيسية
            goToMainActivity();

        } catch (Exception e) {
            Log.e("QUESTIONS_DEBUG", "خطأ في SharedPreferences: " + e.getMessage());
        }
    }


    // دالة التخطي:
    private void skipQuestionsForNow() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "خطأ في بيانات المستخدم", Toast.LENGTH_SHORT).show();
            return;
        }

        // حفظ حالة إكمال الأسئلة (بدون معلومات)
        markQuestionsAsCompleted(userEmail);

        Toast.makeText(this, "تم التخطي بنجاح! ✅", Toast.LENGTH_SHORT).show();
        goToMainActivity();
    }


}