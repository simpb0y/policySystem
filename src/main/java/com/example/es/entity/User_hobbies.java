package com.example.es.entity;


import lombok.Data;

@Data
public class User_hobbies {
    private String userId;

    private String hobbiesId;

    public User_hobbies(String userId, String hobbiesId) {
        this.userId = userId;
        this.hobbiesId = hobbiesId;
    }

    public User_hobbies() {
        super();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getHobbiesId() {
        return hobbiesId;
    }

    public void setHobbiesId(String hobbiesId) {
        this.hobbiesId = hobbiesId == null ? null : hobbiesId.trim();
    }
}