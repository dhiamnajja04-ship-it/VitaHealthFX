package tn.esprit.workshopjdbc.Entities;

import java.time.LocalDateTime;

public class Story {
    private int id;
    private int userId;
    private String userName;
    private String userRole;
    private String imageUrl;
    private String caption;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int views;
    private boolean isExpired;

    public Story() {}

    public Story(int userId, String imageUrl) {
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
        // Stories expire after 24 hours
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public boolean isExpired() { 
        return LocalDateTime.now().isAfter(expiresAt); 
    }
    public void setExpired(boolean expired) { this.isExpired = expired; }

    @Override
    public String toString() {
        return userName;
    }
}
