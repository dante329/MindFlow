package service;

import dao.QuestionDao;
import dao.WrongQuestionDao;
import model.Question;
import model.WrongQuestion;
import util.ConsoleUtil;

import java.util.List;

/**
 * 错题本业务服务
 */
public class WrongBookService {

    private static final WrongBookService INSTANCE = new WrongBookService();
    private final WrongQuestionDao wrongQuestionDao = WrongQuestionDao.getInstance();
    private final QuestionDao questionDao = QuestionDao.getInstance();

    public static WrongBookService getInstance() {
        return INSTANCE;
    }

    /**
     * 查看所有错题
     */
    public void listWrongQuestions() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        List<WrongQuestion> wrongs = wrongQuestionDao.findByUserId(userId);

        if (wrongs.isEmpty()) {
            ConsoleUtil.printSuccess("错题本为空，继续保持！ 🎉");
            return;
        }

        ConsoleUtil.printHeader("我的错题本 (" + wrongs.size() + " 题)");
        for (int i = 0; i < wrongs.size(); i++) {
            WrongQuestion wq = wrongs.get(i);
            Question q = questionDao.findById(wq.getQuestionId());
            if (q == null) continue;

            System.out.printf("  [%d] %s | 错%d次 | %s%n",
                    i + 1,
                    q.getQuestionType().getDisplayName(),
                    wq.getWrongCount(),
                    wq.getMasteryDisplay());
            System.out.println("      " + truncate(q.getContent(), 50));
        }
    }

    /**
     * 查看未掌握的错题
     */
    public void listUnresolved() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        List<WrongQuestion> unresolved = wrongQuestionDao.findUnresolved(userId);
        int total = wrongQuestionDao.countByUser(userId);

        ConsoleUtil.printInfo(String.format("错题总数: %d | 未掌握: %d | 已掌握: %d",
                total, unresolved.size(), total - unresolved.size()));

        if (unresolved.isEmpty()) {
            ConsoleUtil.printSuccess("所有错题都已掌握！");
            return;
        }

        for (int i = 0; i < unresolved.size(); i++) {
            WrongQuestion wq = unresolved.get(i);
            Question q = questionDao.findById(wq.getQuestionId());
            if (q == null) continue;
            System.out.printf("  [%d] 错%d次 | %s | %s%n",
                    i + 1, wq.getWrongCount(), wq.getMasteryDisplay(),
                    truncate(q.getContent(), 40));
        }
    }

    /**
     * 错题统计摘要
     */
    public void showSummary() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        int total = wrongQuestionDao.countByUser(userId);
        int unresolved = wrongQuestionDao.countUnresolved(userId);

        ConsoleUtil.printHeader("错题统计");
        System.out.printf("  总错题数: %d%n", total);
        System.out.printf("  未掌握:   %d%n", unresolved);
        System.out.printf("  已掌握:   %d%n", total - unresolved);
        if (total > 0) {
            System.out.printf("  掌握率:   %.1f%%\n", (total - unresolved) * 100.0 / total);
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
