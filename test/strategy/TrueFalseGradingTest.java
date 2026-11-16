package strategy;

import model.TrueFalseQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 判断题判分策略单元测试
 */
class TrueFalseGradingTest {

    private TrueFalseGrading grading;
    private TrueFalseQuestion trueQuestion;
    private TrueFalseQuestion falseQuestion;

    @BeforeEach
    void setUp() {
        grading = new TrueFalseGrading();
        trueQuestion = new TrueFalseQuestion();
        trueQuestion.setSubjectId(1);
        trueQuestion.setDifficulty(2);
        trueQuestion.setContent("Java是面向对象语言");
        trueQuestion.setAnswer("T");

        falseQuestion = new TrueFalseQuestion();
        falseQuestion.setSubjectId(1);
        falseQuestion.setDifficulty(2);
        falseQuestion.setContent("Python是编译型语言");
        falseQuestion.setAnswer("F");
    }

    @Test
    void correctTrueShouldReturnTrue() {
        assertTrue(grading.grade(trueQuestion, "T"));
    }

    @Test
    void correctFalseShouldReturnTrue() {
        assertTrue(grading.grade(falseQuestion, "F"));
    }

    @Test
    void wrongAnswerShouldReturnFalse() {
        assertFalse(grading.grade(trueQuestion, "F"));
    }

    @Test
    void chineseDuiShouldMapToTrue() {
        assertTrue(grading.grade(trueQuestion, "对"));
    }

    @Test
    void chineseCuoShouldMapToFalse() {
        assertTrue(grading.grade(falseQuestion, "错"));
    }

    @Test
    void chineseZhengqueShouldMapToTrue() {
        assertTrue(grading.grade(trueQuestion, "正确"));
    }

    @Test
    void chineseCuowuShouldMapToFalse() {
        assertTrue(grading.grade(falseQuestion, "错误"));
    }

    @Test
    void caseInsensitiveShouldPass() {
        assertTrue(grading.grade(trueQuestion, "t"));
    }

    @Test
    void nullUserAnswerShouldReturnFalse() {
        assertFalse(grading.grade(trueQuestion, null));
    }

    @Test
    void typeCodeShouldBeTRUE_FALSE() {
        assertEquals("TRUE_FALSE", grading.getTypeCode());
    }
}
