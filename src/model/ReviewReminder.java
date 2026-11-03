package model;

/**
 * 复习提醒实体类（艾宾浩斯遗忘曲线）
 */
public class ReviewReminder {

    private int reminderId;
    private int userId;
    private int questionId;
    private String reviewDate;
    private int reviewStage; // 1-7 对应艾宾浩斯的7个复习周期
    private boolean reviewed;

    public ReviewReminder() {
    }

    public ReviewReminder(int userId, int questionId, String reviewDate, int reviewStage) {
        this.userId = userId;
        this.questionId = questionId;
        this.reviewDate = reviewDate;
        this.reviewStage = reviewStage;
        this.reviewed = false;
    }

    /**
     * 获取当前复习阶段对应的天数间隔
     * 艾宾浩斯遗忘曲线: 1天, 2天, 4天, 7天, 15天, 30天, 90天
     */
    public static int getIntervalDays(int stage) {
        return switch (stage) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            case 4 -> 7;
            case 5 -> 15;
            case 6 -> 30;
            case 7 -> 90;
            default -> 0;
        };
    }

    public static final int MAX_STAGE = 7;

    // ==================== Getters & Setters ====================

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
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

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public int getReviewStage() {
        return reviewStage;
    }

    public void setReviewStage(int reviewStage) {
        this.reviewStage = reviewStage;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }
}
