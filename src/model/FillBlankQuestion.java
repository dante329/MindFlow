package model;

/**
 * 填空题实体类
 * 继承 Question，答案可能包含多个空，用 JSON 格式存储
 * 例如: {"1":"马克思主义","2":"毛泽东思想"}
 */
public class FillBlankQuestion extends Question {

    public FillBlankQuestion() {
        this.questionType = Type.FILL_BLANK;
    }

    public FillBlankQuestion(int subjectId, int difficulty, String content, String answer) {
        super(subjectId, Type.FILL_BLANK, difficulty, content, answer);
    }

    @Override
    public String formatDisplay() {
        return super.formatDisplay() + "\n  请填写空白处的答案（多个空用分号 ; 分隔）";
    }

    /**
     * 获取空白数量（简单解析 JSON 中的键数量）
     */
    public int getBlankCount() {
        if (answer == null || answer.isEmpty()) {
            return 0;
        }
        // 简单计数：统计 "数字": 的模式
        int count = 0;
        for (int i = 0; i < answer.length(); i++) {
            if (answer.charAt(i) == '"') {
                int j = i + 1;
                while (j < answer.length() && Character.isDigit(answer.charAt(j))) {
                    j++;
                }
                if (j > i + 1 && j < answer.length() && answer.charAt(j) == '"') {
                    count++;
                }
                i = j;
            }
        }
        return Math.max(count, 1);
    }
}
