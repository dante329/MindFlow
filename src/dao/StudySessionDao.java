package dao;

import model.StudySession;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习会话数据访问对象
 */
public class StudySessionDao extends BaseDao<StudySession> {

    private static final StudySessionDao INSTANCE = new StudySessionDao();

    public static StudySessionDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<StudySession> ROW_MAPPER = this::mapRow;

    /**
     * 插入新会话
     */
    public int insert(StudySession session) {
        String sql = "INSERT INTO study_sessions (user_id, subject_id, start_time) VALUES (?, ?, ?)";
        return insertAndReturnKey(sql,
                session.getUserId(),
                session.getSubjectId(),
                session.getStartTime().toString().replace("T", " "));
    }

    /**
     * 结束会话
     */
    public void endSession(int sessionId, long durationMs, int questionsDone) {
        String sql = "UPDATE study_sessions SET end_time = datetime('now', 'localtime'), duration_ms = ?, questions_done = ? WHERE session_id = ?";
        update(sql, durationMs, questionsDone, sessionId);
    }

    /**
     * 查询用户今天的学习总时长（毫秒）
     */
    public long sumTodayDuration(int userId) {
        String sql = "SELECT COALESCE(SUM(duration_ms), 0) FROM study_sessions WHERE user_id = ? AND date(start_time) = date('now', 'localtime')";
        try (var conn = util.DbUtil.getInstance().getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * 查询最近 N 天的每日学习时长
     */
    public List<StudySession> findRecent(int userId, int days) {
        String sql = "SELECT * FROM study_sessions WHERE user_id = ? AND start_time >= datetime('now', 'localtime', '-' || ? || ' days') ORDER BY start_time DESC";
        return queryList(sql, ROW_MAPPER, userId, days);
    }

    private StudySession mapRow(ResultSet rs) throws SQLException {
        StudySession session = new StudySession();
        session.setSessionId(rs.getInt("session_id"));
        session.setUserId(rs.getInt("user_id"));
        int subjectId = rs.getInt("subject_id");
        session.setSubjectId(rs.wasNull() ? null : subjectId);
        session.setDurationMs(rs.getLong("duration_ms"));
        session.setQuestionsDone(rs.getInt("questions_done"));

        String startTime = rs.getString("start_time");
        if (startTime != null) {
            session.setStartTime(LocalDateTime.parse(startTime.replace(" ", "T")));
        }
        String endTime = rs.getString("end_time");
        if (endTime != null) {
            session.setEndTime(LocalDateTime.parse(endTime.replace(" ", "T")));
        }
        return session;
    }
}
