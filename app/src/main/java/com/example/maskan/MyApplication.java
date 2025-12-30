package com.example.maskan;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // تهيئة MessageHelper
        // تهيئة MessageHelper باستخدام Application Context
        MessageHelper.init(this);
    }
}