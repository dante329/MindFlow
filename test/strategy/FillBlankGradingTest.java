package strategy;

import model.FillBlankQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 填空题判分策略单元测试
 */
class FillBlankGradingTest {

    private FillBlankGrading grading;

    @BeforeEach
    void setUp() {
        grading = new FillBlankGrading();
    }

    @Test
    void singleBlankCorrectShouldReturnTrue() {
        FillBlankQuestion q = new FillBlankQuestion(1, 2, "Java的父类是___", "Object");
        assertTrue(grading.grade(q, "Object"));
    }

    @Test
    void singleBlankCaseInsensitiveShouldPass() {
        FillBlankQuestion q = new FillBlankQuestion(1, 2, "Java的父类是___", "Object");
        assertTrue(grading.grade(q, "object"));
    }

    @Test
    void singleBlankWrongShouldReturnFalse() {
        FillBlankQuestion q = new FillBlankQuestion(1, 2, "Java的父类是___", "Object");
        assertFalse(grading.grade(q, "String"));
    }

    @Test
    void multiBlankAllCorrectShouldReturnTrue() {
        FillBlankQuestion q = new FillBlankQuestion(1, 3, "填空",
                "{\"1\":\"马克思主义\",\"2\":\"毛泽东思想\"}");
        assertTrue(grading.grade(q, "马克思主义;毛泽东思想"));
    }

    @Test
    void multiBlankPartialWrongShouldReturnFalse() {
        FillBlankQuestion q = new FillBlankQuestion(1, 3, "填空",
                "{\"1\":\"马克思主义\",\"2\":\"毛泽东思想\"}");
        assertFalse(grading.grade(q, "马克思主义;邓小平理论"));
    }

    @Test
    void multiBlankMissingAnswerShouldReturnFalse() {
        FillBlankQuestion q = new FillBlankQuestion(1, 3, "填空",
                "{\"1\":\"马克思主义\",\"2\":\"毛泽东思想\"}");
        assertFalse(grading.grade(q, "马克思主义"));
    }

    @Test
    void nullUserAnswerShouldReturnFalse() {
        FillBlankQuestion q = new FillBlankQuestion(1, 2, "填空", "Object");
        assertFalse(grading.grade(q, null));
    }

    @Test
    void typeCodeShouldBeFILL_BLANK() {
        assertEquals("FILL_BLANK", grading.getTypeCode());
    }
}
