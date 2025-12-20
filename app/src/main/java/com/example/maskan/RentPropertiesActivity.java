package com.example.maskan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RentPropertiesActivity extends AppCompatActivity {

    private RecyclerView rvRentProperties;
    private EditText etSearch;
    private ImageButton btnBack, btnFilter;
    private FloatingActionButton fabAdd;
    private LinearLayout layoutEmpty;
    private TextView tvTotalProperties, tvAveragePrice;

    private DatabaseHelper databaseHelper;
    private PropertyAdapter propertyAdapter;
    private List<Property> allRentProperties = new ArrayList<>();
    private List<Property> filteredProperties = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_properties);

        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);

        setupRecyclerViews();
        setupClickListeners();
        loadRentProperties();
        setupSearch();
    }

    private void initializeViews() {
        rvRentProperties = findViewById(R.id.rvRentProperties);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter);
        fabAdd = findViewById(R.id.fabAdd);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalProperties = findViewById(R.id.tvTotalProperties);
        tvAveragePrice = findViewById(R.id.tvAveragePrice);
    }

    private void setupRecyclerViews() {
        // إعداد قائمة العقارات
        propertyAdapter = new PropertyAdapter(filteredProperties);
        rvRentProperties.setLayoutManager(new LinearLayoutManager(this));
        rvRentProperties.setAdapter(propertyAdapter);

        // إعداد مستمع النقر على العقار
        propertyAdapter.setOnItemClickListener(new PropertyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Property property) {
                openPropertyDetails(property);
            }
        });
    }

    private void setupClickListeners() {
        // زر العودة
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // زر الفلتر
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterOptions();
            }
        });

        // زر إضافة عقار
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RentPropertiesActivity.this, add_property.class);
                startActivity(intent);
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProperties(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadRentProperties() {
        // ✅ جلب عقارات الإيجار فقط من قاعدة البيانات
        allRentProperties = databaseHelper.getPropertiesByType("للإيجار");
        // إذا لم تعمل، جرب: databaseHelper.getPropertiesByType("إيجار")

        filteredProperties.clear();
        filteredProperties.addAll(allRentProperties);

        updateUI();
        updateStatistics();
    }

    private void updateUI() {
        propertyAdapter.updateList(filteredProperties);

        if (filteredProperties.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvRentProperties.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvRentProperties.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        // تحديث الإحصائيات
        tvTotalProperties.setText(String.valueOf(allRentProperties.size()));

        // حساب متوسط السعر
        if (!allRentProperties.isEmpty()) {
            double totalPrice = 0;
            int count = 0;

            for (Property property : allRentProperties) {
                try {
                    double price = Double.parseDouble(property.getPrice());
                    totalPrice += price;
                    count++;
                } catch (NumberFormatException e) {
                    // تجاهل العقارات التي تحتوي على سعر غير صالح
                }
            }

            if (count > 0) {
                double averagePrice = totalPrice / count;
                tvAveragePrice.setText(String.format("%.0f", averagePrice));
            } else {
                tvAveragePrice.setText("0");
            }
        } else {
            tvAveragePrice.setText("0");
        }
    }

    private void filterProperties(String query) {
        filteredProperties.clear();

        if (query.isEmpty()) {
            filteredProperties.addAll(allRentProperties);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Property property : allRentProperties) {
                if (property.getTitle() != null && property.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        property.getLocation() != null && property.getLocation().toLowerCase().contains(lowerCaseQuery) ||
                        property.getDescription() != null && property.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProperties.add(property);
                }
            }
        }

        updateUI();
    }

    private void showFilterOptions() {
        // فلتر سريع بدلاً من التصنيفات المعقدة
        String[] filterOptions = {
                "جميع العقارات",
                "شقق للإيجار",
                "فلل للإيجار",
                "شاليهات للإيجار",
                "مكاتب للإيجار"
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("تصفية النتائج");
        builder.setItems(filterOptions, (dialog, which) -> {
            applyQuickFilter(filterOptions[which]);
        });
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void applyQuickFilter(String filter) {
        filteredProperties.clear();

        switch (filter) {
            case "جميع العقارات":
                filteredProperties.addAll(allRentProperties);
                break;
            case "شقق للإيجار":
                for (Property property : allRentProperties) {
                    if (property.getTitle() != null && property.getTitle().toLowerCase().contains("شقة")) {
                        filteredProperties.add(property);
                    }
                }
                break;
            case "فلل للإيجار":
                for (Property property : allRentProperties) {
                    if (property.getTitle() != null && property.getTitle().toLowerCase().contains("فيلا")) {
                        filteredProperties.add(property);
                    }
                }
                break;
            case "شاليهات للإيجار":
                for (Property property : allRentProperties) {
                    if (property.getTitle() != null && property.getTitle().toLowerCase().contains("شاليه")) {
                        filteredProperties.add(property);
                    }
                }
                break;
            case "مكاتب للإيجار":
                for (Property property : allRentProperties) {
                    if (property.getTitle() != null && property.getTitle().toLowerCase().contains("مكتب")) {
                        filteredProperties.add(property);
                    }
                }
                break;
        }

        updateUI();
        Toast.makeText(this, "عرض: " + filter, Toast.LENGTH_SHORT).show();
    }

    private void openPropertyDetails(Property property) {
        Intent intent = new Intent(this, activity_property_details.class);

        // تمرير بيانات العقار
        intent.putExtra("property_title", property.getTitle());
        intent.putExtra("property_location", property.getLocation());
        intent.putExtra("property_price", property.getPrice());
        intent.putExtra("property_bedrooms", property.getBedrooms());
        intent.putExtra("property_bathrooms", property.getBathrooms());
        intent.putExtra("property_type", property.getType());

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // تحديث البيانات عند العودة للنشاط
        loadRentProperties();
    }
}