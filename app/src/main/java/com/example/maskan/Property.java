package com.example.maskan;

import java.util.ArrayList;
import java.util.List;

public class Property {
    private int id;
    private String title;
    private String location;
    private String price;
    private String bedrooms;
    private String bathrooms;
    private String type;
    private String description;
    private String contactName;
    private String contactPhone;
    private String imageUrl;
    private List<String> imagePaths;
    private String offerType; // ✅ إضافة حقل offerType المفقود
    private String saleType; // "بيع" أو "ايجار"

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    private String area;
    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }



    // ✅ Constructor فارغ
    public Property() {
        this.imagePaths = new ArrayList<>();
    }

    // ✅ Constructor مع البيانات الأساسية
    public Property(String title, String location, String price, String bedrooms, String bathrooms, String type) {
        this.title = title;
        this.location = location;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.type = type;
        this.imageUrl = "";
        this.imagePaths = new ArrayList<>();
        this.offerType = "";
    }

    // ✅ Constructor مع الصورة
    public Property(String title, String location, String price, String bedrooms, String bathrooms, String type, String imageUrl) {
        this.title = title;
        this.location = location;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.type = type;
        this.imageUrl = imageUrl;
        this.imagePaths = new ArrayList<>();
        this.offerType = "";
    }

    // ✅ Constructor جديد مع قائمة الصور
    public Property(String title, String location, String price, String bedrooms, String bathrooms, String type, List<String> imagePaths) {
        this.title = title;
        this.location = location;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.type = type;
        this.imageUrl = "";
        this.imagePaths = imagePaths != null ? imagePaths : new ArrayList<>();
        this.offerType = "";
    }

    // ✅ Getters and Setters لجميع الخصائص
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(String bedrooms) {
        this.bedrooms = bedrooms;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(String bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths != null ? imagePaths : new ArrayList<>();
    }

    // ✅ Getter و Setter لـ offerType المفقود
    public String getOfferType() {
        return offerType;
    }

    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    // ✅ دالة مساعدة لإضافة مسار صورة
    public void addImagePath(String imagePath) {
        if (this.imagePaths == null) {
            this.imagePaths = new ArrayList<>();
        }
        this.imagePaths.add(imagePath);
    }

    // ✅ دالة للتحقق من وجود صور
    public boolean hasImages() {
        return imagePaths != null && !imagePaths.isEmpty();
    }

    // ✅ دالة للحصول على أول صورة (للعرض في القوائم)
    public String getFirstImagePath() {
        if (hasImages()) {
            return imagePaths.get(0);
        }
        return null;
    }

    // ✅ دالة toString للمساعدة في debugging
    @Override
    public String toString() {
        return "Property{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", price='" + price + '\'' +
                ", type='" + type + '\'' +
                ", offerType='" + offerType + '\'' +
                ", images=" + (imagePaths != null ? imagePaths.size() : 0) +
                '}';
    }
}