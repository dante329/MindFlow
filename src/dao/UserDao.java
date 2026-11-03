package dao;

import model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据访问对象
 */
public class UserDao extends BaseDao<User> {

    private static final UserDao INSTANCE = new UserDao();

    public static UserDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<User> ROW_MAPPER = this::mapRow;

    /**
     * 根据用户名查询用户
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return queryOne(sql, ROW_MAPPER, username);
    }

    /**
     * 根据 ID 查询用户
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return queryOne(sql, ROW_MAPPER, userId);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        return queryCount(sql, username) > 0;
    }

    /**
     * 插入新用户，返回自增 ID
     */
    public int insert(User user) {
        String sql = "INSERT INTO users (username, password, nickname, target_school, target_major, exam_date) VALUES (?, ?, ?, ?, ?, ?)";
        return insertAndReturnKey(sql,
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getTargetSchool(),
                user.getTargetMajor(),
                user.getExamDate());
    }

    /**
     * 更新用户信息
     */
    public int update(User user) {
        String sql = "UPDATE users SET nickname = ?, target_school = ?, target_major = ?, exam_date = ?, updated_at = datetime('now', 'localtime') WHERE user_id = ?";
        return update(sql,
                user.getNickname(),
                user.getTargetSchool(),
                user.getTargetMajor(),
                user.getExamDate(),
                user.getUserId());
    }

    /**
     * 更新密码
     */
    public int updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = datetime('now', 'localtime') WHERE user_id = ?";
        return update(sql, hashedPassword, userId);
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return queryList(sql, ROW_MAPPER);
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNickname(rs.getString("nickname"));
        user.setTargetSchool(rs.getString("target_school"));
        user.setTargetMajor(rs.getString("target_major"));
        user.setExamDate(rs.getString("exam_date"));

        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(LocalDateTime.parse(updatedAt.replace(" ", "T")));
        }
        return user;
    }
}
