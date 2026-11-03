package thread;

import dao.ReviewReminderDao;
import model.ReviewReminder;
import util.ConsoleUtil;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 复习提醒线程（多线程）
 * 使用 ScheduledExecutorService 定期检查到期的复习提醒
 * 体现 Thread / Runnable / 线程池的使用
 */
public class ReviewReminderThread implements Runnable {

    private final int userId;
    private final ReviewReminderDao reminderDao = ReviewReminderDao.getInstance();
    private volatile boolean running = true;
    private ScheduledExecutorService scheduler;

    public ReviewReminderThread(int userId) {
        this.userId = userId;
    }

    @Override
    public void run() {
        checkAndNotify();
    }

    /**
     * 启动定时检查（每5分钟检查一次）
     */
    public void startScheduled() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ReviewReminder-Thread");
            t.setDaemon(true); // 守护线程，主程序退出时自动结束
            return t;
        });
        scheduler.scheduleAtFixedRate(this::checkAndNotify, 0, 5, TimeUnit.MINUTES);
        ConsoleUtil.printInfo("[复习提醒] 后台线程已启动，每5分钟检查一次");
    }

    /**
     * 停止定时检查
     */
    public void stop() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        ConsoleUtil.printInfo("[复习提醒] 后台线程已停止");
    }

    /**
     * 检查并通知到期的复习任务
     */
    private void checkAndNotify() {
        if (!running) return;
        try {
            List<ReviewReminder> dueReminders = reminderDao.findDueToday(userId);
            if (!dueReminders.isEmpty()) {
                // 在控制台输出提醒（不阻塞主线程）
                System.out.println();
                ConsoleUtil.printWarning(String.format("📚 复习提醒: 你有 %d 道题需要复习！", dueReminders.size()));
                for (ReviewReminder r : dueReminders) {
                    int intervalDays = ReviewReminder.getIntervalDays(r.getReviewStage());
                    System.out.printf("    - 题目ID:%d | 第%d轮复习 | 间隔%d天%n",
                            r.getQuestionId(), r.getReviewStage(), intervalDays);
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("[复习提醒] 检查异常: " + e.getMessage());
        }
    }

    /**
     * 手动触发一次检查
     */
    public void manualCheck() {
        checkAndNotify();
    }

    public boolean isRunning() {
        return running;
    }
}
