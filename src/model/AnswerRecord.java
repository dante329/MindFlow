package model;

import java.time.LocalDateTime;

/**
 * 答题记录实体类
 */
public class AnswerRecord {

    private int recordId;
    private int userId;
    private int questionId;
    private String userAnswer;
    private boolean correct;
    private long timeSpentMs;
    private LocalDateTime answeredAt;

    public AnswerRecord() {
    }

    public AnswerRecord(int userId, int questionId, String userAnswer, boolean correct, long timeSpentMs) {
        this.userId = userId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.correct = correct;
        this.timeSpentMs = timeSpentMs;
    }

    // ==================== Getters & Setters ====================

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
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

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public long getTimeSpentMs() {
        return timeSpentMs;
    }

    public void setTimeSpentMs(long timeSpentMs) {
        this.timeSpentMs = timeSpentMs;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    @Override
    public String toString() {
        return "AnswerRecord{" +
                "questionId=" + questionId +
                ", answer='" + userAnswer + '\'' +
                ", correct=" + (correct ? "✓" : "✗") +
                ", time=" + timeSpentMs + "ms" +
                '}';
    }
}
