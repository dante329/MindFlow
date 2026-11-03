package strategy;

import model.Question;

import java.util.HashMap;
import java.util.Map;

/**
 * 填空题判分策略
 * 支持多空填空，用户答案用分号分隔
 * 标准答案格式: {"1":"答案1","2":"答案2"}
 * 用户输入格式: 答案1;答案2
 */
public class FillBlankGrading implements GradingStrategy {

    @Override
    public boolean grade(Question question, String userAnswer) {
        if (userAnswer == null || question.getAnswer() == null) {
            return false;
        }

        Map<String, String> correctAnswers = parseJsonAnswers(question.getAnswer());
        String[] userAnswers = userAnswer.split(";");

        if (correctAnswers.isEmpty()) {
            // 简单模式：直接比较
            return question.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());
        }

        // 多空模式：逐个比较
        for (int i = 0; i < Math.min(correctAnswers.size(), userAnswers.length); i++) {
            String key = String.valueOf(i + 1);
            String correct = correctAnswers.getOrDefault(key, "").trim();
            String user = userAnswers[i].trim();
            if (!correct.equalsIgnoreCase(user)) {
                return false;
            }
        }

        return userAnswers.length >= correctAnswers.size();
    }

    @Override
    public String getCorrectAnswerDisplay(Question question) {
        Map<String, String> answers = parseJsonAnswers(question.getAnswer());
        if (answers.isEmpty()) {
            return "正确答案: " + question.getAnswer();
        }
        StringBuilder sb = new StringBuilder("正确答案:\n");
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            sb.append("  第").append(entry.getKey()).append("空: ").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String getTypeCode() {
        return "FILL_BLANK";
    }

    /**
     * 简易 JSON 解析: {"1":"xxx","2":"yyy"} → Map
     */
    private Map<String, String> parseJsonAnswers(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null || !json.startsWith("{")) {
            return result;
        }
        // 去掉花括号
        String inner = json.substring(1, json.length() - 1).trim();
        if (inner.isEmpty()) return result;

        // 按 "," 分割键值对（简化处理，不支持值中含逗号的情况）
        String[] pairs = inner.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                result.put(key, value);
            }
        }
        return result;
    }
}
