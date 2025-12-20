package com.example.maskan;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class add_property extends AppCompatActivity {

    private EditText etPropertyType, etOfferType, etPrice, etAddress;
    private EditText etBedrooms, etBathrooms, etArea, etDescription;
    private EditText etContactName, etContactPhone;
    private Button btnPublish, btnAddImages, btnSelectLocation;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;
    private List<Uri> selectedImages = new ArrayList<>();
    private static final int PICK_IMAGES_REQUEST = 1001;

    // ✅ إضافة متغيرات الموقع
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private static final int MAP_REQUEST_CODE = 1003;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        Log.d("AddProperty", "=== ACTIVITY STARTED ===");

        try {
            initializeViews();
            databaseHelper = DatabaseHelper.getInstance(this);
            setupClickListeners();
            Log.d("AddProperty", "✅ Activity initialized successfully");
        } catch (Exception e) {
            Log.e("AddProperty", "❌ Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "خطأ في تحميل الصفحة", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupBottomNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initializeViews() {
        Log.d("AddProperty", "🔄 Initializing views...");

        try {
            // العناصر الأساسية
            btnBack = findViewById(R.id.btnBack);
            etPropertyType = findViewById(R.id.etPropertyType);
            etOfferType = findViewById(R.id.etOfferType);
            etPrice = findViewById(R.id.etPrice);
            etAddress = findViewById(R.id.etAddress);
            btnPublish = findViewById(R.id.btnPublish);
            btnAddImages = findViewById(R.id.btnAddImages);

            // العناصر الاختيارية
            etBedrooms = findViewById(R.id.etBedrooms);
            etBathrooms = findViewById(R.id.etBathrooms);
            etArea = findViewById(R.id.etArea);
            etDescription = findViewById(R.id.etDescription);
            etContactName = findViewById(R.id.etContactName);
            etContactPhone = findViewById(R.id.etContactPhone);
            btnSelectLocation = findViewById(R.id.btnSelectLocation);

            // تسجيل حالة جميع العناصر
            Log.d("AddProperty", "📋 Views status:");
            Log.d("AddProperty", "   - btnBack: " + (btnBack != null ? "FOUND" : "NULL"));
            Log.d("AddProperty", "   - btnAddImages: " + (btnAddImages != null ? "FOUND" : "NULL"));
            Log.d("AddProperty", "   - btnPublish: " + (btnPublish != null ? "FOUND" : "NULL"));
            Log.d("AddProperty", "   - btnSelectLocation: " + (btnSelectLocation != null ? "FOUND" : "NULL"));
            Log.d("AddProperty", "✅ All views initialized");

        } catch (Exception e) {
            Log.e("AddProperty", "❌ Error in initializeViews: " + e.getMessage());
            Toast.makeText(this, "بعض العناصر غير موجودة ولكن يمكنك الاستمرار", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        Log.d("AddProperty", "🔄 Setting up click listeners...");

        // زر الرجوع
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AddProperty", "🔙 Back button clicked");
                    finish();
                }
            });
            Log.d("AddProperty", "✅ Back button listener set");
        } else {
            Log.e("AddProperty", "❌ btnBack is NULL - Check XML ID: btnBack");
        }

        // زر إضافة الصور
        if (btnAddImages != null) {
            btnAddImages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AddProperty", "📸 Add Images button CLICKED!");
                    openImagePicker();
                }
            });
            Log.d("AddProperty", "✅ Add Images button listener set");

            // اختبار إضافي: تغيير لون الزر للتأكد من أنه مرئي
            btnAddImages.setBackgroundColor(0xFFE3F2FD); // أزرق فاتح
        } else {
            Log.e("AddProperty", "❌ btnAddImages is NULL - Check XML ID: btnAddImages");
            Toast.makeText(this, "زر إضافة الصور غير موجود", Toast.LENGTH_LONG).show();
        }

        // زر تحديد الموقع - ✅ محدث
        if (btnSelectLocation != null) {
            btnSelectLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AddProperty", "🗺️ Select Location button clicked");
                    requestLocationPermission();
                }
            });
            Log.d("AddProperty", "✅ Select Location button listener set");
        } else {
            Log.e("AddProperty", "❌ btnSelectLocation is NULL");
        }

        // زر النشر
        if (btnPublish != null) {
            btnPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AddProperty", "📤 Publish button CLICKED!");
                    addPropertyToDatabase();
                }
            });
            Log.d("AddProperty", "✅ Publish button listener set");

            // اختبار إضافي: تغيير لون الزر للتأكد من أنه مرئي
            btnPublish.setBackgroundColor(0xFF2196F3); // أزرق
        } else {
            Log.e("AddProperty", "❌ btnPublish is NULL - Check XML ID: btnPublish");
            Toast.makeText(this, "زر النشر غير موجود", Toast.LENGTH_LONG).show();
        }

        setupSelectableFields();
        Log.d("AddProperty", "✅ All click listeners setup completed");
    }

    // ✅ دالة طلب إذن الموقع
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            openMapForLocationSelection();
        }
    }

    // ✅ التعامل مع نتيجة طلب الإذن
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openMapForLocationSelection();
            } else {
                Toast.makeText(this, "يجب منح إذن الموقع لتحديد الموقع", Toast.LENGTH_SHORT).show();
                showManualLocationDialog();
            }
        }
    }

    // ✅ فتح الخريطة لتحديد الموقع
    private void openMapForLocationSelection() {
        try {
            String address = etAddress.getText().toString().trim();
            String searchQuery = address.isEmpty() ? "السعودية" : address;

            // استخدام خرائط Google
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + Uri.encode(searchQuery)));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, MAP_REQUEST_CODE);
                Toast.makeText(this, "افتح الخريطة وحدد الموقع، ثم ارجع للتطبيق", Toast.LENGTH_LONG).show();
            } else {
                // إذا لم تكن خرائط Google مثبتة، افتح متصفح
                String url = "https://www.google.com/maps/search/?api=1&query=" +
                        Uri.encode(searchQuery);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                Toast.makeText(this, "افتح الخريطة في المتصفح وانسخ الإحداثيات", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Location", "Error opening map: " + e.getMessage());
            Toast.makeText(this, "خطأ في فتح الخريطة، استخدم الإدخال اليدوي", Toast.LENGTH_LONG).show();
            showManualLocationDialog();
        }
    }

    // ✅ بديل يدوي لإدخال الإحداثيات
    private void showManualLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("إدخال الإحداثيات يدوياً");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location, null);
        builder.setView(dialogView);

        EditText etLatitude = dialogView.findViewById(R.id.etLatitude);
        EditText etLongitude = dialogView.findViewById(R.id.etLongitude);

        // تعيين قيم افتراضية للإحداثيات
        etLatitude.setText(String.valueOf(selectedLatitude));
        etLongitude.setText(String.valueOf(selectedLongitude));

        builder.setPositiveButton("حفظ", (dialog, which) -> {
            try {
                selectedLatitude = Double.parseDouble(etLatitude.getText().toString());
                selectedLongitude = Double.parseDouble(etLongitude.getText().toString());
                Toast.makeText(this, "تم حفظ الموقع: " + selectedLatitude + ", " + selectedLongitude, Toast.LENGTH_SHORT).show();
                Log.d("Location", "Manual location saved: " + selectedLatitude + ", " + selectedLongitude);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "يرجى إدخال إحداثيات صحيحة", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void openImagePicker() {
        Log.d("AddProperty", "🖼️ Opening image picker...");
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            Log.d("AddProperty", "✅ Image picker intent created");

            startActivityForResult(Intent.createChooser(intent, "اختر الصور"), PICK_IMAGES_REQUEST);
            Log.d("AddProperty", "✅ Image picker activity started");

        } catch (Exception e) {
            Log.e("AddProperty", "❌ Error opening image picker: " + e.getMessage());
            Toast.makeText(this, "خطأ في فتح المعرض: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AddProperty", "📬 onActivityResult - Request: " + requestCode + ", Result: " + resultCode);

        if (requestCode == PICK_IMAGES_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d("AddProperty", "✅ Image selection successful");
                if (data != null) {
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        Log.d("AddProperty", "📸 Multiple images selected: " + count);
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            selectedImages.add(imageUri);
                            Log.d("AddProperty", "   - Added image: " + imageUri);
                        }
                        Toast.makeText(this, "تم اختيار " + count + " صورة", Toast.LENGTH_SHORT).show();
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        selectedImages.add(imageUri);
                        Log.d("AddProperty", "📸 Single image selected: " + imageUri);
                        Toast.makeText(this, "تم اختيار صورة واحدة", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("AddProperty", "❌ No images found in data");
                        Toast.makeText(this, "لم يتم اختيار أي صورة", Toast.LENGTH_SHORT).show();
                    }
                    updateAddImagesButton();
                } else {
                    Log.d("AddProperty", "❌ Data is null");
                    Toast.makeText(this, "خطأ في البيانات", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("AddProperty", "❌ Image selection cancelled or failed");
                Toast.makeText(this, "تم إلغاء اختيار الصور", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateAddImagesButton() {
        if (btnAddImages != null) {
            if (selectedImages.size() > 0) {
                btnAddImages.setText("إضافة صور (" + selectedImages.size() + ")");
                Log.d("AddProperty", "🔄 Updated button to show " + selectedImages.size() + " images");
            } else {
                btnAddImages.setText("إضافة صور");
            }
        }
    }

    private void setupSelectableFields() {
        Log.d("AddProperty", "🔄 Setting up selectable fields...");

        if (etPropertyType != null) {
            etPropertyType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AddProperty", "🏠 Property type field clicked");
                    showPropertyTypeDialog();
                }
            });
            Log.d("AddProperty", "✅ Property type field listener set");
        }

        if (etOfferType != null) {
            etOfferType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AddProperty", "💰 Offer type field clicked");
                    showOfferTypeDialog();
                }
            });
            Log.d("AddProperty", "✅ Offer type field listener set");
        }
    }

    private void showPropertyTypeDialog() {
        Log.d("AddProperty", "📋 Showing property type dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اختر نوع العقار");

        String[] propertyTypes = {
                "شقة", "فيلا", "منزل", "أرض", "مكتب",
                "محل تجاري", "مخزن", "مصنع", "مزرعة",
                "شاليه", "استوديو", "بنتهاوس", "عمارة"
        };

        builder.setItems(propertyTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedType = propertyTypes[which];
                if (etPropertyType != null) {
                    etPropertyType.setText(selectedType);
                    Log.d("AddProperty", "✅ Selected property type: " + selectedType);
                }
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void showOfferTypeDialog() {
        Log.d("AddProperty", "📋 Showing offer type dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اختر طريقة العرض");

        String[] offerTypes = {"للإيجار", "للبيع", "للإيجار اليومي", "للإيجار الشهري", "للإيجار السنوي"};

        builder.setItems(offerTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOffer = offerTypes[which];
                if (etOfferType != null) {
                    etOfferType.setText(selectedOffer);
                    Log.d("AddProperty", "✅ Selected offer type: " + selectedOffer);
                }
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void addPropertyToDatabase() {
        Log.d("AddProperty", "🔄 Starting to add property to database...");

        if (!validateForm()) {
            Log.d("AddProperty", "❌ Form validation failed");
            return;
        }

        Log.d("AddProperty", "✅ Form validation passed");

        try {
            // جمع البيانات من النموذج
            String title = etPropertyType.getText().toString() + " - " + etAddress.getText().toString();
            String description = etDescription.getText().toString().isEmpty() ?
                    "لا يوجد وصف" : etDescription.getText().toString();

            String priceText = etPrice.getText().toString().trim();
            double price = Double.parseDouble(priceText);

            String type = etPropertyType.getText().toString();
            String offerType = etOfferType.getText().toString();
            String address = etAddress.getText().toString();

            int bedrooms = 0;
            if (!etBedrooms.getText().toString().isEmpty()) {
                try {
                    bedrooms = Integer.parseInt(etBedrooms.getText().toString());
                } catch (NumberFormatException e) {
                    Log.d("AddProperty", "⚠️ Bedrooms parsing error, using default 0");
                }
            }

            int bathrooms = 0;
            if (!etBathrooms.getText().toString().isEmpty()) {
                try {
                    bathrooms = Integer.parseInt(etBathrooms.getText().toString());
                } catch (NumberFormatException e) {
                    Log.d("AddProperty", "⚠️ Bathrooms parsing error, using default 0");
                }
            }

            double area = 0.0;
            if (!etArea.getText().toString().isEmpty()) {
                try {
                    area = Double.parseDouble(etArea.getText().toString());
                } catch (NumberFormatException e) {
                    Log.d("AddProperty", "⚠️ Area parsing error, using default 0.0");
                }
            }

            String contactName = etContactName.getText().toString().trim();
            String contactPhone = etContactPhone.getText().toString().trim();

            Log.d("AddProperty", "📊 Collected data:");
            Log.d("AddProperty", "   - Title: " + title);
            Log.d("AddProperty", "   - Price: " + price);
            Log.d("AddProperty", "   - Type: " + type);
            Log.d("AddProperty", "   - Offer Type: " + offerType);
            Log.d("AddProperty", "   - Address: " + address);
            Log.d("AddProperty", "   - Contact: " + contactName + " - " + contactPhone);
            Log.d("AddProperty", "   - Location: " + selectedLatitude + ", " + selectedLongitude);

            // حفظ الصور أولاً
            List<String> savedImagePaths = new ArrayList<>();
            if (!selectedImages.isEmpty()) {
                Log.d("AddProperty", "💾 Saving " + selectedImages.size() + " images...");
                long tempId = System.currentTimeMillis();
                savedImagePaths = saveImagesToStorage(tempId);
                Log.d("AddProperty", "✅ Saved " + savedImagePaths.size() + " images temporarily");
            } else {
                Log.d("AddProperty", "📷 No images to save");
            }

            // ✅ استخدام الدالة المحدثة مع الإحداثيات
            Log.d("AddProperty", "💾 Adding property to database...");
            long propertyId = databaseHelper.addProperty(
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
                    selectedLatitude,  // ✅ الإحداثيات
                    selectedLongitude, // ✅ الإحداثيات
                    savedImagePaths
            );

            if (propertyId != -1) {
                Log.d("AddProperty", "✅ Property added successfully with ID: " + propertyId);

                // إعادة حفظ الصور بالـ ID الحقيقي للعقار
                if (!savedImagePaths.isEmpty()) {
                    List<String> finalImagePaths = saveImagesToStorage(propertyId);
                    databaseHelper.updatePropertyImages(propertyId, finalImagePaths);
                    Log.d("AddProperty", "✅ Images updated with real property ID");
                }

                String successMessage = "تم إضافة العقار بنجاح!";
                if (!savedImagePaths.isEmpty()) {
                    successMessage += " (" + savedImagePaths.size() + " صورة)";
                }

                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();
                Log.d("AddProperty", "🎉 PROPERTY SAVED SUCCESSFULLY!");

                // تنظيف النموذج والعودة
                clearForm();

                // العودة للصفحة الرئيسية بعد ثانيتين
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.d("AddProperty", "🔙 Returning to MainActivity");
                                Intent intent = new Intent(add_property.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            }
                        },
                        2000
                );

            } else {
                Log.e("AddProperty", "❌ FAILED to add property to database");
                Toast.makeText(this, "خطأ في إضافة العقار إلى قاعدة البيانات", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Log.e("AddProperty", "❌ Number format error: " + e.getMessage());
            Toast.makeText(this, "يرجى إدخال سعر صحيح", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AddProperty", "❌ UNEXPECTED ERROR: " + e.getMessage(), e);
            Toast.makeText(this, "حدث خطأ غير متوقع: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private List<String> saveImagesToStorage(long propertyId) {
        List<String> savedImagePaths = new ArrayList<>();

        if (selectedImages.isEmpty()) {
            return savedImagePaths;
        }

        try {
            File propertyDir = new File(getFilesDir(), "property_" + propertyId);
            if (!propertyDir.exists()) {
                propertyDir.mkdirs();
                Log.d("SaveImages", "📁 Created directory: " + propertyDir.getAbsolutePath());
            }

            for (int i = 0; i < selectedImages.size(); i++) {
                Uri imageUri = selectedImages.get(i);
                String imagePath = saveSingleImage(imageUri, propertyId, i);
                if (imagePath != null) {
                    savedImagePaths.add(imagePath);
                    Log.d("SaveImages", "💾 Saved image: " + imagePath);
                } else {
                    Log.e("SaveImages", "❌ Failed to save image: " + imageUri);
                }
            }

            Log.d("SaveImages", "✅ Successfully saved " + savedImagePaths.size() + " images for property: " + propertyId);
        } catch (Exception e) {
            Log.e("SaveImages", "❌ Error saving images: " + e.getMessage());
        }

        return savedImagePaths;
    }

    private String saveSingleImage(Uri imageUri, long propertyId, int imageIndex) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e("SaveImage", "❌ Cannot open input stream for URI: " + imageUri);
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                Log.e("SaveImage", "❌ Cannot decode bitmap from URI: " + imageUri);
                inputStream.close();
                return null;
            }

            File imageFile = new File(getFilesDir(), "property_" + propertyId + "_" + imageIndex + ".jpg");
            Log.d("SaveImage", "💾 Saving to: " + imageFile.getAbsolutePath());

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            inputStream.close();

            Log.d("SaveImage", "✅ Image saved successfully: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("SaveImage", "❌ Error saving image: " + e.getMessage());
            return null;
        }
    }

    private boolean validateForm() {
        Log.d("AddProperty", "🔄 Validating form...");
        boolean isValid = true;

        if (etPropertyType.getText().toString().trim().isEmpty()) {
            etPropertyType.setError("يرجى اختيار نوع العقار");
            isValid = false;
            Log.d("AddProperty", "❌ Property type is empty");
        } else {
            etPropertyType.setError(null);
        }

        if (etOfferType.getText().toString().trim().isEmpty()) {
            etOfferType.setError("يرجى اختيار طريقة العرض");
            isValid = false;
            Log.d("AddProperty", "❌ Offer type is empty");
        } else {
            etOfferType.setError(null);
        }

        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("يرجى إدخال السعر");
            isValid = false;
            Log.d("AddProperty", "❌ Price is empty");
        } else {
            etPrice.setError(null);
        }

        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("يرجى إدخال العنوان");
            isValid = false;
            Log.d("AddProperty", "❌ Address is empty");
        } else {
            etAddress.setError(null);
        }

        if (etContactName.getText().toString().trim().isEmpty()) {
            etContactName.setError("يرجى إدخال اسم المعلن");
            isValid = false;
            Log.d("AddProperty", "❌ Contact name is empty");
        } else {
            etContactName.setError(null);
        }

        if (etContactPhone.getText().toString().trim().isEmpty()) {
            etContactPhone.setError("يرجى إدخال رقم الهاتف");
            isValid = false;
            Log.d("AddProperty", "❌ Contact phone is empty");
        } else {
            etContactPhone.setError(null);
        }

        Log.d("AddProperty", "📋 Form validation result: " + (isValid ? "PASSED" : "FAILED"));
        return isValid;
    }

    private void clearForm() {
        Log.d("AddProperty", "🔄 Clearing form...");
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
        selectedImages.clear();
        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
        updateAddImagesButton();
        Log.d("AddProperty", "✅ Form cleared");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        Log.d("AddProperty", "=== ACTIVITY DESTROYED ===");
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_add);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_add) {
                    // نحن بالفعل في الصفحة الاضافة
                    return true;
                } else if (id == R.id.nav_search) {
                    openSearchActivity();
                    return true;
                } else if (id == R.id.nav_home) {
                    openMainActivity();
                    return true;
                } else if (id == R.id.nav_my_properties) {
                    openMyProperties();
                    return true;
                }
                return false;
            }
        });
    }

    private void openSearchActivity() {
        try {
            Intent intent = new Intent(add_property.this, SearchActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة البحث: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Error opening SearchActivity: " + e.getMessage());
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(add_property.this, MainActivity.class);
        startActivity(intent);
    }

    // ✅ دالة فتح واجهة المفضلات
    private void openFavoritesActivity() {
        Intent intent = new Intent(add_property.this, FavoritesActivity.class);
        startActivity(intent);
    }

    private void openMyProperties() {
        // ✅ عرض عقارات المستخدم
        List<Property> myProperties = databaseHelper.getAllProperties();
        if (myProperties.isEmpty()) {
            Toast.makeText(this, "لم تقم بإضافة أي عقارات بعد", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(add_property.this, activity_my_properties.class);
            startActivity(intent);
        }
    }
}