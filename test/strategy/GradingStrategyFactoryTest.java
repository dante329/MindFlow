package strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 判分策略工厂单元测试
 */
class GradingStrategyFactoryTest {

    @Test
    void getChoiceStrategyShouldReturnChoiceGrading() {
        GradingStrategy s = GradingStrategyFactory.getStrategy("CHOICE");
        assertInstanceOf(ChoiceGrading.class, s);
    }

    @Test
    void getTrueFalseStrategyShouldReturnTrueFalseGrading() {
        GradingStrategy s = GradingStrategyFactory.getStrategy("TRUE_FALSE");
        assertInstanceOf(TrueFalseGrading.class, s);
    }

    @Test
    void getFillBlankStrategyShouldReturnFillBlankGrading() {
        GradingStrategy s = GradingStrategyFactory.getStrategy("FILL_BLANK");
        assertInstanceOf(FillBlankGrading.class, s);
    }

    @Test
    void unknownTypeCodeShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> GradingStrategyFactory.getStrategy("UNKNOWN"));
    }
}
