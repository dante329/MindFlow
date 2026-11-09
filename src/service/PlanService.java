package service;

import dao.AnswerRecordDao;
import dao.DailyPlanDao;
import model.DailyPlan;
import util.ConsoleUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日计划与打卡业务服务
 */
public class PlanService {

    private static final PlanService INSTANCE = new PlanService();
    private final DailyPlanDao planDao = DailyPlanDao.getInstance();
    private final AnswerRecordDao answerRecordDao = AnswerRecordDao.getInstance();

    public static PlanService getInstance() {
        return INSTANCE;
    }

    /**
     * 创建今日计划
     */
    public void createTodayPlan(Integer subjectId, int targetCount, String note) {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        DailyPlan plan = new DailyPlan(userId, LocalDate.now(), targetCount);
        plan.setSubjectId(subjectId);
        plan.setNote(note);
        planDao.insert(plan);
        ConsoleUtil.printSuccess("今日计划已创建: 目标 " + targetCount + " 题");
    }

    /**
     * 查看今日计划
     */
    public void showTodayPlan() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        List<DailyPlan> plans = planDao.findToday(userId);
        int todayDone = answerRecordDao.countToday(userId);
        int todayCorrect = answerRecordDao.countCorrectToday(userId);

        ConsoleUtil.printHeader("今日计划 " + LocalDate.now());

        if (plans.isEmpty()) {
            ConsoleUtil.printWarning("今天还没有制定计划");
        } else {
            for (DailyPlan plan : plans) {
                // 用今天实际答题数来显示进度，比 plan 表里的 actualCount 更准
                int actual = Math.max(plan.getActualCount(), todayDone);
                int percent = plan.getTargetCount() > 0 ? Math.min(100, actual * 100 / plan.getTargetCount()) : 0;
                int filled = percent / 5;
                String bar = "[" + "█".repeat(filled) + "░".repeat(20 - filled) + "] " + percent + "%";

                System.out.println("  目标: " + plan.getTargetCount() + " 题");
                System.out.println("  进度: " + bar);
                System.out.println("  实际完成: " + actual + " 题");
                if (actual >= plan.getTargetCount()) {
                    ConsoleUtil.printSuccess("今日目标已完成！🎉");
                }
                if (plan.getNote() != null) {
                    System.out.println("  备注: " + plan.getNote());
                }
                System.out.println();
            }
        }

        ConsoleUtil.printSeparator();
        System.out.printf("  今日答题: %d 题 | 正确: %d 题%n", todayDone, todayCorrect);
        if (todayDone > 0) {
            System.out.printf("  今日正确率: %.1f%%\n", todayCorrect * 100.0 / todayDone);
        }

        // 连续打卡天数
        int streak = planDao.countStreak(userId);
        System.out.printf("  近30天完成天数: %d 天%n", streak);
    }

    /**
     * 查看最近 N 天的计划
     */
    public void showRecentPlans(int days) {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        List<DailyPlan> plans = planDao.findRecent(userId, days);

        ConsoleUtil.printHeader("最近 " + days + " 天计划");
        if (plans.isEmpty()) {
            ConsoleUtil.printInfo("暂无计划记录");
            return;
        }

        for (DailyPlan plan : plans) {
            String status = plan.isCompleted() ? "✅" : "⬜";
            System.out.printf("  %s %s | 目标:%d | 完成:%d | %s%n",
                    status, plan.getPlanDate(), plan.getTargetCount(),
                    plan.getActualCount(), plan.getProgressBar());
        }
    }

    /**
     * 快速打卡（交互式创建计划）
     */
    public void quickCheckIn() {
        int targetCount = ConsoleUtil.readInt("今日目标题数", 1, 500);
        String note = ConsoleUtil.readLine("备注（可留空）");
        createTodayPlan(null, targetCount, note.isEmpty() ? null : note);
    }
}
