package com.example.collegeadmission;

public class Student {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String category;
    private double percentage;
    private String state;
    private String stream;
    private String targetCourse;
    private String preferredState;
    private int savedCount;
    private int searchCount;
    private String profileCompletion;

    public Student() {}

    public Student(String uid, String name, String email,
                   String phone, String category, double percentage) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.category = category;
        this.percentage = percentage;
        this.stream = "";
        this.targetCourse = "";
        this.preferredState = "";
        this.state = "";
        this.savedCount = 0;
        this.searchCount = 0;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCategory() { return category; }
    public double getPercentage() { return percentage; }
    public String getState() { return state; }
    public String getStream() { return stream; }
    public String getTargetCourse() { return targetCourse; }
    public String getPreferredState() { return preferredState; }
    public int getSavedCount() { return savedCount; }
    public int getSearchCount() { return searchCount; }
    public String getProfileCompletion() { return profileCompletion; }

    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCategory(String category) { this.category = category; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public void setState(String state) { this.state = state; }
    public void setStream(String stream) { this.stream = stream; }
    public void setTargetCourse(String targetCourse) { this.targetCourse = targetCourse; }
    public void setPreferredState(String preferredState) { this.preferredState = preferredState; }
    public void setSavedCount(int savedCount) { this.savedCount = savedCount; }
    public void setSearchCount(int searchCount) { this.searchCount = searchCount; }
    public void setProfileCompletion(String profileCompletion) { this.profileCompletion = profileCompletion; }

    public int calculateCompletion() {
        int score = 0;
        if (name != null && !name.isEmpty())            score += 15;
        if (email != null && !email.isEmpty())          score += 15;
        if (phone != null && !phone.isEmpty())          score += 10;
        if (category != null && !category.isEmpty())    score += 10;
        if (percentage > 0)                             score += 15;
        if (stream != null && !stream.isEmpty())        score += 10;
        if (targetCourse != null && !targetCourse.isEmpty()) score += 10;
        if (preferredState != null && !preferredState.isEmpty()) score += 10;
        if (state != null && !state.isEmpty())          score += 5;
        return score;
    }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(parts[0].charAt(0)).toUpperCase();
    }
}