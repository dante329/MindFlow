package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Question 实体类单元测试
 */
class QuestionTest {

    @Test
    void choiceQuestionFormatDisplayShouldContainOptions() {
        ChoiceQuestion q = new ChoiceQuestion(1, 3, "Java继承关键字？",
                "implements", "extends", "abstract", "interface", "B");
        String display = q.formatDisplay();
        assertTrue(display.contains("选择题"));
        assertTrue(display.contains("A. implements"));
        assertTrue(display.contains("B. extends"));
        assertTrue(display.contains("★★★"));
    }

    @Test
    void trueFalseQuestionFormatDisplayShouldContainPrompt() {
        TrueFalseQuestion q = new TrueFalseQuestion(1, 2, "Java是面向对象语言", "T");
        String display = q.formatDisplay();
        assertTrue(display.contains("判断题"));
        assertTrue(display.contains("T=正确"));
    }

    @Test
    void fillBlankQuestionFormatDisplayShouldContainPrompt() {
        FillBlankQuestion q = new FillBlankQuestion(1, 2, "Java父类是___", "Object");
        String display = q.formatDisplay();
        assertTrue(display.contains("填空题"));
        assertTrue(display.contains("分号"));
    }

    @Test
    void questionTypeCodeShouldMatchEnumName() {
        ChoiceQuestion cq = new ChoiceQuestion();
        assertEquals("CHOICE", cq.getTypeCode());

        TrueFalseQuestion tfq = new TrueFalseQuestion();
        assertEquals("TRUE_FALSE", tfq.getTypeCode());

        FillBlankQuestion fbq = new FillBlankQuestion();
        assertEquals("FILL_BLANK", fbq.getTypeCode());
    }

    @Test
    void difficultyStarsShouldMatchLevel() {
        ChoiceQuestion q = new ChoiceQuestion(1, 5, "难题", "a", "b", "c", "d", "A");
        String display = q.formatDisplay();
        assertTrue(display.contains("★★★★★"));
        assertFalse(display.contains("☆"));
    }

    @Test
    void trueFalseAnswerDisplayShouldShowChinese() {
        TrueFalseQuestion tq = new TrueFalseQuestion(1, 1, "测试", "T");
        assertTrue(tq.getAnswerDisplay().contains("正确"));

        TrueFalseQuestion fq = new TrueFalseQuestion(1, 1, "测试", "F");
        assertTrue(fq.getAnswerDisplay().contains("错误"));
    }
}
