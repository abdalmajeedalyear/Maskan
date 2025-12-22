package com.example.maskan;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String profileImage;
    private String createdAt;

    // Constructors
    public User() {}

    public User(int id, String fullName, String email, String phone, String profileImage) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.profileImage = profileImage;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}