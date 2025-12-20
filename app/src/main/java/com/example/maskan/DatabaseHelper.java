package com.example.maskan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "maskan.db";
    private static final int DATABASE_VERSION = 4; // ✅ زيادة الإصدار لإضافة جدول المفضلات

    // جدول العقارات
    private static final String TABLE_PROPERTIES = "properties";
    private static DatabaseHelper instance;

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_OFFER_TYPE = "offer_type";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_BEDROOMS = "bedrooms";
    private static final String COLUMN_BATHROOMS = "bathrooms";
    private static final String COLUMN_AREA = "area";
    private static final String COLUMN_CONTACT_NAME = "contact_name";
    private static final String COLUMN_CONTACT_PHONE = "contact_phone";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_IMAGES = "images";
    private static final String COLUMN_CREATED_AT = "created_at";

    // ✅ جدول المفضلات الجديد
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_FAVORITE_ID = "favorite_id";
    private static final String COLUMN_PROPERTY_ID = "property_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_ADDED_AT = "added_at";

    // ✅ جعل Constructor خاص
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ✅ طريقة للحصول على نسخة واحدة فقط
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROPERTIES_TABLE = "CREATE TABLE " + TABLE_PROPERTIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_OFFER_TYPE + " TEXT,"
                + COLUMN_ADDRESS + " TEXT,"
                + COLUMN_BEDROOMS + " INTEGER,"
                + COLUMN_BATHROOMS + " INTEGER,"
                + COLUMN_AREA + " REAL,"
                + COLUMN_CONTACT_NAME + " TEXT,"
                + COLUMN_CONTACT_PHONE + " TEXT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + COLUMN_IMAGES + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_PROPERTIES_TABLE);

        // ✅ إنشاء جدول المفضلات
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PROPERTY_ID + " INTEGER,"
                + COLUMN_USER_ID + " INTEGER DEFAULT 1,"
                + COLUMN_ADDED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_PROPERTY_ID + ") REFERENCES " + TABLE_PROPERTIES + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);

        // ✅ إضافة بعض البيانات التجريبية إذا كان الجدول فارغاً
        addSampleData(db);

        // ✅ أضف بعد إنشاء الجداول الحالية
// جدول التقييمات
        String CREATE_RATINGS_TABLE = "CREATE TABLE IF NOT EXISTS property_ratings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "property_id INTEGER," +
                "rating REAL," +
                "comment TEXT," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(property_id) REFERENCES properties(id)" +
                ")";
        db.execSQL(CREATE_RATINGS_TABLE);

// جدول المشاركات
        String CREATE_SHARES_TABLE = "CREATE TABLE IF NOT EXISTS property_shares (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "property_id INTEGER," +
                "shared_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(property_id) REFERENCES properties(id)" +
                ")";
        db.execSQL(CREATE_SHARES_TABLE);

// جدول الاتصالات
        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS property_contacts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "property_id INTEGER," +
                "contacted_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(property_id) REFERENCES properties(id)" +
                ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) { // ✅ زيادة الإصدار إلى 3
            // إنشاء الجداول الجديدة
            String CREATE_RATINGS_TABLE = "CREATE TABLE IF NOT EXISTS property_ratings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "property_id INTEGER," +
                    "rating REAL," +
                    "comment TEXT," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(property_id) REFERENCES properties(id)" +
                    ")";
            db.execSQL(CREATE_RATINGS_TABLE);

            String CREATE_SHARES_TABLE = "CREATE TABLE IF NOT EXISTS property_shares (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "property_id INTEGER," +
                    "shared_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(property_id) REFERENCES properties(id)" +
                    ")";
            db.execSQL(CREATE_SHARES_TABLE);

            String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS property_contacts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "property_id INTEGER," +
                    "contacted_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(property_id) REFERENCES properties(id)" +
                    ")";
            db.execSQL(CREATE_CONTACTS_TABLE);

            Log.d("Database", "تم إنشاء الجداول الجديدة في الترقية");
        }


        if (oldVersion < 4) {
            // هذا هو الأمر الأكثر أماناً: أضف العمود فقط إذا لم يكن موجوداً
           /* try {
                db.execSQL("ALTER TABLE properties ADD COLUMN sale_type TEXT DEFAULT 'بيع'");
                // أو DEFAULT 'ايجار' حسب الافتراضي الذي تريده
                Log.d("DatabaseHelper", "تم ترقية قاعدة البيانات: أضيف عمود sale_type");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "خطأ أثناء إضافة العمود sale_type: " + e.getMessage());
            }*/
        }
    }

    // ✅ إضافة بيانات تجريبية عند إنشاء الجدول لأول مرة
    private void addSampleData(SQLiteDatabase db) {
        try {
            ContentValues values1 = new ContentValues();
            values1.put(COLUMN_TITLE, "شقة فاخرة في الرياض - النخيل");
            values1.put(COLUMN_DESCRIPTION, "شقة جميلة في حي النخيل بمساحة 150م²");
            values1.put(COLUMN_PRICE, 1200.0);
            values1.put(COLUMN_TYPE, "شقة");
            values1.put(COLUMN_OFFER_TYPE, "للإيجار");
            values1.put(COLUMN_ADDRESS, "حي النخيل، الرياض");
            values1.put(COLUMN_BEDROOMS, 3);
            values1.put(COLUMN_BATHROOMS, 2);
            values1.put(COLUMN_AREA, 150.0);
            values1.put(COLUMN_CONTACT_NAME, "أحمد محمد");
            values1.put(COLUMN_CONTACT_PHONE, "0551234567");
            values1.put(COLUMN_LATITUDE, 0.0);
            values1.put(COLUMN_LONGITUDE, 0.0);
            values1.put(COLUMN_IMAGES, "");

            db.insert(TABLE_PROPERTIES, null, values1);

            // ✅ إضافة عقار تجريبي ثاني
            ContentValues values2 = new ContentValues();
            values2.put(COLUMN_TITLE, "فيلا للبيع في جدة - السلام");
            values2.put(COLUMN_DESCRIPTION, "فيلا فاخرة بمساحة 300م² مع حديقة وموقف سيارات");
            values2.put(COLUMN_PRICE, 2500000.0);
            values2.put(COLUMN_TYPE, "فيلا");
            values2.put(COLUMN_OFFER_TYPE, "للبيع");
            values2.put(COLUMN_ADDRESS, "حي السلام، جدة");
            values2.put(COLUMN_BEDROOMS, 4);
            values2.put(COLUMN_BATHROOMS, 3);
            values2.put(COLUMN_AREA, 300.0);
            values2.put(COLUMN_CONTACT_NAME, "محمد علي");
            values2.put(COLUMN_CONTACT_PHONE, "0509876543");
            values2.put(COLUMN_LATITUDE, 0.0);
            values2.put(COLUMN_LONGITUDE, 0.0);
            values2.put(COLUMN_IMAGES, "");

            db.insert(TABLE_PROPERTIES, null, values2);

            android.util.Log.d("Database", "تم إضافة البيانات التجريبية بنجاح");
        } catch (Exception e) {
            android.util.Log.e("Database", "خطأ في إضافة البيانات التجريبية: " + e.getMessage());
        }
    }

    // ✅ دوال إدارة المفضلات

    // إضافة عقار إلى المفضلات
    // إضافة عقار إلى المفضلات
    public boolean addToFavorites(int propertyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROPERTY_ID, propertyId);
        values.put(COLUMN_USER_ID, 1);

        try {
            Log.d("DatabaseHelper", "=== إضافة إلى المفضلة ===");
            Log.d("DatabaseHelper", "propertyId: " + propertyId);

            // التحقق أولاً إذا كان العقار موجوداً في المفضلات
            if (isPropertyInFavorites(propertyId)) {
                Log.d("DatabaseHelper", "العقار موجود بالفعل في المفضلات");
                return true;
            }

            long result = db.insert(TABLE_FAVORITES, null, values);
            Log.d("DatabaseHelper", "نتيجة الإضافة: " + result);

            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "خطأ في إضافة المفضلة: " + e.getMessage());
            return false;
        } finally {
            // ❌ لا تغلق قاعدة البيانات هنا، دع المستدعي يتحكم
            // db.close();
        }
    }

    // إزالة عقار من المفضلات
    public boolean removeFromFavorites(int propertyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int result = db.delete(TABLE_FAVORITES,
                    COLUMN_PROPERTY_ID + " = ?",
                    new String[]{String.valueOf(propertyId)});
            android.util.Log.d("Favorites", "تم إزالة العقار " + propertyId + " من المفضلات");
            return result > 0;
        } catch (Exception e) {
            android.util.Log.e("Favorites", "خطأ في إزالة المفضلة: " + e.getMessage());
            return false;
        } finally {
            //db.close();
        }
    }

    // التحقق إذا كان العقار في المفضلات
    public boolean isPropertyInFavorites(int propertyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_FAVORITES,
                    new String[]{COLUMN_FAVORITE_ID},
                    COLUMN_PROPERTY_ID + " = ?",
                    new String[]{String.valueOf(propertyId)},
                    null, null, null);

            boolean exists = cursor.getCount() > 0;
            android.util.Log.d("Favorites", "التحقق من المفضلة " + propertyId + ": " + exists);
            return exists;
        } catch (Exception e) {
            android.util.Log.e("Favorites", "خطأ في التحقق من المفضلة: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();

        }
    }

    public void closeDatabase() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db != null && db.isOpen()) {
                //db.close();
                Log.d("DatabaseHelper", "تم إغلاق قاعدة البيانات");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "خطأ في إغلاق قاعدة البيانات: " + e.getMessage());
        }
    }

    // جلب جميع العقارات المفضلة
    public List<Property> getFavoriteProperties() {
        List<Property> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT p.* FROM " + TABLE_PROPERTIES + " p " +
                    "INNER JOIN " + TABLE_FAVORITES + " f ON p." + COLUMN_ID + " = f." + COLUMN_PROPERTY_ID +
                    " ORDER BY f." + COLUMN_ADDED_AT + " DESC";

            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                    int bedrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BEDROOMS));
                    int bathrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATHROOMS));
                    String propertyType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                    String offerType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_TYPE));
                    String images = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGES));

                    Property property = new Property();
                    property.setId(id);
                    property.setTitle(title);
                    property.setDescription(description);
                    property.setLocation(address);
                    property.setPrice(String.valueOf(price));
                    property.setBedrooms(String.valueOf(bedrooms));
                    property.setBathrooms(String.valueOf(bathrooms));
                    property.setType(propertyType);
                    property.setOfferType(offerType);

                    // تحميل مسارات الصور
                    if (images != null && !images.isEmpty()) {
                        List<String> imagePaths = Arrays.asList(images.split(","));
                        property.setImagePaths(imagePaths);
                    }

                    favorites.add(property);

                } while (cursor.moveToNext());
            }

            android.util.Log.d("Favorites", "تم جلب " + favorites.size() + " عقار من المفضلات");

        } catch (Exception e) {
            android.util.Log.e("Favorites", "خطأ في جلب المفضلات: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            //db.close();
        }

        return favorites;
    }

    // ✅ دوال العقارات الحالية (تبقى كما هي)

    // ✅ الطريقة الصحيحة - تحديث الدالة الأصلية
    public long addProperty(String title, String description, double price, String type,
                            String offerType, String address, int bedrooms, int bathrooms,
                            double area, String contactName, String contactPhone,
                            double latitude, double longitude, List<String> imagePaths) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_PRICE, price);
            values.put(COLUMN_TYPE, type);
            values.put(COLUMN_OFFER_TYPE, offerType);
            values.put(COLUMN_ADDRESS, address);
            values.put(COLUMN_BEDROOMS, bedrooms);
            values.put(COLUMN_BATHROOMS, bathrooms);
            values.put(COLUMN_AREA, area);
            values.put(COLUMN_CONTACT_NAME, contactName);
            values.put(COLUMN_CONTACT_PHONE, contactPhone);

            // ✅ استخدام الإحداثيات الجديدة
            values.put(COLUMN_LATITUDE, latitude);
            values.put(COLUMN_LONGITUDE, longitude);

            // حفظ مسارات الصور
            String imagesValue = "";
            if (imagePaths != null && !imagePaths.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String path : imagePaths) {
                    if (path != null && !path.isEmpty()) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(path);
                    }
                }
                imagesValue = sb.toString();
            }
            values.put(COLUMN_IMAGES, imagesValue);

            id = db.insert(TABLE_PROPERTIES, null, values);

            android.util.Log.d("Database", "تم إضافة عقار جديد - ID: " + id +
                    " - الاتصال: " + contactName + " - " + contactPhone +
                    " - الموقع: " + latitude + ", " + longitude);

        } catch (Exception e) {
            android.util.Log.e("Database", "خطأ في إضافة العقار: " + e.getMessage());
        } finally {
            //  db.close();
        }

        return id;
    }

    // ✅ الاحتفاظ بالدالة القديمة للتوافق (Overloading)
    public long addProperty(String title, String description, double price, String type,
                            String offerType, String address, int bedrooms, int bathrooms,
                            double area, String contactName, String contactPhone,
                            List<String> imagePaths) {
        // استدعاء الدالة الجديدة مع إحداثيات افتراضية
        return addProperty(title, description, price, type, offerType, address,
                bedrooms, bathrooms, area, contactName, contactPhone,
                0.0, 0.0, imagePaths); // ✅ إحداثيات افتراضية
    }

    public List<Property> getAllProperties() {
        List<Property> properties = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // ✅ تأكد من جلب جميع الحقول
            String selectQuery = "SELECT * FROM " + TABLE_PROPERTIES + " ORDER BY " + COLUMN_CREATED_AT + " DESC";
            cursor = db.rawQuery(selectQuery, null);

            android.util.Log.d("Database", "جاري استرجاع العقارات، العدد: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    try {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                        String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                        String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
                        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                        int bedrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BEDROOMS));
                        int bathrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATHROOMS));
                        String propertyType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                        String offerType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_TYPE));
                        String images = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGES));
                        double area = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AREA));

                        // ✅ تأكد من جلب بيانات الاتصال
                        String contactName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME));
                        String contactPhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE));

                        Property property = new Property();
                        property.setId(id);
                        property.setTitle(title);
                        property.setDescription(description);
                        property.setLocation(address);
                        property.setPrice(String.valueOf(price));
                        property.setBedrooms(String.valueOf(bedrooms));
                        property.setBathrooms(String.valueOf(bathrooms));
                        property.setType(propertyType);
                        property.setOfferType(offerType);
                        property.setArea(String.valueOf(area));

                        // ✅ تعيين بيانات الاتصال
                        property.setContactName(contactName);
                        property.setContactPhone(contactPhone);

                        if (images != null && !images.isEmpty()) {
                            List<String> imagePaths = Arrays.asList(images.split(","));
                            property.setImagePaths(imagePaths);
                        }

                        properties.add(property);

                        // ✅ تسجيل بيانات الاتصال للمراقبة
                        android.util.Log.d("Database", "العقار " + id + ": " + title + " - الهاتف: " + contactPhone);

                    } catch (Exception e) {
                        android.util.Log.e("Database", "خطأ في معالجة عقار: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("Database", "خطأ في getAllProperties: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close();
        }

        return properties;
    }

    public List<Property> getPropertiesByType(String offerType) {
        List<Property> properties = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_PROPERTIES, null,
                    COLUMN_OFFER_TYPE + " = ?",
                    new String[]{offerType},
                    null, null,
                    COLUMN_CREATED_AT + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    try {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                        String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                        String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
                        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                        int bedrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BEDROOMS));
                        int bathrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATHROOMS));
                        String propertyType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                        String images = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGES));

                        Property property = new Property();
                        property.setId(id);
                        property.setTitle(title);
                        property.setDescription(description);
                        property.setLocation(address);
                        property.setPrice(String.valueOf(price));
                        property.setBedrooms(String.valueOf(bedrooms));
                        property.setBathrooms(String.valueOf(bathrooms));
                        property.setType(propertyType);
                        property.setOfferType(offerType);

                        if (images != null && !images.isEmpty()) {
                            List<String> imagePaths = Arrays.asList(images.split(","));
                            property.setImagePaths(imagePaths);
                        }

                        properties.add(property);

                    } catch (Exception e) {
                        android.util.Log.e("Database", "خطأ في معالجة عقار: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("Database", "خطأ في getPropertiesByType: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close();
        }

        return properties;
    }

    public Property getPropertyById(int propertyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Property property = null;
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_PROPERTIES, null,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(propertyId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
                int bedrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BEDROOMS));
                int bathrooms = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATHROOMS));
                String propertyType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                String offerType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_TYPE));
                String images = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGES));

                // ✅ تأكد من استرجاع بيانات الاتصال
                String contactName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME));
                String contactPhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE));

                property = new Property();
                property.setId(id);
                property.setTitle(title);
                property.setDescription(description);
                property.setLocation(address);
                property.setPrice(String.valueOf(price));
                property.setBedrooms(String.valueOf(bedrooms));
                property.setBathrooms(String.valueOf(bathrooms));
                property.setType(propertyType);
                property.setOfferType(offerType);

                // ✅ تعيين بيانات الاتصال
                property.setContactName(contactName);
                property.setContactPhone(contactPhone);

                if (images != null && !images.isEmpty()) {
                    List<String> imagePaths = Arrays.asList(images.split(","));
                    property.setImagePaths(imagePaths);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("Database", "خطأ في getPropertyById: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close();
        }

        return property;
    }



    public int getPropertiesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PROPERTIES, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            android.util.Log.e("Database", "خطأ في getPropertiesCount: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            //  db.close();
        }

        return count;
    }

    public boolean updatePropertyImages(long propertyId, List<String> imagePaths) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();

            String imagesValue = "";
            if (imagePaths != null && !imagePaths.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String path : imagePaths) {
                    if (path != null && !path.isEmpty()) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(path);
                    }
                }
                imagesValue = sb.toString();
            }
            values.put(COLUMN_IMAGES, imagesValue);

            int rowsAffected = db.update(TABLE_PROPERTIES, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(propertyId)});

            success = (rowsAffected > 0);
            android.util.Log.d("Database", "تم تحديث صور العقار: " + propertyId + " - الصور: " + imagesValue);

        } catch (Exception e) {
            android.util.Log.e("Database", "خطأ في تحديث صور العقار: " + e.getMessage());
        } finally {
            // db.close();
        }

        return success;
    }








    // ✅ تسجيل مشاركة العقار
    public boolean logPropertyShare(int propertyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("property_id", propertyId);
        values.put("shared_at", "CURRENT_TIMESTAMP");

        try {
            long result = db.insert("property_shares", null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("Database", "Error logging share: " + e.getMessage());
            return false;
        } finally {
            // db.close();
        }
    }

    // ✅ تسجيل اتصال بالعقار
    public boolean logPropertyContact(int propertyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("property_id", propertyId);
        values.put("contacted_at", "CURRENT_TIMESTAMP");

        try {
            long result = db.insert("property_contacts", null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("Database", "Error logging contact: " + e.getMessage());
            return false;
        } finally {
            //  db.close();
        }
    }

    // ✅ حذف العقار
    public boolean deleteProperty(int propertyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // حذف من المفضلات أولاً
            db.delete(TABLE_FAVORITES, COLUMN_PROPERTY_ID + " = ?",
                    new String[]{String.valueOf(propertyId)});

            // ثم حذف العقار
            int result = db.delete(TABLE_PROPERTIES, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(propertyId)});

            return result > 0;
        } catch (Exception e) {
            Log.e("Database", "Error deleting property: " + e.getMessage());
            return false;
        } finally {
            //  db.close();
        }
    }




    // ✅ دوال إدارة التقييمات
    public boolean addPropertyRating(int propertyId, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("property_id", propertyId);
        values.put("rating", rating);
        values.put("comment", comment);
        values.put("created_at", "CURRENT_TIMESTAMP");

        try {
            long result = db.insert("property_ratings", null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e("Database", "Error adding rating: " + e.getMessage());
            return false;
        } finally {
            // db.close();
        }
    }

    // ✅ الحصول على متوسط التقييم
    public float getAverageRating(int propertyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        float averageRating = 0.0f;

        try {
            cursor = db.rawQuery(
                    "SELECT AVG(rating) FROM property_ratings WHERE property_id = ?",
                    new String[]{String.valueOf(propertyId)}
            );

            if (cursor.moveToFirst()) {
                averageRating = cursor.getFloat(0);
            }

            Log.d("DATABASE_RATING", "العقار " + propertyId + ": المتوسط = " + averageRating);

        } catch (Exception e) {
            Log.e("DATABASE_RATING", "خطأ: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return averageRating;
    }

    // ✅ الحصول على جميع التقييمات
    public List<PropertyRating> getPropertyRatings(int propertyId) {
        List<PropertyRating> ratings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM property_ratings WHERE property_id = ? ORDER BY created_at DESC",
                    new String[]{String.valueOf(propertyId)});

            if (cursor.moveToFirst()) {
                do {
                    PropertyRating rating = new PropertyRating();
                    rating.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    rating.setPropertyId(cursor.getInt(cursor.getColumnIndexOrThrow("property_id")));
                    rating.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow("rating")));
                    rating.setComment(cursor.getString(cursor.getColumnIndexOrThrow("comment")));
                    rating.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                    ratings.add(rating);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("Database", "Error getting ratings: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            //db.close();
        }
        return ratings;
    }



    // ✅ الحصول على متوسط التقييم مع عدد المقيمين
    public RatingStats getRatingStats(int propertyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        RatingStats stats = new RatingStats();

        try {
            // استعلام للحصول على المتوسط وعدد التقييمات
            String query = "SELECT " +
                    "COUNT(*) as total_ratings, " +
                    "AVG(rating) as average_rating, " +
                    "MAX(created_at) as last_rating " +
                    "FROM property_ratings " +
                    "WHERE property_id = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(propertyId)});

            if (cursor.moveToFirst()) {
                stats.setTotalRatings(cursor.getInt(0));
                stats.setAverageRating(cursor.getFloat(1));
                stats.setLastRatingDate(cursor.getString(2));
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "خطأ في جلب إحصاءات التقييم: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            // لا تغلق قاعدة البيانات هنا
        }

        return stats;
    }

    // ✅ الحصول على آخر 10 تعليقات
    public List<PropertyRating> getRecentComments(int propertyId, int limit) {
        List<PropertyRating> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM property_ratings " +
                    "WHERE property_id = ? AND comment IS NOT NULL AND comment != '' " +
                    "ORDER BY created_at DESC LIMIT ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(propertyId), String.valueOf(limit)});

            if (cursor.moveToFirst()) {
                do {
                    PropertyRating rating = new PropertyRating();
                    rating.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    rating.setPropertyId(cursor.getInt(cursor.getColumnIndexOrThrow("property_id")));
                    rating.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow("rating")));
                    rating.setComment(cursor.getString(cursor.getColumnIndexOrThrow("comment")));
                    rating.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                    comments.add(rating);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "خطأ في جلب التعليقات: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return comments;
    }

    // ✅ فئة مساعدة لإحصاءات التقييم
    public class RatingStats {
        private int totalRatings;
        private float averageRating;
        private String lastRatingDate;

        // Getters and Setters
        public int getTotalRatings() { return totalRatings; }
        public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

        public float getAverageRating() { return averageRating; }
        public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

        public String getLastRatingDate() { return lastRatingDate; }
        public void setLastRatingDate(String lastRatingDate) { this.lastRatingDate = lastRatingDate; }

        public String getFormattedAverage() {
            return String.format("%.1f", averageRating);
        }

        public boolean hasRatings() {
            return totalRatings > 0;
        }
    }

    // ✅ تسجيل إحصائية التقييم
    public boolean logPropertyRating(int propertyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("property_id", propertyId);
        values.put("rated_at", "CURRENT_TIMESTAMP");

        try {
            long result = db.insert("property_ratings_stats", null, values);
            Log.d("DatabaseHelper", "تم تسجيل إحصائية تقييم للعقار: " + propertyId);
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "خطأ في تسجيل إحصائية التقييم: " + e.getMessage());
            return false;
        }
    }


    // في DatabaseHelper.java
    public boolean isDatabaseOpen() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            boolean isOpen = db.isOpen();
            Log.d("DatabaseCheck", "قاعدة البيانات مفتوحة: " + isOpen);
            return isOpen;
        } catch (Exception e) {
            Log.e("DatabaseCheck", "خطأ في التحقق من قاعدة البيانات: " + e.getMessage());
            return false;
        }
    }
}