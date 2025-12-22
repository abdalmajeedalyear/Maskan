package com.example.maskan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class Register extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin, tvLoginQuestion;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2); // ⚠️ تأكد من الاسم

        checkIfLoggedIn();
        initViews();
        setupListeners();
        dbHelper = DatabaseHelper.getInstance(this);
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvLoginQuestion = findViewById(R.id.tvLoginQuestion);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        tvLoginQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(fullName, email, phone, password, confirmPassword)) {
            return;
        }

        if (dbHelper.isEmailExists(email)) {
            etEmail.setError("البريد الإلكتروني مستخدم مسبقاً");
            etEmail.requestFocus();
            return;
        }

        long userId = dbHelper.addUser(fullName, email, phone, password);

        if (userId > 0) {
            Toast.makeText(this, "تم إنشاء الحساب بنجاح! ✅", Toast.LENGTH_SHORT).show();
            saveLoginState(email, fullName);
            goToQuestionsActivity();
        } else {
            Toast.makeText(this, "فشل إنشاء الحساب. حاول مرة أخرى", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String fullName, String email, String phone,
                                  String password, String confirmPassword) {

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("الرجاء إدخال الاسم الكامل");
            etFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 3) {
            etFullName.setError("الاسم يجب أن يكون 3 أحرف على الأقل");
            etFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("الرجاء إدخال البريد الإلكتروني");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("البريد الإلكتروني غير صالح");
            etEmail.requestFocus();
            return false;
        }

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

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("كلمات المرور غير متطابقة");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void saveLoginState(String email, String fullName) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_email", email);
        editor.putString("user_name", fullName);
        editor.putBoolean("is_logged_in", true);
        editor.putBoolean("questions_completed_" + email, false);
        editor.apply();
    }

    private void checkIfLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            String email = prefs.getString("user_email", "");
            boolean questionsCompleted = prefs.getBoolean("questions_completed_" + email, false);

            if (questionsCompleted) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, QuestionsActivity.class));
            }
            finish();
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, login.class));
        finish();
    }

    private void goToQuestionsActivity() {
        startActivity(new Intent(this, QuestionsActivity.class));
        finish();
    }
}