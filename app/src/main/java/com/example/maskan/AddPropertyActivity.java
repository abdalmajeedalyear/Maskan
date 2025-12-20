package com.example.maskan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AddPropertyActivity extends AppCompatActivity {

    private EditText etPropertyType, etOfferType, etPrice, etAddress;
    private EditText etBedrooms, etBathrooms, etArea, etDescription;
    private EditText etContactName, etContactPhone;
    private Button btnPublish, btnAddImages, btnSelectLocation;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);

        setupClickListeners();

    }

    private void initializeViews() {
        // العثور على جميع العناصر
        btnBack = findViewById(R.id.btnBack);
        etPropertyType = findViewById(R.id.etPropertyType);
        etOfferType = findViewById(R.id.etOfferType);
        etPrice = findViewById(R.id.etPrice);
        etAddress = findViewById(R.id.etAddress);
        etBedrooms = findViewById(R.id.etBedrooms);
        etBathrooms = findViewById(R.id.etBathrooms);
        etArea = findViewById(R.id.etArea);
        etDescription = findViewById(R.id.etDescription);
        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnPublish = findViewById(R.id.btnPublish);
        btnAddImages = findViewById(R.id.btnAddImages);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
    }

    private void setupClickListeners() {
        // زر العودة
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // زر إضافة الصور
        btnAddImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // سيتم تنفيذ اختيار الصور لاحقاً
                Toast.makeText(AddPropertyActivity.this, "إضافة صور", Toast.LENGTH_SHORT).show();
            }
        });

        // زر تحديد الموقع
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // سيتم تنفيذ الخريطة لاحقاً
                Toast.makeText(AddPropertyActivity.this, "تحديد الموقع", Toast.LENGTH_SHORT).show();
            }
        });

        // زر النشر
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPropertyToDatabase();
            }
        });

        // جعل الحقول القابلة للنقر تفتح قوائم اختيار
        setupSelectableFields();
    }

    private void setupSelectableFields() {
        // نوع العقار
        etPropertyType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPropertyTypeDialog();
            }
        });

        // نوع العرض
        etOfferType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOfferTypeDialog();
            }
        });
    }

    private void showPropertyTypeDialog() {
        String[] propertyTypes = {"فيلا", "شقة", "أرض", "منزل", "مكتب", "محل تجاري"};
        // يمكنك استخدام AlertDialog أو BottomSheetDialog هنا
        Toast.makeText(this, "اختر نوع العقار", Toast.LENGTH_SHORT).show();
    }

    private void showOfferTypeDialog() {
        String[] offerTypes = {"بيع", "إيجار"};
        // يمكنك استخدام AlertDialog أو BottomSheetDialog هنا
        Toast.makeText(this, "اختر نوع العرض", Toast.LENGTH_SHORT).show();
    }

    private void addPropertyToDatabase() {
        // التحقق من الحقول المطلوبة
        if (!validateForm()) {
            return;
        }

        try {
            // الحصول على القيم من الحقول
            String title = etPropertyType.getText().toString() + " - " + etAddress.getText().toString();
            String description = etDescription.getText().toString();
            double price = Double.parseDouble(etPrice.getText().toString());
            String type = etPropertyType.getText().toString();
            String offerType = etOfferType.getText().toString();
            String address = etAddress.getText().toString();
            int bedrooms = etBedrooms.getText().toString().isEmpty() ? 0 : Integer.parseInt(etBedrooms.getText().toString());
            int bathrooms = etBathrooms.getText().toString().isEmpty() ? 0 : Integer.parseInt(etBathrooms.getText().toString());
            double area = etArea.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(etArea.getText().toString());
            String contactName = etContactName.getText().toString();
            String contactPhone = etContactPhone.getText().toString();

            // ✅ إضافة العقار إلى قاعدة البيانات باستخدام الدالة المعدلة
            long id = databaseHelper.addProperty(
                    title,
                    description,
                    price,
                    type,
                    offerType,
                    address,
                    bedrooms,
                    bathrooms,
                    area,
                    contactName,
                    contactPhone,
                    new ArrayList<>()  // ✅ قائمة صور فارغة (لأن هذا الملف لا يدعم إضافة الصور بعد)
            );

            if (id != -1) {
                Toast.makeText(this, "تم إضافة العقار بنجاح!", Toast.LENGTH_SHORT).show();
                clearForm();
                // العودة للصفحة الرئيسية
                Intent intent = new Intent(AddPropertyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "خطأ في إضافة العقار", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "يرجى إدخال أرقام صحيحة في الحقول الرقمية", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm() {
        if (etPropertyType.getText().toString().trim().isEmpty()) {
            etPropertyType.setError("يرجى اختيار نوع العقار");
            return false;
        }
        if (etOfferType.getText().toString().trim().isEmpty()) {
            etOfferType.setError("يرجى اختيار نوع العرض");
            return false;
        }
        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("يرجى إدخال السعر");
            return false;
        }
        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("يرجى إدخال العنوان");
            return false;
        }
        if (etContactName.getText().toString().trim().isEmpty()) {
            etContactName.setError("يرجى إدخال اسم المعلن");
            return false;
        }
        if (etContactPhone.getText().toString().trim().isEmpty()) {
            etContactPhone.setError("يرجى إدخال رقم الهاتف");
            return false;
        }
        return true;
    }

    private void clearForm() {
        etPropertyType.setText("");
        etOfferType.setText("");
        etPrice.setText("");
        etAddress.setText("");
        etBedrooms.setText("");
        etBathrooms.setText("");
        etArea.setText("");
        etDescription.setText("");
        etContactName.setText("");
        etContactPhone.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }






}