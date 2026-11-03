package model;

import java.time.LocalDateTime;

/**
 * 科目实体类
 */
public class Subject {

    private int subjectId;
    private int userId;
    private String name;
    private String description;
    private double totalScore;
    private boolean preset;
    private LocalDateTime createdAt;

    public Subject() {
    }

    public Subject(int userId, String name, String description, double totalScore) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.totalScore = totalScore;
        this.preset = false;
    }

    // ==================== Getters & Setters ====================

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public boolean isPreset() {
        return preset;
    }

    public void setPreset(boolean preset) {
        this.preset = preset;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "subjectId=" + subjectId +
                ", name='" + name + '\'' +
                ", totalScore=" + totalScore +
                '}';
    }
}
