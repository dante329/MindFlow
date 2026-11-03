package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日学习计划实体类
 */
public class DailyPlan {

    private int planId;
    private int userId;
    private LocalDate planDate;
    private Integer subjectId;
    private int targetCount;
    private int actualCount;
    private boolean completed;
    private int studyMinutes;
    private String note;
    private LocalDateTime createdAt;

    public DailyPlan() {
    }

    public DailyPlan(int userId, LocalDate planDate, int targetCount) {
        this.userId = userId;
        this.planDate = planDate;
        this.targetCount = targetCount;
    }

    /**
     * 计算完成百分比
     */
    public int getProgressPercent() {
        if (targetCount <= 0) return 0;
        return Math.min(100, (actualCount * 100) / targetCount);
    }

    /**
     * 获取进度条显示
     */
    public String getProgressBar() {
        int percent = getProgressPercent();
        int filled = percent / 5; // 20格
        int empty = 20 - filled;
        return "[" + "█".repeat(filled) + "░".repeat(empty) + "] " + percent + "%";
    }

    // ==================== Getters & Setters ====================

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getActualCount() {
        return actualCount;
    }

    public void setActualCount(int actualCount) {
        this.actualCount = actualCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getStudyMinutes() {
        return studyMinutes;
    }

    public void setStudyMinutes(int studyMinutes) {
        this.studyMinutes = studyMinutes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
