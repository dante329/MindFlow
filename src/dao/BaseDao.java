package dao;

import util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO 基类
 * 提供通用的数据库操作方法，减少重复代码
 *
 * @param <T> 实体类型
 */
public abstract class BaseDao<T> {

    /**
     * 执行查询，返回实体列表
     */
    protected List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (Connection conn = DbUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DAO] 查询失败: " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * 执行查询，返回单个实体（可能为 null）
     */
    protected T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = queryList(sql, mapper, params);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 执行更新（INSERT/UPDATE/DELETE），返回影响行数
     */
    protected int update(String sql, Object... params) {
        try (Connection conn = DbUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DAO] 更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行插入并返回自增主键
     */
    protected int insertAndReturnKey(String sql, Object... params) {
        try (Connection conn = DbUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            setParameters(ps, params);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DAO] 插入失败: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 查询计数
     */
    protected int queryCount(String sql, Object... params) {
        try (Connection conn = DbUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DAO] 计数查询失败: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 设置 PreparedStatement 参数
     */
    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /**
     * 行映射器函数式接口
     */
    @FunctionalInterface
    protected interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }
}
