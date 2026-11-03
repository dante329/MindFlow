package dao;

import model.AnswerRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 答题记录数据访问对象
 */
public class AnswerRecordDao extends BaseDao<AnswerRecord> {

    private static final AnswerRecordDao INSTANCE = new AnswerRecordDao();

    public static AnswerRecordDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<AnswerRecord> ROW_MAPPER = this::mapRow;

    /**
     * 插入答题记录
     */
    public int insert(AnswerRecord record) {
        String sql = "INSERT INTO answer_records (user_id, question_id, user_answer, is_correct, time_spent_ms) VALUES (?, ?, ?, ?, ?)";
        return insertAndReturnKey(sql,
                record.getUserId(),
                record.getQuestionId(),
                record.getUserAnswer(),
                record.isCorrect() ? 1 : 0,
                record.getTimeSpentMs());
    }

    /**
     * 查询用户的所有答题记录
     */
    public List<AnswerRecord> findByUserId(int userId) {
        String sql = "SELECT * FROM answer_records WHERE user_id = ? ORDER BY answered_at DESC";
        return queryList(sql, ROW_MAPPER, userId);
    }

    /**
     * 查询用户对某题的答题记录
     */
    public List<AnswerRecord> findByUserAndQuestion(int userId, int questionId) {
        String sql = "SELECT * FROM answer_records WHERE user_id = ? AND question_id = ? ORDER BY answered_at DESC";
        return queryList(sql, ROW_MAPPER, userId, questionId);
    }

    /**
     * 统计用户答题总数
     */
    public int countByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM answer_records WHERE user_id = ?";
        return queryCount(sql, userId);
    }

    /**
     * 统计用户正确数
     */
    public int countCorrectByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM answer_records WHERE user_id = ? AND is_correct = 1";
        return queryCount(sql, userId);
    }

    /**
     * 按科目统计正确率
     */
    public int countCorrectBySubject(int userId, int subjectId) {
        String sql = "SELECT COUNT(*) FROM answer_records ar JOIN questions q ON ar.question_id = q.question_id WHERE ar.user_id = ? AND q.subject_id = ? AND ar.is_correct = 1";
        return queryCount(sql, userId, subjectId);
    }

    public int countBySubject(int userId, int subjectId) {
        String sql = "SELECT COUNT(*) FROM answer_records ar JOIN questions q ON ar.question_id = q.question_id WHERE ar.user_id = ? AND q.subject_id = ?";
        return queryCount(sql, userId, subjectId);
    }

    /**
     * 查询今天的答题记录
     */
    public List<AnswerRecord> findToday(int userId) {
        String sql = "SELECT * FROM answer_records WHERE user_id = ? AND date(answered_at) = date('now', 'localtime') ORDER BY answered_at DESC";
        return queryList(sql, ROW_MAPPER, userId);
    }

    public int countToday(int userId) {
        String sql = "SELECT COUNT(*) FROM answer_records WHERE user_id = ? AND date(answered_at) = date('now', 'localtime')";
        return queryCount(sql, userId);
    }

    public int countCorrectToday(int userId) {
        String sql = "SELECT COUNT(*) FROM answer_records WHERE user_id = ? AND date(answered_at) = date('now', 'localtime') AND is_correct = 1";
        return queryCount(sql, userId);
    }

    private AnswerRecord mapRow(ResultSet rs) throws SQLException {
        AnswerRecord record = new AnswerRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setQuestionId(rs.getInt("question_id"));
        record.setUserAnswer(rs.getString("user_answer"));
        record.setCorrect(rs.getInt("is_correct") == 1);
        record.setTimeSpentMs(rs.getLong("time_spent_ms"));

        String answeredAt = rs.getString("answered_at");
        if (answeredAt != null) {
            record.setAnsweredAt(LocalDateTime.parse(answeredAt.replace(" ", "T")));
        }
        return record;
    }
}
