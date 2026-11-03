package model;

/**
 * 判断题实体类
 * 继承 Question，答案为 "T"(正确) 或 "F"(错误)
 */
public class TrueFalseQuestion extends Question {

    public TrueFalseQuestion() {
        this.questionType = Type.TRUE_FALSE;
    }

    public TrueFalseQuestion(int subjectId, int difficulty, String content, String answer) {
        super(subjectId, Type.TRUE_FALSE, difficulty, content, answer);
    }

    @Override
    public String formatDisplay() {
        return super.formatDisplay() + "\n  请判断上述说法是否正确 (T=正确 / F=错误)";
    }

    /**
     * 获取答案的中文描述
     */
    public String getAnswerDisplay() {
        if ("T".equalsIgnoreCase(answer)) {
            return "正确 ✓";
        } else if ("F".equalsIgnoreCase(answer)) {
            return "错误 ✗";
        }
        return answer;
    }
}
