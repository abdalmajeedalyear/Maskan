package com.example.maskan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 ثانية

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // جعل الشاشة كاملة بدون شريط الحالة
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // إخفاء ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // تهيئة العناصر
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.appName);
        TextView slogan = findViewById(R.id.slogan);

        // تحميل الحركات
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // تطبيق الحركات
        logo.startAnimation(fadeIn);
        appName.startAnimation(slideUp);

        // تأخير بسيط ثم حركة الشعار
        new Handler().postDelayed(() -> {
            logo.startAnimation(bounce);
            slogan.startAnimation(fadeIn);
        }, 500);

        // الانتقال إلى الشاشة الرئيسية بعد المدة المحددة
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

            // ✅ إضافة حركة انتقال سلسة
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        }, SPLASH_DURATION);
    }

    @Override
    public void onBackPressed() {
        // ❌ منع العودة أثناء شاشة التمهيد
        super.onBackPressed();
    }
}