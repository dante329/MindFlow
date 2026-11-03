package model;

import java.time.LocalDateTime;

/**
 * 学习会话实体类
 * 记录每次学习的开始/结束时间和做题数量
 */
public class StudySession {

    private int sessionId;
    private int userId;
    private Integer subjectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;
    private int questionsDone;

    public StudySession() {
    }

    public StudySession(int userId, Integer subjectId) {
        this.userId = userId;
        this.subjectId = subjectId;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 结束会话，计算时长
     */
    public void end() {
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 增加做题计数
     */
    public void incrementQuestions() {
        this.questionsDone++;
    }

    // ==================== Getters & Setters ====================

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public int getQuestionsDone() {
        return questionsDone;
    }

    public void setQuestionsDone(int questionsDone) {
        this.questionsDone = questionsDone;
    }
}
