package com.example.maskan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class login extends AppCompatActivity {

    // ✅ أسماء الـ IDs المطابقة للـ XML
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword, tvCreateAccountQuestion;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // التحقق إذا كان المستخدم مسجل دخول مسبقاً
        checkIfLoggedIn();

        // ✅ تهيئة العناصر مع الأسماء الجديدة
        initViews();

        // تعيين الأحداث
        setupListeners();

        // تهيئة DatabaseHelper
        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void initViews() {
        // ✅ استخدام TextInputEditText بدلاً من EditText العادي
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccountQuestion = findViewById(R.id.tvCreateAccountQuestion);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // ✅ كلا العنصرين يمكن النقر عليهما للانتقال للتسجيل
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });

        tvCreateAccountQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: إضافة وظيفة استعادة كلمة المرور
                Toast.makeText(login.this, "وظيفة استعادة كلمة المرور قريباً", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        // جلب البيانات من الحقول
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // التحقق من صحة البيانات
        if (!validateInput(email, password)) {
            return;
        }

        // التحقق من بيانات الدخول في قاعدة البيانات
        boolean isValid = dbHelper.checkUserCredentials(email, password);

        if (isValid) {
            // ✅ تسجيل الدخول ناجح
            Toast.makeText(this, "تم تسجيل الدخول بنجاح!", Toast.LENGTH_SHORT).show();

            // حفظ حالة تسجيل الدخول
            saveLoginState(email);

            // جلب بيانات المستخدم
            User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                saveUserInfo(user);
            }

            // التحقق من إكمال الأسئلة
            checkUserStatus(email);

        } else {
            // ❌ فشل تسجيل الدخول
            Toast.makeText(this, "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String email, String password) {

        // التحقق من البريد الإلكتروني
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("الرجاء إدخال البريد الإلكتروني");
            etEmail.requestFocus();
            return false;
        }

        // التحقق من كلمة المرور
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("الرجاء إدخال كلمة المرور");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("كلمة المرور يجب أن تكون 6 أحرف على الأقل");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void saveLoginState(String email) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_email", email);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    private void saveUserInfo(User user) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_name", user.getFullName());
        editor.putString("user_phone", user.getPhone());
        if (user.getProfileImage() != null) {
            editor.putString("user_profile_image", user.getProfileImage());
        }
        editor.apply();
    }

    private void checkUserStatus(String email) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean questionsCompleted = prefs.getBoolean("questions_completed_" + email, false);

        if (questionsCompleted) {
            // الانتقال إلى الشاشة الرئيسية
            Intent intent = new Intent(login.this, MainActivity.class);
            startActivity(intent);
        } else {
            // الانتقال إلى شاشة الأسئلة
            Intent intent = new Intent(login.this, QuestionsActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void checkIfLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            String email = prefs.getString("user_email", "");
            boolean questionsCompleted = prefs.getBoolean("questions_completed_" + email, false);

            if (questionsCompleted) {
                Intent intent = new Intent(login.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(login.this, QuestionsActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void goToRegister() {
        Intent intent = new Intent(login.this, Register.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // تنظيف الموارد
    }
}