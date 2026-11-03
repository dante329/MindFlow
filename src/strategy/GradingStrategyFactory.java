package strategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 判分策略工厂
 * 根据题型代码返回对应的判分策略
 */
public class GradingStrategyFactory {

    private static final Map<String, GradingStrategy> STRATEGIES = new HashMap<>();

    static {
        register(new ChoiceGrading());
        register(new TrueFalseGrading());
        register(new FillBlankGrading());
    }

    private static void register(GradingStrategy strategy) {
        STRATEGIES.put(strategy.getTypeCode(), strategy);
    }

    /**
     * 根据题型代码获取判分策略
     *
     * @param typeCode 题型代码 (CHOICE / TRUE_FALSE / FILL_BLANK)
     * @return 对应的判分策略
     * @throws IllegalArgumentException 未知题型
     */
    public static GradingStrategy getStrategy(String typeCode) {
        GradingStrategy strategy = STRATEGIES.get(typeCode);
        if (strategy == null) {
            throw new IllegalArgumentException("未知的题型代码: " + typeCode);
        }
        return strategy;
    }
}
