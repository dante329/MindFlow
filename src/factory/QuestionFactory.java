package factory;

import model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * 题目工厂类（工厂模式）
 * 根据题型创建对应的 Question 子类实例
 * 也负责从 ResultSet 构建对象
 */
public class QuestionFactory {

    private QuestionFactory() {
        // 工厂类不允许实例化
    }

    /**
     * 创建选择题
     */
    public static ChoiceQuestion createChoice(int subjectId, int difficulty, String content,
                                               String optA, String optB, String optC, String optD,
                                               String answer) {
        return new ChoiceQuestion(subjectId, difficulty, content, optA, optB, optC, optD, answer);
    }

    /**
     * 创建判断题
     */
    public static TrueFalseQuestion createTrueFalse(int subjectId, int difficulty, String content, String answer) {
        return new TrueFalseQuestion(subjectId, difficulty, content, answer);
    }

    /**
     * 创建填空题
     */
    public static FillBlankQuestion createFillBlank(int subjectId, int difficulty, String content, String answer) {
        return new FillBlankQuestion(subjectId, difficulty, content, answer);
    }

    /**
     * 从 ResultSet 创建 Question 子类（多态）
     * 这是 DAO 层使用的核心方法
     */
    public static Question createFromResultSet(ResultSet rs) throws SQLException {
        String type = rs.getString("question_type");
        Question question;

        switch (type) {
            case "CHOICE" -> {
                ChoiceQuestion cq = new ChoiceQuestion();
                cq.setOptionA(rs.getString("option_a"));
                cq.setOptionB(rs.getString("option_b"));
                cq.setOptionC(rs.getString("option_c"));
                cq.setOptionD(rs.getString("option_d"));
                question = cq;
            }
            case "TRUE_FALSE" -> question = new TrueFalseQuestion();
            case "FILL_BLANK" -> question = new FillBlankQuestion();
            default -> throw new IllegalArgumentException("未知题型: " + type);
        }

        // 填充公共字段
        question.setQuestionId(rs.getInt("question_id"));
        question.setSubjectId(rs.getInt("subject_id"));
        int kpId = rs.getInt("kp_id");
        question.setKpId(rs.wasNull() ? null : kpId);
        question.setDifficulty(rs.getInt("difficulty"));
        question.setContent(rs.getString("content"));
        question.setAnswer(rs.getString("answer"));
        question.setExplanation(rs.getString("explanation"));

        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            question.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }

        return question;
    }
}
