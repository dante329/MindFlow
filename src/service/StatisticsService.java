package service;

import dao.AnswerRecordDao;
import dao.DailyPlanDao;
import dao.StudySessionDao;
import dao.WrongQuestionDao;
import model.Subject;
import util.ConsoleUtil;
import util.FileExportUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 学习统计业务服务
 * 提供各科正确率、学习时长、进步曲线等统计数据
 */
public class StatisticsService {

    private static final StatisticsService INSTANCE = new StatisticsService();
    private final AnswerRecordDao answerRecordDao = AnswerRecordDao.getInstance();
    private final StudySessionDao sessionDao = StudySessionDao.getInstance();
    private final WrongQuestionDao wrongQuestionDao = WrongQuestionDao.getInstance();
    private final DailyPlanDao planDao = DailyPlanDao.getInstance();

    public static StatisticsService getInstance() {
        return INSTANCE;
    }

    /**
     * 显示综合统计面板
     */
    public void showDashboard() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();

        ConsoleUtil.printHeader("学习统计面板");

        // 总体数据
        int totalAnswered = answerRecordDao.countByUser(userId);
        int totalCorrect = answerRecordDao.countCorrectByUser(userId);
        double overallAccuracy = totalAnswered > 0 ? (totalCorrect * 100.0 / totalAnswered) : 0;

        System.out.println("  ┌─── 总体数据 ───┐");
        System.out.printf("  │ 总答题数:  %5d  │%n", totalAnswered);
        System.out.printf("  │ 总正确数:  %5d  │%n", totalCorrect);
        System.out.printf("  │ 总正确率:  %5.1f%% │%n", overallAccuracy);
        System.out.println("  └─────────────────┘");
        System.out.println();

        // 今日数据
        int todayAnswered = answerRecordDao.countToday(userId);
        int todayCorrect = answerRecordDao.countCorrectToday(userId);
        long todayMinutes = sessionDao.sumTodayDuration(userId) / 60000;

        System.out.println("  ┌─── 今日数据 ───");
        System.out.printf("  │ 今日答题:  %5d  │%n", todayAnswered);
        System.out.printf("  │ 今日正确:  %5d  │%n", todayCorrect);
        System.out.printf("  │ 今日时长:  %5d分 │%n", todayMinutes);
        System.out.println("  └─────────────────┘");
        System.out.println();

        // 各科数据
        List<Subject> subjects = SubjectService.getInstance().getMySubjects();
        if (!subjects.isEmpty()) {
            System.out.println("  ┌─── 各科正确率 ───┐");
            for (Subject s : subjects) {
                int sTotal = answerRecordDao.countBySubject(userId, s.getSubjectId());
                int sCorrect = answerRecordDao.countCorrectBySubject(userId, s.getSubjectId());
                double sAcc = sTotal > 0 ? (sCorrect * 100.0 / sTotal) : 0;
                String bar = renderBar(sAcc, 20);
                System.out.printf("  │ %-6s %5.1f%% %s │%n", s.getName(), sAcc, bar);
            }
            System.out.println("  └────────────────────┘");
        }

        // 错题数据
        int wrongTotal = wrongQuestionDao.countByUser(userId);
        int wrongUnresolved = wrongQuestionDao.countUnresolved(userId);
        System.out.println();
        System.out.printf("  错题: %d 题 (未掌握: %d)%n", wrongTotal, wrongUnresolved);

        // 打卡数据
        int streak = planDao.countStreak(userId);
        System.out.printf("  近30天完成: %d 天%n", streak);
    }

    /**
     * 导出学习报告为 CSV
     */
    public void exportReport() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();

        try {
            // 导出各科统计
            List<Subject> subjects = SubjectService.getInstance().getMySubjects();
            String[] headers = {"科目", "总题数", "正确数", "正确率"};
            List<String[]> rows = new ArrayList<>();

            for (Subject s : subjects) {
                int total = answerRecordDao.countBySubject(userId, s.getSubjectId());
                int correct = answerRecordDao.countCorrectBySubject(userId, s.getSubjectId());
                double acc = total > 0 ? (correct * 100.0 / total) : 0;
                rows.add(new String[]{s.getName(), String.valueOf(total), String.valueOf(correct), String.format("%.1f%%", acc)});
            }

            String path = FileExportUtil.exportCsv("学习统计", headers, rows);
            ConsoleUtil.printSuccess("报告已导出到: " + path);
        } catch (IOException e) {
            ConsoleUtil.printError("导出失败: " + e.getMessage());
        }
    }

    /**
     * 渲染简易进度条
     */
    private String renderBar(double percent, int width) {
        int filled = (int) (percent / 100 * width);
        int empty = width - filled;
        return "█".repeat(Math.max(0, filled)) + "░".repeat(Math.max(0, empty));
    }
}
