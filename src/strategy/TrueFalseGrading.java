package strategy;

import model.Question;

/**
 * 判断题判分策略
 * 比较 T/F（不区分大小写）
 */
public class TrueFalseGrading implements GradingStrategy {

    @Override
    public boolean grade(Question question, String userAnswer) {
        if (userAnswer == null || question.getAnswer() == null) {
            return false;
        }
        String normalizedUser = userAnswer.trim().toUpperCase();
        String normalizedAnswer = question.getAnswer().trim().toUpperCase();

        // 支持中文输入：对/错、正确/错误
        if ("对".equals(normalizedUser) || "正确".equals(normalizedUser)) {
            normalizedUser = "T";
        } else if ("错".equals(normalizedUser) || "错误".equals(normalizedUser)) {
            normalizedUser = "F";
        }

        return normalizedAnswer.equals(normalizedUser);
    }

    @Override
    public String getCorrectAnswerDisplay(Question question) {
        String answer = question.getAnswer().trim().toUpperCase();
        return "正确答案: " + ("T".equals(answer) ? "正确 (T)" : "错误 (F)");
    }

    @Override
    public String getTypeCode() {
        return "TRUE_FALSE";
    }
}
