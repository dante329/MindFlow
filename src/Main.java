import exception.BusinessException;
import model.Subject;
import model.User;
import service.*;
import thread.ReviewReminderThread;
import util.ConsoleUtil;
import util.DbUtil;

/**
 * MindFlow 考研备考复习系统 - 主入口
 *
 * 功能模块:
 * 1. 用户注册/登录
 * 2. 科目管理（含预设考研四科）
 * 3. 题库管理（选择题/判断题/填空题）
 * 4. 答题练习（随机/按题型/错题重做）
 * 5. 错题本管理
 * 6. 每日计划与打卡
 * 7. 学习统计与导出
 * 8. 艾宾浩斯复习提醒（多线程）
 *
 * 设计模式:
 * - 单例模式: DbUtil, 各 Service, 各 Dao
 * - 工厂模式: QuestionFactory
 * - 策略模式: GradingStrategy + GradingStrategyFactory
 * - 模板方法: Question.formatDisplay()
 *
 * @author G01 - 何冠宇
 * @version 1.0
 */
public class Main {

    private static ReviewReminderThread reminderThread;

    public static void main(String[] args) {
        try {
            // 初始化数据库
            DbUtil.getInstance().initDatabase();

            ConsoleUtil.printHeader("MindFlow 考研备考复习系统");
            System.out.println("  欢迎使用 MindFlow！祝你考研顺利 🎓");
            System.out.println();

            // 主循环
            boolean running = true;
            while (running) {
                if (!UserService.getInstance().isLoggedIn()) {
                    running = showAuthMenu();
                } else {
                    running = showMainMenu();
                }
            }

            // 清理资源
            cleanup();
            ConsoleUtil.printInfo("感谢使用 MindFlow，再见！👋");

        } catch (Exception e) {
            System.err.println("\n[致命错误] " + e.getMessage());
            e.printStackTrace();
            cleanup();
        }
    }

    /**
     * 认证菜单（未登录时显示）
     */
    private static boolean showAuthMenu() {
        String[] options = {"注册新账号", "登录", "退出程序"};
        int choice = ConsoleUtil.selectFromMenu(options);

        try {
            switch (choice) {
                case 1 -> handleRegister();
                case 2 -> handleLogin();
                case 3 -> {
                    return false;
                }
            }
        } catch (BusinessException e) {
            ConsoleUtil.printError(e.getMessage());
        }

        return true;
    }

    /**
     * 主菜单（已登录时显示）
     */
    private static boolean showMainMenu() {
        User user = UserService.getInstance().getCurrentUser();
        System.out.println();
        ConsoleUtil.printInfo("当前用户: " + user.getNickname() + " | 今日已答: " +
                dao.AnswerRecordDao.getInstance().countToday(user.getUserId()) + " 题");

        String[] options = {
                "📝 答题练习",
                "📚 题库管理",
                "📖 错题本",
                "📅 每日计划",
                "📊 学习统计",
                "🏷️  科目管理",
                "👤 个人中心",
                "🔄 检查复习提醒",
                "🚪 退出登录",
                "❌ 退出程序"
        };

        int choice = ConsoleUtil.selectFromMenu(options);

        try {
            switch (choice) {
                case 1 -> handleQuiz();
                case 2 -> handleQuestionManage();
                case 3 -> handleWrongBook();
                case 4 -> handleDailyPlan();
                case 5 -> handleStatistics();
                case 6 -> handleSubjectManage();
                case 7 -> handleProfile();
                case 8 -> handleReviewCheck();
                case 9 -> {
                    cleanup();
                    UserService.getInstance().logout();
                }
                case 10 -> {
                    cleanup();
                    return false;
                }
            }
        } catch (BusinessException e) {
            ConsoleUtil.printError(e.getMessage());
        } catch (Exception e) {
            ConsoleUtil.printError("操作异常: " + e.getMessage());
        }

        return true;
    }

    // ==================== 认证相关 ====================

    private static void handleRegister() {
        ConsoleUtil.printHeader("注册");
        String username = ConsoleUtil.readLine("用户名");
        String password = ConsoleUtil.readPassword("密码");
        String confirmPassword = ConsoleUtil.readPassword("确认密码");
        if (!password.equals(confirmPassword)) {
            throw new BusinessException("两次密码不一致");
        }
        String nickname = ConsoleUtil.readLine("昵称");

        User user = UserService.getInstance().register(username, password, nickname);

        // 可选：填写考研信息
        if (ConsoleUtil.confirm("是否现在填写考研目标信息？")) {
            String school = ConsoleUtil.readLine("目标院校");
            String major = ConsoleUtil.readLine("目标专业");
            String examDate = ConsoleUtil.readLine("考试日期 (yyyy-MM-dd)");
            UserService.getInstance().updateProfile(nickname, school, major, examDate);
        }

        // 自动登录
        UserService.getInstance().login(username, password);
        startReminderThread();
        // 首次注册自动导入示例题目
        SampleDataService.getInstance().initIfEmpty();
    }

    private static void handleLogin() {
        ConsoleUtil.printHeader("登录");
        String username = ConsoleUtil.readLine("用户名");
        String password = ConsoleUtil.readPassword("密码");
        UserService.getInstance().login(username, password);
        startReminderThread();
        // 登录后检查是否需要导入示例数据
        SampleDataService.getInstance().initIfEmpty();
    }

    // ==================== 答题练习 ====================

    private static void handleQuiz() {
        Subject subject = SubjectService.getInstance().selectSubject();

        String[] modes = {"随机练习", "按题型练习", "错题重做"};
        int modeChoice = ConsoleUtil.selectFromMenu(modes);

        String mode = switch (modeChoice) {
            case 1 -> "random";
            case 2 -> "by_type";
            case 3 -> "wrong";
            default -> "random";
        };

        int count = ConsoleUtil.readInt("题目数量", 1, 100);
        QuizService.getInstance().startQuiz(subject.getSubjectId(), count, mode);
        ConsoleUtil.pause();
    }

    // ==================== 题库管理 ====================

    private static void handleQuestionManage() {
        Subject subject = SubjectService.getInstance().selectSubject();

        String[] options = {"查看题目列表", "添加题目", "搜索题目", "返回"};
        int choice = ConsoleUtil.selectFromMenu(options);

        switch (choice) {
            case 1 -> {
                QuestionService.getInstance().listQuestions(subject.getSubjectId());
                ConsoleUtil.pause();
            }
            case 2 -> {
                QuestionService.getInstance().addQuestionInteractive(subject.getSubjectId());
                ConsoleUtil.pause();
            }
            case 3 -> {
                String keyword = ConsoleUtil.readLine("搜索关键词");
                var results = QuestionService.getInstance().searchQuestions(subject.getSubjectId(), keyword);
                ConsoleUtil.printInfo("找到 " + results.size() + " 道相关题目");
                for (var q : results) {
                    System.out.printf("  [ID:%d] %s | %s%n", q.getQuestionId(),
                            q.getQuestionType().getDisplayName(),
                            q.getContent().length() > 40 ? q.getContent().substring(0, 40) + "..." : q.getContent());
                }
                ConsoleUtil.pause();
            }
        }
    }

    // ==================== 错题本 ====================

    private static void handleWrongBook() {
        String[] options = {"查看所有错题", "查看未掌握错题", "错题统计", "返回"};
        int choice = ConsoleUtil.selectFromMenu(options);

        switch (choice) {
            case 1 -> {
                WrongBookService.getInstance().listWrongQuestions();
                ConsoleUtil.pause();
            }
            case 2 -> {
                WrongBookService.getInstance().listUnresolved();
                ConsoleUtil.pause();
            }
            case 3 -> {
                WrongBookService.getInstance().showSummary();
                ConsoleUtil.pause();
            }
        }
    }

    // ==================== 每日计划 ====================

    private static void handleDailyPlan() {
        String[] options = {"今日概览", "制定今日计划", "最近7天记录", "返回"};
        int choice = ConsoleUtil.selectFromMenu(options);

        switch (choice) {
            case 1 -> {
                PlanService.getInstance().showTodayPlan();
                ConsoleUtil.pause();
            }
            case 2 -> {
                PlanService.getInstance().quickCheckIn();
                ConsoleUtil.pause();
            }
            case 3 -> {
                PlanService.getInstance().showRecentPlans(7);
                ConsoleUtil.pause();
            }
        }
    }

    // ==================== 学习统计 ====================

    private static void handleStatistics() {
        String[] options = {"统计面板", "导出报告", "返回"};
        int choice = ConsoleUtil.selectFromMenu(options);

        switch (choice) {
            case 1 -> {
                StatisticsService.getInstance().showDashboard();
                ConsoleUtil.pause();
            }
            case 2 -> {
                StatisticsService.getInstance().exportReport();
                ConsoleUtil.pause();
            }
        }
    }

    // ==================== 科目管理 ====================

    private static void handleSubjectManage() {
        String[] options = {"查看科目", "添加科目", "管理知识点", "返回"};
        int choice = ConsoleUtil.selectFromMenu(options);

        switch (choice) {
            case 1 -> {
                SubjectService.getInstance().displaySubjects();
                ConsoleUtil.pause();
            }
            case 2 -> {
                String name = ConsoleUtil.readLine("科目名称");
                String desc = ConsoleUtil.readLine("描述");
                double score = ConsoleUtil.readInt("满分");
                SubjectService.getInstance().addSubject(name, desc, score);
                ConsoleUtil.pause();
            }
            case 3 -> {
                Subject subject = SubjectService.getInstance().selectSubject();
                SubjectService.getInstance().displayKnowledgePoints(subject.getSubjectId());
                if (ConsoleUtil.confirm("添加新知识点？")) {
                    String kpName = ConsoleUtil.readLine("知识点名称");
                    SubjectService.getInstance().addKnowledgePoint(subject.getSubjectId(), null, kpName);
                }
                ConsoleUtil.pause();
            }
        }
    }

    // ==================== 个人中心 ====================

    private static void handleProfile() {
        User user = UserService.getInstance().getCurrentUser();
        ConsoleUtil.printHeader("个人中心");
        System.out.println("  用户名:   " + user.getUsername());
        System.out.println("  昵称:     " + user.getNickname());
        System.out.println("  目标院校: " + (user.getTargetSchool() != null ? user.getTargetSchool() : "未设置"));
        System.out.println("  目标专业: " + (user.getTargetMajor() != null ? user.getTargetMajor() : "未设置"));
        System.out.println("  考试日期: " + (user.getExamDate() != null ? user.getExamDate() : "未设置"));
        System.out.println();

        String[] options = {"修改个人信息", "修改密码", "返回"};
        int choice = ConsoleUtil.selectFromMenu(options);

        switch (choice) {
            case 1 -> {
                String nickname = ConsoleUtil.readLine("新昵称（留空不修改）");
                String school = ConsoleUtil.readLine("目标院校");
                String major = ConsoleUtil.readLine("目标专业");
                String examDate = ConsoleUtil.readLine("考试日期 (yyyy-MM-dd)");
                UserService.getInstance().updateProfile(
                        nickname.isEmpty() ? null : nickname, school, major, examDate);
                ConsoleUtil.pause();
            }
            case 2 -> {
                String oldPwd = ConsoleUtil.readPassword("原密码");
                String newPwd = ConsoleUtil.readPassword("新密码");
                String confirmPwd = ConsoleUtil.readPassword("确认新密码");
                if (!newPwd.equals(confirmPwd)) {
                    throw new BusinessException("两次密码不一致");
                }
                UserService.getInstance().changePassword(oldPwd, newPwd);
                ConsoleUtil.pause();
            }
        }
    }

    // ==================== 复习提醒 ====================

    private static void handleReviewCheck() {
        if (reminderThread != null) {
            reminderThread.manualCheck();
        }
        int dueCount = dao.ReviewReminderDao.getInstance().countDue(
                UserService.getInstance().getCurrentUser().getUserId());
        ConsoleUtil.printInfo("当前待复习题目: " + dueCount + " 道");
        ConsoleUtil.pause();
    }

    // ==================== 辅助方法 ====================

    private static void startReminderThread() {
        if (reminderThread == null || !reminderThread.isRunning()) {
            int userId = UserService.getInstance().getCurrentUser().getUserId();
            reminderThread = new ReviewReminderThread(userId);
            reminderThread.startScheduled();
        }
    }

    private static void cleanup() {
        if (reminderThread != null) {
            reminderThread.stop();
            reminderThread = null;
        }
    }
}
