package com.example.maskan;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AddPropertyActivity extends AppCompatActivity {

    private EditText etPropertyType, etOfferType, etPrice, etAddress;
    private EditText etBedrooms, etBathrooms, etArea, etDescription;
    private EditText etContactName, etContactPhone;
    private Button btnPublish, btnAddImages, btnSelectLocation;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;
    private MessageHelper messageHelper;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);

        // تهيئة MessageHelper
        initMessageHelper();

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

    private void initMessageHelper() {
        // استخدم Application Context بدلاً من Activity Context
        if (!isMessageHelperInitialized()) {
            MessageHelper.init(getApplicationContext());
        }
        messageHelper = MessageHelper.getInstance();
    }

    private boolean isMessageHelperInitialized() {
        try {
            MessageHelper.getInstance();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
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
                Toast.makeText(AddPropertyActivity.this, "إضافة صور", Toast.LENGTH_SHORT).show();
            }
        });

        // زر تحديد الموقع
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddPropertyActivity.this, "تحديد الموقع", Toast.LENGTH_SHORT).show();
            }
        });

        // زر النشر - تم التعديل هنا
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // استدعاء الدالة المحسنة للنشر
                publishPropertyWithMessage();
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
        Toast.makeText(this, "اختر نوع العقار", Toast.LENGTH_SHORT).show();
    }

    private void showOfferTypeDialog() {
        String[] offerTypes = {"بيع", "إيجار"};
        Toast.makeText(this, "اختر نوع العرض", Toast.LENGTH_SHORT).show();
    }

    /**
     * دالة النشر المحسنة مع رسائل متكاملة
     */
    private void publishPropertyWithMessage() {
        // التحقق من الحقول المطلوبة
        if (!validateForm()) {
            return;
        }

        // عرض رسالة تأكيد قبل النشر
        showPublishConfirmation();  // ⬅️ هذه الدالة يجب أن تكون موجودة
    }

    /**
     * عرض رسالة تأكيد النشر
     */
    private void showPublishConfirmation() {
        String propertyType = etPropertyType.getText().toString();
        String price = etPrice.getText().toString();

        String confirmationMessage = String.format(
                "هل أنت متأكد من نشر هذا العقار؟\n\n" +
                        "📌 نوع العقار: %s\n" +
                        "💰 السعر: %s ريال\n\n" +
                        "سيصبح العقار مرئياً لجميع المستخدمين.",
                propertyType, price
        );

        messageHelper.showDialog("تأكيد النشر",
                confirmationMessage,
                MessageHelper.TYPE_WARNING,
                "نعم، أنشر العقار",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // المتابعة بعملية النشر
                        proceedWithPublishing();
                    }
                });
    }

    /**
     * متابعة عملية النشر بعد التأكيد
     */
    private void proceedWithPublishing() {
        // عرض رسالة تحميل
        messageHelper.showLoading("جاري نشر العقار...");

        // محاكاة عملية النشر (يمكن استبدالها بالكود الحقيقي)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // إضافة العقار إلى قاعدة البيانات
                    boolean success = addPropertyToDatabase();

                    if (success) {
                        // عرض رسالة النجاح
                        showPublishSuccess();
                    } else {
                        messageHelper.hideLoading();
                        messageHelper.showError("حدث خطأ في حفظ العقار");
                    }
                } catch (Exception e) {
                    messageHelper.hideLoading();
                    messageHelper.showError("خطأ: " + e.getMessage());
                }
            }
        }, 2000); // محاكاة تأخير 2 ثانية
    }

    /**
     * عرض رسالة النجاح بعد النشر
     */
    private void showPublishSuccess() {
        String propertyType = etPropertyType.getText().toString();
        String price = etPrice.getText().toString();
        String address = etAddress.getText().toString();

        String successMessage = String.format(
                "🎉 مبروك! تم نشر عقارك بنجاح\n\n" +
                        "🏠 %s في %s\n" +
                        "💰 بسعر %s ريال\n\n" +
                        "✅ سيظهر في نتائج البحث خلال دقائق\n" +
                        "📞 سيتصل بك المهتمون على الرقم: %s\n" +
                        "⚙️ يمكنك تعديل العقار من قائمة عقاراتك",
                propertyType, address, price, etContactPhone.getText().toString()
        );

        messageHelper.hideLoading();
        messageHelper.showDialog("تم النشر بنجاح!",
                successMessage,
                MessageHelper.TYPE_SUCCESS,
                "عرض العقار",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // تنظيف الحقول والرجوع
                        clearForm();

                        // عرض رسالة تأكيد إضافية
                        messageHelper.showToast("تم حفظ العقار في قائمتك",
                                MessageHelper.TYPE_SUCCESS);

                        // العودة بعد ثانيتين
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                });
    }

    /**
     * إضافة العقار إلى قاعدة البيانات
     */
    private boolean addPropertyToDatabase() {
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

            // إضافة العقار إلى قاعدة البيانات
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
                    new ArrayList<>()  // قائمة صور فارغة
            );

            return id != -1;

        } catch (NumberFormatException e) {
            messageHelper.showError("يرجى إدخال أرقام صحيحة في الحقول الرقمية");
            return false;
        } catch (Exception e) {
            messageHelper.showError("حدث خطأ: " + e.getMessage());
            return false;
        }
    }

    /**
     * التحقق من صحة النموذج
     */
    private boolean validateForm() {
        if (etPropertyType.getText().toString().trim().isEmpty()) {
            etPropertyType.setError("يرجى اختيار نوع العقار");
            messageHelper.showWarning("يرجى اختيار نوع العقار");
            return false;
        }
        if (etOfferType.getText().toString().trim().isEmpty()) {
            etOfferType.setError("يرجى اختيار نوع العرض");
            messageHelper.showWarning("يرجى اختيار نوع العرض");
            return false;
        }
        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("يرجى إدخال السعر");
            messageHelper.showWarning("يرجى إدخال السعر");
            return false;
        }
        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("يرجى إدخال العنوان");
            messageHelper.showWarning("يرجى إدخال العنوان");
            return false;
        }
        if (etContactName.getText().toString().trim().isEmpty()) {
            etContactName.setError("يرجى إدخال اسم المعلن");
            messageHelper.showWarning("يرجى إدخال اسم المعلن");
            return false;
        }
        if (etContactPhone.getText().toString().trim().isEmpty()) {
            etContactPhone.setError("يرجى إدخال رقم الهاتف");
            messageHelper.showWarning("يرجى إدخال رقم الهاتف");
            return false;
        }

        // التحقق من صحة رقم الهاتف
        String phone = etContactPhone.getText().toString().trim();
        if (!isValidPhoneNumber(phone)) {
            etContactPhone.setError("رقم الهاتف غير صالح");
            messageHelper.showWarning("يرجى إدخال رقم هاتف صحيح (10 أرقام)");
            return false;
        }

        return true;
    }

    /**
     * التحقق من صحة رقم الهاتف
     */
    private boolean isValidPhoneNumber(String phone) {
        // تحقق أن الرقم يحتوي على 10 أرقام ويبدأ بـ 05
        return phone.matches("05[0-9]{8}");
    }

    /**
     * تنظيف الحقول بعد النشر
     */
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
        if (messageHelper != null) {
            messageHelper.cleanup();
        }
    }
}