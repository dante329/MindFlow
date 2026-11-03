package model;

/**
 * 选择题实体类
 * 继承 Question，增加四个选项字段
 */
public class ChoiceQuestion extends Question {

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    public ChoiceQuestion() {
        this.questionType = Type.CHOICE;
    }

    public ChoiceQuestion(int subjectId, int difficulty, String content,
                          String optionA, String optionB, String optionC, String optionD,
                          String answer) {
        super(subjectId, Type.CHOICE, difficulty, content, answer);
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
    }

    @Override
    public String formatDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.formatDisplay()).append("\n");
        sb.append("  A. ").append(optionA).append("\n");
        sb.append("  B. ").append(optionB).append("\n");
        sb.append("  C. ").append(optionC).append("\n");
        sb.append("  D. ").append(optionD);
        return sb.toString();
    }

    // ==================== Getters & Setters ====================

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }
}
