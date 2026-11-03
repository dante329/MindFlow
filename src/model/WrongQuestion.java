package model;

import java.time.LocalDateTime;

/**
 * 错题本实体类
 */
public class WrongQuestion {

    private int wqId;
    private int userId;
    private int questionId;
    private int wrongCount;
    private int masteryLevel; // 0=未掌握, 1=部分掌握, 2=基本掌握, 3=已掌握
    private LocalDateTime lastWrongAt;
    private LocalDateTime resolvedAt;

    public WrongQuestion() {
    }

    public WrongQuestion(int userId, int questionId) {
        this.userId = userId;
        this.questionId = questionId;
        this.wrongCount = 1;
        this.masteryLevel = 0;
    }

    /**
     * 获取掌握程度的中文描述
     */
    public String getMasteryDisplay() {
        return switch (masteryLevel) {
            case 0 -> "❌ 未掌握";
            case 1 -> "🔶 部分掌握";
            case 2 -> "🔷 基本掌握";
            case 3 -> "✅ 已掌握";
            default -> "未知";
        };
    }

    // ==================== Getters & Setters ====================

    public int getWqId() {
        return wqId;
    }

    public void setWqId(int wqId) {
        this.wqId = wqId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public int getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(int masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public LocalDateTime getLastWrongAt() {
        return lastWrongAt;
    }

    public void setLastWrongAt(LocalDateTime lastWrongAt) {
        this.lastWrongAt = lastWrongAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
