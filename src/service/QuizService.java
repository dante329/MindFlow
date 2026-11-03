package service;

import dao.AnswerRecordDao;
import dao.QuestionDao;
import dao.ReviewReminderDao;
import dao.WrongQuestionDao;
import model.*;
import strategy.GradingStrategy;
import strategy.GradingStrategyFactory;
import util.ConsoleUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 答题引擎业务服务
 * 核心功能：随机抽题、判分、记录答题、更新错题本、创建复习提醒
 */
public class QuizService {

    private static final QuizService INSTANCE = new QuizService();
    private final QuestionDao questionDao = QuestionDao.getInstance();
    private final AnswerRecordDao answerRecordDao = AnswerRecordDao.getInstance();
    private final WrongQuestionDao wrongQuestionDao = WrongQuestionDao.getInstance();
    private final ReviewReminderDao reviewReminderDao = ReviewReminderDao.getInstance();

    public static QuizService getInstance() {
        return INSTANCE;
    }

    /**
     * 开始一轮练习
     *
     * @param subjectId 科目 ID
     * @param count     题目数量
     * @param mode      练习模式: "random" / "wrong" / "by_type"
     */
    public void startQuiz(int subjectId, int count, String mode) {
        int userId = UserService.getInstance().getCurrentUser().getUserId();

        List<Question> questions;
        switch (mode) {
            case "wrong" -> {
                // 错题重做模式
                List<WrongQuestion> wrongs = wrongQuestionDao.findUnresolved(userId);
                if (wrongs.isEmpty()) {
                    ConsoleUtil.printSuccess("太棒了！没有未掌握的错题 🎉");
                    return;
                }
                questions = wrongs.stream()
                        .limit(count)
                        .map(wq -> questionDao.findById(wq.getQuestionId()))
                        .filter(q -> q != null)
                        .toList();
                ConsoleUtil.printHeader("错题重做 (" + questions.size() + " 题)");
            }
            case "by_type" -> {
                String[] types = {"选择题", "判断题", "填空题"};
                int typeChoice = ConsoleUtil.selectFromMenu(types);
                String typeCode = switch (typeChoice) {
                    case 1 -> "CHOICE";
                    case 2 -> "TRUE_FALSE";
                    case 3 -> "FILL_BLANK";
                    default -> "CHOICE";
                };
                questions = questionDao.findByType(subjectId, typeCode);
                if (questions.size() > count) {
                    questions = questions.subList(0, count);
                }
                ConsoleUtil.printHeader(types[typeChoice - 1] + "练习 (" + questions.size() + " 题)");
            }
            default -> {
                // 随机模式
                questions = questionDao.findRandom(subjectId, count);
                ConsoleUtil.printHeader("随机练习 (" + questions.size() + " 题)");
            }
        }

        if (questions.isEmpty()) {
            ConsoleUtil.printWarning("没有足够的题目，请先添加题目");
            return;
        }

        // 逐题作答
        int correctCount = 0;
        long totalTime = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println();
            ConsoleUtil.printSeparator();
            System.out.printf("\n  第 %d/%d 题\n\n", i + 1, questions.size());
            System.out.println(q.formatDisplay());
            System.out.println();

            // 计时
            long startTime = System.currentTimeMillis();
            String userAnswer = ConsoleUtil.readLine("你的答案");
            long elapsed = System.currentTimeMillis() - startTime;

            // 判分（策略模式）
            GradingStrategy strategy = GradingStrategyFactory.getStrategy(q.getTypeCode());
            boolean isCorrect = strategy.grade(q, userAnswer);

            // 显示结果
            if (isCorrect) {
                correctCount++;
                ConsoleUtil.printSuccess("回答正确！ ✓");
            } else {
                ConsoleUtil.printError("回答错误 ✗");
                System.out.println(strategy.getCorrectAnswerDisplay(q));
            }

            // 显示解析
            if (q.getExplanation() != null && !q.getExplanation().isEmpty()) {
                ConsoleUtil.printInfo("解析: " + q.getExplanation());
            }

            // 保存答题记录
            AnswerRecord record = new AnswerRecord(userId, q.getQuestionId(), userAnswer, isCorrect, elapsed);
            answerRecordDao.insert(record);

            // 更新错题本
            if (isCorrect) {
                wrongQuestionDao.improveMastery(userId, q.getQuestionId());
            } else {
                wrongQuestionDao.addOrUpdate(userId, q.getQuestionId());
            }

            // 创建复习提醒（艾宾浩斯）
            createReviewReminder(userId, q.getQuestionId());

            totalTime += elapsed;
        }

        // 显示本轮统计
        ConsoleUtil.printSeparator();
        ConsoleUtil.printHeader("本轮统计");
        double accuracy = questions.isEmpty() ? 0 : (correctCount * 100.0 / questions.size());
        System.out.printf("  总题数: %d%n", questions.size());
        System.out.printf("  正确数: %d%n", correctCount);
        System.out.printf("  正确率: %.1f%%\n", accuracy);
        System.out.printf("  总用时: %.1f 秒%n", totalTime / 1000.0);
        System.out.printf("  平均每题: %.1f 秒%n", totalTime / 1000.0 / questions.size());

        // 更新今日计划进度
        updateDailyPlanProgress(userId, questions.size());
    }

    /**
     * 为答过的题创建艾宾浩斯复习提醒
     */
    private void createReviewReminder(int userId, int questionId) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReviewReminder reminder = new ReviewReminder(userId, questionId,
                tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE), 1);
        reviewReminderDao.insert(reminder);
    }

    /**
     * 更新今日计划的实际完成数
     */
    private void updateDailyPlanProgress(int userId, int count) {
        var plans = dao.DailyPlanDao.getInstance().findToday(userId);
        for (var plan : plans) {
            int newActual = plan.getActualCount() + count;
            dao.DailyPlanDao.getInstance().updateActualCount(plan.getPlanId(), newActual);
        }
    }

    /**
     * 查看某题详情
     */
    public void showQuestionDetail(int questionId) {
        Question q = questionDao.findById(questionId);
        if (q == null) {
            ConsoleUtil.printError("题目不存在");
            return;
        }
        ConsoleUtil.printHeader("题目详情");
        System.out.println(q.formatDisplay());
        System.out.println();
        System.out.println("  答案: " + q.getAnswer());
        if (q.getExplanation() != null) {
            System.out.println("  解析: " + q.getExplanation());
        }
    }
}
