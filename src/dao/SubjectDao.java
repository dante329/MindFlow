package dao;

import model.Subject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 科目数据访问对象
 */
public class SubjectDao extends BaseDao<Subject> {

    private static final SubjectDao INSTANCE = new SubjectDao();

    public static SubjectDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<Subject> ROW_MAPPER = this::mapRow;

    /**
     * 查询用户的所有科目
     */
    public List<Subject> findByUserId(int userId) {
        String sql = "SELECT * FROM subjects WHERE user_id = ? ORDER BY is_preset DESC, created_at ASC";
        return queryList(sql, ROW_MAPPER, userId);
    }

    /**
     * 根据 ID 查询科目
     */
    public Subject findById(int subjectId) {
        String sql = "SELECT * FROM subjects WHERE subject_id = ?";
        return queryOne(sql, ROW_MAPPER, subjectId);
    }

    /**
     * 插入科目
     */
    public int insert(Subject subject) {
        String sql = "INSERT INTO subjects (user_id, name, description, total_score, is_preset) VALUES (?, ?, ?, ?, ?)";
        return insertAndReturnKey(sql,
                subject.getUserId(),
                subject.getName(),
                subject.getDescription(),
                subject.getTotalScore(),
                subject.isPreset() ? 1 : 0);
    }

    /**
     * 更新科目
     */
    public int update(Subject subject) {
        String sql = "UPDATE subjects SET name = ?, description = ?, total_score = ? WHERE subject_id = ?";
        return update(sql,
                subject.getName(),
                subject.getDescription(),
                subject.getTotalScore(),
                subject.getSubjectId());
    }

    /**
     * 删除科目
     */
    public int delete(int subjectId) {
        String sql = "DELETE FROM subjects WHERE subject_id = ? AND is_preset = 0";
        return update(sql, subjectId);
    }

    /**
     * 检查用户是否已有预设科目
     */
    public boolean hasPresetSubjects(int userId) {
        String sql = "SELECT COUNT(*) FROM subjects WHERE user_id = ? AND is_preset = 1";
        return queryCount(sql, userId) > 0;
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(rs.getInt("subject_id"));
        subject.setUserId(rs.getInt("user_id"));
        subject.setName(rs.getString("name"));
        subject.setDescription(rs.getString("description"));
        subject.setTotalScore(rs.getDouble("total_score"));
        subject.setPreset(rs.getInt("is_preset") == 1);

        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            subject.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        return subject;
    }
}
