package strategy;

import model.Question;

/**
 * 判分策略接口（策略模式）
 * 不同题型有不同的判分逻辑
 */
public interface GradingStrategy {

    /**
     * 判断用户答案是否正确
     *
     * @param question   题目对象
     * @param userAnswer 用户输入的答案
     * @return 是否正确
     */
    boolean grade(Question question, String userAnswer);

    /**
     * 获取正确答案的显示文本
     */
    String getCorrectAnswerDisplay(Question question);

    /**
     * 获取该策略对应的题型代码
     */
    String getTypeCode();
}
