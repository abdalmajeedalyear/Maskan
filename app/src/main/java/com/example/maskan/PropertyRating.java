package com.example.maskan;

public class PropertyRating {
    private int id;
    private int propertyId;
    private float rating;
    private String comment;
    private String createdAt;

    // Constructors
    public PropertyRating() {}

    public PropertyRating(int propertyId, float rating, String comment) {
        this.propertyId = propertyId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}