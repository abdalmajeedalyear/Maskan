package com.example.maskan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    // عناصر شريط الفلترة
    private View cardPropertyType, cardPrice, cardOfferType, cardBedrooms;
    private TextView tvPropertyType, tvPrice, tvOfferType, tvBedrooms;
    private TextView tvResultsCount, tvSortBy;
    private RecyclerView rvSearchResults;
    private View layoutEmpty;
    private BottomNavigationView bottomNavigationView;

    // البيانات
    private DatabaseHelper databaseHelper;
    private PropertyAdapter propertyAdapter;
    private List<Property> allProperties = new ArrayList<>();
    private List<Property> filteredProperties = new ArrayList<>();

    // معايير البحث
    private String selectedPropertyType = "";
    private String selectedOfferType = "";
    private String selectedBedrooms = "";
    private String minPrice = "";
    private String maxPrice = "";

    // البحث التلقائي
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeViews();
        databaseHelper = DatabaseHelper.getInstance(this);

        setupRecyclerView();
        setupClickListeners();
        loadAllProperties();

        setupBottomNavigation();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initializeViews() {
        // شريط الفلترة
        cardPropertyType = findViewById(R.id.cardPropertyType);
        cardPrice = findViewById(R.id.cardPrice);
        cardOfferType = findViewById(R.id.cardOfferType);
        cardBedrooms = findViewById(R.id.cardBedrooms);

        tvPropertyType = findViewById(R.id.tvPropertyType);
        tvPrice = findViewById(R.id.tvPrice);
        tvOfferType = findViewById(R.id.tvOfferType);
        tvBedrooms = findViewById(R.id.tvBedrooms);

        // النتائج
        tvResultsCount = findViewById(R.id.tvResultsCount);
        tvSortBy = findViewById(R.id.tvSortBy);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        // شريط التنقل السفلي
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupRecyclerView() {
        propertyAdapter = new PropertyAdapter(filteredProperties);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(propertyAdapter);

        propertyAdapter.setOnItemClickListener(new PropertyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Property property) {
                openPropertyDetails(property);
            }
        });
    }

    private void setupClickListeners() {
        // أزرار الفلترة
        cardPropertyType.setOnClickListener(v -> showPropertyTypeFilter());
        cardPrice.setOnClickListener(v -> showPriceFilter());
        cardOfferType.setOnClickListener(v -> showOfferTypeFilter());
        cardBedrooms.setOnClickListener(v -> showBedroomsFilter());

        // الترتيب
        tvSortBy.setOnClickListener(v -> showSortOptions());
    }

    private void showPropertyTypeFilter() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_simple_list, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.tvTitle);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        title.setText("اختر نوع العقار");

        List<String> options = new ArrayList<>();
        options.add("جميع الأنواع");
        options.add("شقة");
        options.add("فيلا");
        options.add("منزل");
        options.add("أرض");
        options.add("مكتب");
        options.add("محل تجاري");
        options.add("عمارة سكنية");
        options.add("استراحة");
        options.add("شاليه");
        options.add("مزرعة");
        options.add("مصنع");
        options.add("مخزن");

        SimpleListAdapter adapter = new SimpleListAdapter(options, new SimpleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                if (item.equals("جميع الأنواع")) {
                    selectedPropertyType = "";
                    tvPropertyType.setText("نوع العقار");
                } else {
                    selectedPropertyType = item;
                    tvPropertyType.setText(item);
                }
                dialog.dismiss();
                performAutoSearch();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void showPriceFilter() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_price, null);
        dialog.setContentView(view);

        setupPriceBottomSheet(view, dialog);
        dialog.show();
    }

    private void showOfferTypeFilter() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_simple_list, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.tvTitle);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        title.setText("اختر نوع العرض");

        List<String> options = new ArrayList<>();
        options.add("جميع العروض");
        options.add("للبيع");
        options.add("للإيجار");
        options.add("للإيجار اليومي");
        options.add("للإيجار الشهري");

        SimpleListAdapter adapter = new SimpleListAdapter(options, new SimpleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                if (item.equals("جميع العروض")) {
                    selectedOfferType = "";
                    tvOfferType.setText("نوع العرض");
                } else {
                    selectedOfferType = item;
                    tvOfferType.setText(item);
                }
                dialog.dismiss();
                performAutoSearch();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void showBedroomsFilter() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_simple_list, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.tvTitle);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        title.setText("اختر عدد الغرف");

        List<String> options = new ArrayList<>();
        options.add("أي عدد");
        options.add("1 غرفة");
        options.add("2 غرف");
        options.add("3 غرف");
        options.add("4 غرف");
        options.add("5 غرف");
        options.add("6+ غرف");

        SimpleListAdapter adapter = new SimpleListAdapter(options, new SimpleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                if (item.equals("أي عدد")) {
                    selectedBedrooms = "";
                    tvBedrooms.setText("الغرف");
                } else {
                    selectedBedrooms = item;
                    tvBedrooms.setText(item);
                }
                dialog.dismiss();
                performAutoSearch();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void setupPriceBottomSheet(View view, BottomSheetDialog dialog) {
        EditText etMinPriceBottom = view.findViewById(R.id.etMinPriceBottom);
        EditText etMaxPriceBottom = view.findViewById(R.id.etMaxPriceBottom);
        SeekBar priceSeekBar = view.findViewById(R.id.priceSeekBar);
        Button btnCancelPrice = view.findViewById(R.id.btnCancelPrice);
        Button btnApplyPrice = view.findViewById(R.id.btnApplyPrice);

        if (!minPrice.isEmpty()) {
            etMinPriceBottom.setText(minPrice);
        }
        if (!maxPrice.isEmpty()) {
            etMaxPriceBottom.setText(maxPrice);
        }

        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int maxPriceValue = 5000000;
                    int calculatedPrice = (progress * maxPriceValue) / 100;
                    etMaxPriceBottom.setText(String.valueOf(calculatedPrice));

                    int minPriceValue = (calculatedPrice * 20) / 100;
                    etMinPriceBottom.setText(String.valueOf(minPriceValue));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnCancelPrice.setOnClickListener(v -> dialog.dismiss());

        btnApplyPrice.setOnClickListener(v -> {
            minPrice = etMinPriceBottom.getText().toString();
            maxPrice = etMaxPriceBottom.getText().toString();

            if (!minPrice.isEmpty() && !maxPrice.isEmpty()) {
                tvPrice.setText(minPrice + " - " + maxPrice);
            } else if (!minPrice.isEmpty()) {
                tvPrice.setText("من " + minPrice);
            } else if (!maxPrice.isEmpty()) {
                tvPrice.setText("إلى " + maxPrice);
            } else {
                tvPrice.setText("السعر");
            }

            dialog.dismiss();
            performAutoSearch();
        });

        TextWatcher priceWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String maxText = etMaxPriceBottom.getText().toString();
                    if (!maxText.isEmpty()) {
                        int maxPriceValue = Integer.parseInt(maxText);
                        int progress = (maxPriceValue * 100) / 5000000;
                        priceSeekBar.setProgress(Math.min(progress, 100));
                    }
                } catch (NumberFormatException e) {}
            }
        };

        etMinPriceBottom.addTextChangedListener(priceWatcher);
        etMaxPriceBottom.addTextChangedListener(priceWatcher);
    }

    private void showSortOptions() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_simple_list, null);
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.tvTitle);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        title.setText("ترتيب النتائج");

        List<String> options = new ArrayList<>();
        options.add("الافتراضي");
        options.add("الأحدث أولاً");
        options.add("الأقدم أولاً");
        options.add("السعر: من الأقل للأعلى");
        options.add("السعر: من الأعلى للأقل");

        SimpleListAdapter adapter = new SimpleListAdapter(options, new SimpleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                tvSortBy.setText(item);
                dialog.dismiss();
                performAutoSearch();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void loadAllProperties() {
        new Thread(() -> {
            try {
                allProperties = databaseHelper.getAllProperties();
                filteredProperties.clear();
                filteredProperties.addAll(allProperties);

                runOnUiThread(() -> {
                    updateUI();
                    performAutoSearch();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(SearchActivity.this, "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void performAutoSearch() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        searchRunnable = new Runnable() {
            @Override
            public void run() {
                filterProperties();
            }
        };
        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
    }

    private void filterProperties() {
        List<Property> results = new ArrayList<>();

        for (Property property : allProperties) {
            if (matchesSearchCriteria(property)) {
                results.add(property);
            }
        }

        filteredProperties.clear();
        filteredProperties.addAll(results);
        updateUI();
    }

    private boolean matchesSearchCriteria(Property property) {
        if (!selectedPropertyType.isEmpty()) {
            String propertyType = property.getType();
            if (propertyType == null || !propertyType.toLowerCase().contains(selectedPropertyType.toLowerCase())) {
                return false;
            }
        }

        if (!selectedOfferType.isEmpty()) {
            String offerType = property.getOfferType();
            if (offerType == null || !offerType.toLowerCase().contains(selectedOfferType.toLowerCase())) {
                return false;
            }
        }

        if (!selectedBedrooms.isEmpty() && !selectedBedrooms.equals("أي عدد")) {
            String propertyBedrooms = property.getBedrooms();
            if (propertyBedrooms != null) {
                try {
                    int requiredRooms = extractNumberFromBedrooms(selectedBedrooms);
                    int propertyRooms = Integer.parseInt(propertyBedrooms);

                    if (selectedBedrooms.contains("+")) {
                        if (propertyRooms < requiredRooms) {
                            return false;
                        }
                    } else {
                        if (propertyRooms != requiredRooms) {
                            return false;
                        }
                    }
                } catch (NumberFormatException e) {}
            }
        }

        if (!minPrice.isEmpty() || !maxPrice.isEmpty()) {
            try {
                double propertyPrice = Double.parseDouble(property.getPrice());
                double min = minPrice.isEmpty() ? 0 : Double.parseDouble(minPrice);
                double max = maxPrice.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPrice);

                if (propertyPrice < min || propertyPrice > max) {
                    return false;
                }
            } catch (NumberFormatException e) {}
        }

        return true;
    }

    private int extractNumberFromBedrooms(String bedroomsText) {
        try {
            return Integer.parseInt(bedroomsText.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateUI() {
        propertyAdapter.updateList(filteredProperties);
        tvResultsCount.setText(filteredProperties.size() + " عقار");

        if (filteredProperties.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    private void openPropertyDetails(Property property) {
        Intent intent = new Intent(this, activity_property_details.class);
        intent.putExtra("property_id", property.getId());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        // ✅ هذا هو السر: تحديد العنصر النشط في شريط التنقل
        bottomNavigationView.setSelectedItemId(R.id.nav_search);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_search) {
                    // نحن بالفعل في صفحة البحث - لا تفعل شيئاً
                    return true;
                } else if (id == R.id.nav_home) {
                    openMainActivity();
                    return true;
                } else if (id == R.id.nav_add) {
                    openAddProperty();
                    return true;
                } else if (id == R.id.nav_my_properties) {
                    openMyProperties();
                    return true;
                }
                return false;
            }
        });
    }

    private void openMainActivity() {
        try {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
            // لا تستخدم finish() هنا للحفاظ على تجربة المستخدم
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في فتح شاشة الرئيسية: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("SearchActivity", "Error opening MainActivity: " + e.getMessage());
        }
    }

    private void openAddProperty() {
        Intent intent = new Intent(SearchActivity.this, add_property.class);
        startActivity(intent);
    }

    private void openFavoritesActivity() {
        Intent intent = new Intent(SearchActivity.this, FavoritesActivity.class);
        startActivity(intent);
    }

    private void openMyProperties() {
        // ✅ عرض عقارات المستخدم
        List<Property> myProperties = databaseHelper.getAllProperties();
        if (myProperties.isEmpty()) {
            Toast.makeText(this, "لم تقم بإضافة أي عقارات بعد", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(SearchActivity.this, activity_my_properties.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}