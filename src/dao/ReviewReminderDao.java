package dao;

import model.ReviewReminder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 复习提醒数据访问对象
 */
public class ReviewReminderDao extends BaseDao<ReviewReminder> {

    private static final ReviewReminderDao INSTANCE = new ReviewReminderDao();

    public static ReviewReminderDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<ReviewReminder> ROW_MAPPER = this::mapRow;

    /**
     * 插入复习提醒
     */
    public int insert(ReviewReminder reminder) {
        String sql = "INSERT INTO review_reminders (user_id, question_id, review_date, review_stage) VALUES (?, ?, ?, ?)";
        return insertAndReturnKey(sql,
                reminder.getUserId(),
                reminder.getQuestionId(),
                reminder.getReviewDate(),
                reminder.getReviewStage());
    }

    /**
     * 查询今天到期的复习提醒
     */
    public List<ReviewReminder> findDueToday(int userId) {
        String sql = "SELECT * FROM review_reminders WHERE user_id = ? AND review_date <= date('now', 'localtime') AND is_reviewed = 0 ORDER BY review_date";
        return queryList(sql, ROW_MAPPER, userId);
    }

    /**
     * 标记为已复习，并创建下一阶段的提醒
     */
    public void markReviewed(int reminderId) {
        String sql = "UPDATE review_reminders SET is_reviewed = 1 WHERE reminder_id = ?";
        update(sql, reminderId);
    }

    /**
     * 统计待复习数量
     */
    public int countDue(int userId) {
        String sql = "SELECT COUNT(*) FROM review_reminders WHERE user_id = ? AND review_date <= date('now', 'localtime') AND is_reviewed = 0";
        return queryCount(sql, userId);
    }

    private ReviewReminder mapRow(ResultSet rs) throws SQLException {
        ReviewReminder r = new ReviewReminder();
        r.setReminderId(rs.getInt("reminder_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setQuestionId(rs.getInt("question_id"));
        r.setReviewDate(rs.getString("review_date"));
        r.setReviewStage(rs.getInt("review_stage"));
        r.setReviewed(rs.getInt("is_reviewed") == 1);
        return r;
    }
}
