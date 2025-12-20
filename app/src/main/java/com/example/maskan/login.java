package com.example.maskan;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;

    private TextView tvRegister;
    public  static final String FILE_NAME="users";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);//لقرائة البيانات
        SharedPreferences.Editor edit=sp.edit();//لتعديل البيانات

        // ربط العناصر مع الكود
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);


        // إضافة المستمعين للأزرار
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.putString("name","badalmajeed");
                edit.apply();
                String name=sp.getString("name","لم يتم ايجاد اسم المستخدم");
                Toast.makeText(login.this,name.toString(),Toast.LENGTH_LONG);
                Intent intent = new Intent(login.this, MainActivity.class);
               // startActivity(intent);
                //loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // الانتقال لواجهة التسجيل
                Intent intentv = new Intent(login.this,activity_register.class);
                startActivity(intentv);
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        // التحقق من الحقول
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
            return;
        }

        // هنا سنضيف لاحقاً التحقق من قاعدة البيانات
        Toast.makeText(this, "جاري تسجيل الدخول...", Toast.LENGTH_SHORT).show();
    }




}