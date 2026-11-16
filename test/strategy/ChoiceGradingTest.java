package strategy;

import model.ChoiceQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 选择题判分策略单元测试
 */
class ChoiceGradingTest {

    private ChoiceGrading grading;
    private ChoiceQuestion question;

    @BeforeEach
    void setUp() {
        grading = new ChoiceGrading();
        question = new ChoiceQuestion(1, 3, "Java中哪个关键字用于继承？",
                "implements", "extends", "abstract", "interface", "B");
    }

    @Test
    void correctAnswerShouldReturnTrue() {
        assertTrue(grading.grade(question, "B"));
    }

    @Test
    void wrongAnswerShouldReturnFalse() {
        assertFalse(grading.grade(question, "A"));
    }

    @Test
    void caseInsensitiveShouldPass() {
        assertTrue(grading.grade(question, "b"));
    }

    @Test
    void nullUserAnswerShouldReturnFalse() {
        assertFalse(grading.grade(question, null));
    }

    @Test
    void whitespaceAroundAnswerShouldBeTrimmed() {
        assertTrue(grading.grade(question, "  B  "));
    }

    @Test
    void typeCodeShouldBeCHOICE() {
        assertEquals("CHOICE", grading.getTypeCode());
    }

    @Test
    void correctAnswerDisplayShouldContainAnswer() {
        String display = grading.getCorrectAnswerDisplay(question);
        assertTrue(display.contains("B"));
    }
}
