package model;

import java.time.LocalDateTime;

/**
 * 题目抽象基类
 * 使用模板方法模式定义题目的通用行为
 * 子类：ChoiceQuestion, TrueFalseQuestion, FillBlankQuestion
 */
public abstract class Question {

    /** 题型枚举 */
    public enum Type {
        CHOICE("选择题"),
        TRUE_FALSE("判断题"),
        FILL_BLANK("填空题");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    protected int questionId;
    protected int subjectId;
    protected Integer kpId;
    protected Type questionType;
    protected int difficulty; // 1-5
    protected String content;
    protected String answer;
    protected String explanation;
    protected LocalDateTime createdAt;

    public Question() {
    }

    public Question(int subjectId, Type questionType, int difficulty, String content, String answer) {
        this.subjectId = subjectId;
        this.questionType = questionType;
        this.difficulty = difficulty;
        this.content = content;
        this.answer = answer;
    }

    /**
     * 格式化显示题目（模板方法，子类可重写）
     */
    public String formatDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(questionType.getDisplayName()).append("] ");
        sb.append("(难度:").append("★".repeat(difficulty)).append("☆".repeat(5 - difficulty)).append(") ");
        sb.append("\n").append(content);
        return sb.toString();
    }

    /**
     * 获取题型标识字符串（用于数据库存储）
     */
    public String getTypeCode() {
        return questionType.name();
    }

    // ==================== Getters & Setters ====================

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getKpId() {
        return kpId;
    }

    public void setKpId(Integer kpId) {
        this.kpId = kpId;
    }

    public Type getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Type questionType) {
        this.questionType = questionType;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
