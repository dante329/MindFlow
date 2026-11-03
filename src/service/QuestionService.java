package service;

import dao.QuestionDao;
import exception.BusinessException;
import factory.QuestionFactory;
import model.*;
import util.ConsoleUtil;

import java.util.List;

/**
 * 题库管理业务服务
 * 处理题目的增删改查
 */
public class QuestionService {

    private static final QuestionService INSTANCE = new QuestionService();
    private final QuestionDao questionDao = QuestionDao.getInstance();

    public static QuestionService getInstance() {
        return INSTANCE;
    }

    /**
     * 添加选择题
     */
    public Question addChoiceQuestion(int subjectId, Integer kpId, int difficulty,
                                       String content, String optA, String optB, String optC, String optD,
                                       String answer, String explanation) {
        ChoiceQuestion q = QuestionFactory.createChoice(subjectId, difficulty, content, optA, optB, optC, optD, answer);
        q.setKpId(kpId);
        q.setExplanation(explanation);
        int id = questionDao.insert(q);
        q.setQuestionId(id);
        ConsoleUtil.printSuccess("选择题添加成功 (ID: " + id + ")");
        return q;
    }

    /**
     * 添加判断题
     */
    public Question addTrueFalseQuestion(int subjectId, Integer kpId, int difficulty,
                                          String content, String answer, String explanation) {
        TrueFalseQuestion q = QuestionFactory.createTrueFalse(subjectId, difficulty, content, answer);
        q.setKpId(kpId);
        q.setExplanation(explanation);
        int id = questionDao.insert(q);
        q.setQuestionId(id);
        ConsoleUtil.printSuccess("判断题添加成功 (ID: " + id + ")");
        return q;
    }

    /**
     * 添加填空题
     */
    public Question addFillBlankQuestion(int subjectId, Integer kpId, int difficulty,
                                          String content, String answer, String explanation) {
        FillBlankQuestion q = QuestionFactory.createFillBlank(subjectId, difficulty, content, answer);
        q.setKpId(kpId);
        q.setExplanation(explanation);
        int id = questionDao.insert(q);
        q.setQuestionId(id);
        ConsoleUtil.printSuccess("填空题添加成功 (ID: " + id + ")");
        return q;
    }

    /**
     * 交互式添加题目
     */
    public void addQuestionInteractive(int subjectId) {
        ConsoleUtil.printHeader("添加题目");

        String[] types = {"选择题", "判断题", "填空题"};
        int typeChoice = ConsoleUtil.selectFromMenu(types);

        String content = ConsoleUtil.readLine("题目内容");
        int difficulty = ConsoleUtil.readInt("难度 (1-5)", 1, 5);

        // 知识点（可选）
        Integer kpId = null;
        List<KnowledgePoint> kps = SubjectService.getInstance().getKnowledgePoints(subjectId);
        if (!kps.isEmpty()) {
            if (ConsoleUtil.confirm("是否关联知识点？")) {
                SubjectService.getInstance().displayKnowledgePoints(subjectId);
                int kpChoice = ConsoleUtil.readInt("知识点编号", 1, kps.size());
                kpId = kps.get(kpChoice - 1).getKpId();
            }
        }

        String explanation = ConsoleUtil.readLine("解析（可留空）");
        if (explanation.isEmpty()) explanation = null;

        switch (typeChoice) {
            case 1 -> {
                String optA = ConsoleUtil.readLine("选项 A");
                String optB = ConsoleUtil.readLine("选项 B");
                String optC = ConsoleUtil.readLine("选项 C");
                String optD = ConsoleUtil.readLine("选项 D");
                String answer = ConsoleUtil.readLine("正确答案 (A/B/C/D)").toUpperCase();
                if (!"A".equals(answer) && !"B".equals(answer) && !"C".equals(answer) && !"D".equals(answer)) {
                    throw new BusinessException("答案必须是 A/B/C/D");
                }
                addChoiceQuestion(subjectId, kpId, difficulty, content, optA, optB, optC, optD, answer, explanation);
            }
            case 2 -> {
                String answer = ConsoleUtil.readLine("答案 (T=正确 / F=错误)").toUpperCase();
                if (!"T".equals(answer) && !"F".equals(answer)) {
                    throw new BusinessException("答案必须是 T 或 F");
                }
                addTrueFalseQuestion(subjectId, kpId, difficulty, content, answer, explanation);
            }
            case 3 -> {
                ConsoleUtil.printInfo("多个空的答案用 JSON 格式: {\"1\":\"答案1\",\"2\":\"答案2\"}");
                ConsoleUtil.printInfo("单个空直接写答案即可");
                String answer = ConsoleUtil.readLine("答案");
                addFillBlankQuestion(subjectId, kpId, difficulty, content, answer, explanation);
            }
        }
    }

    /**
     * 查看科目下的题目列表
     */
    public void listQuestions(int subjectId) {
        List<Question> questions = questionDao.findBySubjectId(subjectId);
        if (questions.isEmpty()) {
            ConsoleUtil.printInfo("该科目下暂无题目");
            return;
        }
        ConsoleUtil.printHeader("题目列表 (" + questions.size() + " 题)");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.printf("  [%d] %s | 难度:%s | ID:%d%n",
                    i + 1,
                    q.getQuestionType().getDisplayName(),
                    "★".repeat(q.getDifficulty()),
                    q.getQuestionId());
            System.out.println("      " + truncate(q.getContent(), 60));
        }
    }

    /**
     * 删除题目
     */
    public void deleteQuestion(int questionId) {
        Question q = questionDao.findById(questionId);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        if (ConsoleUtil.confirm("确认删除这道题？")) {
            questionDao.delete(questionId);
            ConsoleUtil.printSuccess("题目已删除");
        }
    }

    /**
     * 搜索题目
     */
    public List<Question> searchQuestions(int subjectId, String keyword) {
        return questionDao.search(subjectId, keyword);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
