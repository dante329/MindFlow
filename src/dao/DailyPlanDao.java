package dao;

import model.DailyPlan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日计划数据访问对象
 */
public class DailyPlanDao extends BaseDao<DailyPlan> {

    private static final DailyPlanDao INSTANCE = new DailyPlanDao();

    public static DailyPlanDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<DailyPlan> ROW_MAPPER = this::mapRow;

    /**
     * 插入计划
     */
    public int insert(DailyPlan plan) {
        String sql = "INSERT INTO daily_plans (user_id, plan_date, subject_id, target_count, note) VALUES (?, ?, ?, ?, ?)";
        return insertAndReturnKey(sql,
                plan.getUserId(),
                plan.getPlanDate().toString(),
                plan.getSubjectId(),
                plan.getTargetCount(),
                plan.getNote());
    }

    /**
     * 查询某天的计划
     */
    public List<DailyPlan> findByDate(int userId, LocalDate date) {
        String sql = "SELECT * FROM daily_plans WHERE user_id = ? AND plan_date = ? ORDER BY created_at";
        return queryList(sql, ROW_MAPPER, userId, date.toString());
    }

    /**
     * 查询今天的计划
     */
    public List<DailyPlan> findToday(int userId) {
        return findByDate(userId, LocalDate.now());
    }

    /**
     * 更新实际完成数
     */
    public void updateActualCount(int planId, int actualCount) {
        String sql = "UPDATE daily_plans SET actual_count = ?, is_completed = CASE WHEN ? >= target_count THEN 1 ELSE 0 END WHERE plan_id = ?";
        update(sql, actualCount, actualCount, planId);
    }

    /**
     * 更新学习时长
     */
    public void addStudyMinutes(int planId, int minutes) {
        String sql = "UPDATE daily_plans SET study_minutes = study_minutes + ? WHERE plan_id = ?";
        update(sql, minutes, planId);
    }

    /**
     * 查询最近 N 天的计划
     */
    public List<DailyPlan> findRecent(int userId, int days) {
        String sql = "SELECT * FROM daily_plans WHERE user_id = ? AND plan_date >= date('now', 'localtime', '-' || ? || ' days') ORDER BY plan_date DESC";
        return queryList(sql, ROW_MAPPER, userId, days);
    }

    /**
     * 统计连续打卡天数
     */
    public int countStreak(int userId) {
        // 从今天往回数，连续有已完成计划的天数
        String sql = """
                WITH RECURSIVE dates(d) AS (
                    SELECT date('now', 'localtime')
                    UNION ALL
                    SELECT date(d, '-1 day') FROM dates
                    WHERE d > date('now', 'localtime', '-365 days')
                )
                SELECT COUNT(*) FROM dates d
                WHERE EXISTS (
                    SELECT 1 FROM daily_plans dp
                    WHERE dp.user_id = ? AND dp.plan_date = d.d AND dp.is_completed = 1
                )
                AND NOT EXISTS (
                    SELECT 1 FROM dates d2
                    WHERE d2.d = date(d.d, '+1 day')
                    AND NOT EXISTS (
                        SELECT 1 FROM daily_plans dp2
                        WHERE dp2.user_id = ? AND dp2.plan_date = d2.d AND dp2.is_completed = 1
                    )
                    AND d2.d <= date('now', 'localtime')
                )
                """;
        // SQLite 递归 CTE 可能复杂，用简单方式：直接查最近连续完成天数
        String simpleSql = "SELECT COUNT(DISTINCT plan_date) FROM daily_plans WHERE user_id = ? AND is_completed = 1 AND plan_date >= date('now', 'localtime', '-30 days')";
        return queryCount(simpleSql, userId);
    }

    private DailyPlan mapRow(ResultSet rs) throws SQLException {
        DailyPlan plan = new DailyPlan();
        plan.setPlanId(rs.getInt("plan_id"));
        plan.setUserId(rs.getInt("user_id"));
        plan.setPlanDate(LocalDate.parse(rs.getString("plan_date")));
        int subjectId = rs.getInt("subject_id");
        plan.setSubjectId(rs.wasNull() ? null : subjectId);
        plan.setTargetCount(rs.getInt("target_count"));
        plan.setActualCount(rs.getInt("actual_count"));
        plan.setCompleted(rs.getInt("is_completed") == 1);
        plan.setStudyMinutes(rs.getInt("study_minutes"));
        plan.setNote(rs.getString("note"));

        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            plan.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        return plan;
    }
}
