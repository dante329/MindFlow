package strategy;

import model.Question;

/**
 * 选择题判分策略
 * 比较用户选择的字母 (A/B/C/D) 与正确答案
 */
public class ChoiceGrading implements GradingStrategy {

    @Override
    public boolean grade(Question question, String userAnswer) {
        if (userAnswer == null || question.getAnswer() == null) {
            return false;
        }
        return question.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());
    }

    @Override
    public String getCorrectAnswerDisplay(Question question) {
        return "正确答案: " + question.getAnswer();
    }

    @Override
    public String getTypeCode() {
        return "CHOICE";
    }
}
