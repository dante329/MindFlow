package dao;

import model.KnowledgePoint;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 知识点数据访问对象
 */
public class KnowledgePointDao extends BaseDao<KnowledgePoint> {

    private static final KnowledgePointDao INSTANCE = new KnowledgePointDao();

    public static KnowledgePointDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<KnowledgePoint> ROW_MAPPER = this::mapRow;

    /**
     * 查询科目下的所有知识点
     */
    public List<KnowledgePoint> findBySubjectId(int subjectId) {
        String sql = "SELECT * FROM knowledge_points WHERE subject_id = ? ORDER BY parent_id NULLS FIRST, sort_order";
        return queryList(sql, ROW_MAPPER, subjectId);
    }

    /**
     * 查询某知识点的子节点
     */
    public List<KnowledgePoint> findChildren(int parentId) {
        String sql = "SELECT * FROM knowledge_points WHERE parent_id = ? ORDER BY sort_order";
        return queryList(sql, ROW_MAPPER, parentId);
    }

    /**
     * 根据 ID 查询
     */
    public KnowledgePoint findById(int kpId) {
        String sql = "SELECT * FROM knowledge_points WHERE kp_id = ?";
        return queryOne(sql, ROW_MAPPER, kpId);
    }

    /**
     * 插入知识点
     */
    public int insert(KnowledgePoint kp) {
        String sql = "INSERT INTO knowledge_points (subject_id, parent_id, name, sort_order) VALUES (?, ?, ?, ?)";
        return insertAndReturnKey(sql, kp.getSubjectId(), kp.getParentId(), kp.getName(), kp.getSortOrder());
    }

    /**
     * 删除知识点
     */
    public int delete(int kpId) {
        String sql = "DELETE FROM knowledge_points WHERE kp_id = ?";
        return update(sql, kpId);
    }

    private KnowledgePoint mapRow(ResultSet rs) throws SQLException {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setKpId(rs.getInt("kp_id"));
        kp.setSubjectId(rs.getInt("subject_id"));
        int parentId = rs.getInt("parent_id");
        kp.setParentId(rs.wasNull() ? null : parentId);
        kp.setName(rs.getString("name"));
        kp.setSortOrder(rs.getInt("sort_order"));
        return kp;
    }
}
