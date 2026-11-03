package dao;

import model.WrongQuestion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题本数据访问对象
 */
public class WrongQuestionDao extends BaseDao<WrongQuestion> {

    private static final WrongQuestionDao INSTANCE = new WrongQuestionDao();

    public static WrongQuestionDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<WrongQuestion> ROW_MAPPER = this::mapRow;

    /**
     * 添加或更新错题（如果已存在则 wrong_count + 1）
     */
    public void addOrUpdate(int userId, int questionId) {
        // 先查是否存在
        String checkSql = "SELECT wq_id, wrong_count FROM wrong_questions WHERE user_id = ? AND question_id = ?";
        WrongQuestion existing = queryOne(checkSql, rs -> {
            WrongQuestion wq = new WrongQuestion();
            wq.setWqId(rs.getInt("wq_id"));
            wq.setWrongCount(rs.getInt("wrong_count"));
            return wq;
        }, userId, questionId);

        if (existing != null) {
            // 已存在，更新计数和状态
            String updateSql = "UPDATE wrong_questions SET wrong_count = wrong_count + 1, mastery_level = 0, resolved_at = NULL, last_wrong_at = datetime('now', 'localtime') WHERE wq_id = ?";
            update(updateSql, existing.getWqId());
        } else {
            // 新错题
            String insertSql = "INSERT INTO wrong_questions (user_id, question_id, wrong_count, mastery_level) VALUES (?, ?, 1, 0)";
            insertAndReturnKey(insertSql, userId, questionId);
        }
    }

    /**
     * 答对时提升掌握程度
     */
    public void improveMastery(int userId, int questionId) {
        String sql = "UPDATE wrong_questions SET mastery_level = MIN(mastery_level + 1, 3), resolved_at = CASE WHEN mastery_level + 1 >= 3 THEN datetime('now', 'localtime') ELSE resolved_at END WHERE user_id = ? AND question_id = ? AND mastery_level < 3";
        update(sql, userId, questionId);
    }

    /**
     * 查询用户的所有错题
     */
    public List<WrongQuestion> findByUserId(int userId) {
        String sql = "SELECT * FROM wrong_questions WHERE user_id = ? ORDER BY mastery_level ASC, wrong_count DESC";
        return queryList(sql, ROW_MAPPER, userId);
    }

    /**
     * 查询未掌握的错题
     */
    public List<WrongQuestion> findUnresolved(int userId) {
        String sql = "SELECT * FROM wrong_questions WHERE user_id = ? AND mastery_level < 3 ORDER BY wrong_count DESC";
        return queryList(sql, ROW_MAPPER, userId);
    }

    /**
     * 统计错题数量
     */
    public int countByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM wrong_questions WHERE user_id = ?";
        return queryCount(sql, userId);
    }

    public int countUnresolved(int userId) {
        String sql = "SELECT COUNT(*) FROM wrong_questions WHERE user_id = ? AND mastery_level < 3";
        return queryCount(sql, userId);
    }

    /**
     * 删除错题记录
     */
    public int delete(int wqId) {
        String sql = "DELETE FROM wrong_questions WHERE wq_id = ?";
        return update(sql, wqId);
    }

    private WrongQuestion mapRow(ResultSet rs) throws SQLException {
        WrongQuestion wq = new WrongQuestion();
        wq.setWqId(rs.getInt("wq_id"));
        wq.setUserId(rs.getInt("user_id"));
        wq.setQuestionId(rs.getInt("question_id"));
        wq.setWrongCount(rs.getInt("wrong_count"));
        wq.setMasteryLevel(rs.getInt("mastery_level"));

        String lastWrongAt = rs.getString("last_wrong_at");
        if (lastWrongAt != null) {
            wq.setLastWrongAt(LocalDateTime.parse(lastWrongAt.replace(" ", "T")));
        }
        String resolvedAt = rs.getString("resolved_at");
        if (resolvedAt != null) {
            wq.setResolvedAt(LocalDateTime.parse(resolvedAt.replace(" ", "T")));
        }
        return wq;
    }
}
