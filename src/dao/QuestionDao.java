package dao;

import factory.QuestionFactory;
import model.Question;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目数据访问对象
 */
public class QuestionDao extends BaseDao<Question> {

    private static final QuestionDao INSTANCE = new QuestionDao();

    public static QuestionDao getInstance() {
        return INSTANCE;
    }

    private final RowMapper<Question> ROW_MAPPER = this::mapRow;

    /**
     * 根据科目查询所有题目
     */
    public List<Question> findBySubjectId(int subjectId) {
        String sql = "SELECT * FROM questions WHERE subject_id = ? ORDER BY question_type, difficulty";
        return queryList(sql, ROW_MAPPER, subjectId);
    }

    /**
     * 根据 ID 查询题目
     */
    public Question findById(int questionId) {
        String sql = "SELECT * FROM questions WHERE question_id = ?";
        return queryOne(sql, ROW_MAPPER, questionId);
    }

    /**
     * 按题型查询
     */
    public List<Question> findByType(int subjectId, String questionType) {
        String sql = "SELECT * FROM questions WHERE subject_id = ? AND question_type = ? ORDER BY difficulty";
        return queryList(sql, ROW_MAPPER, subjectId, questionType);
    }

    /**
     * 按知识点查询
     */
    public List<Question> findByKnowledgePoint(int kpId) {
        String sql = "SELECT * FROM questions WHERE kp_id = ? ORDER BY question_type, difficulty";
        return queryList(sql, ROW_MAPPER, kpId);
    }

    /**
     * 随机抽取题目
     */
    public List<Question> findRandom(int subjectId, int limit) {
        String sql = "SELECT * FROM questions WHERE subject_id = ? ORDER BY RANDOM() LIMIT ?";
        return queryList(sql, ROW_MAPPER, subjectId, limit);
    }

    /**
     * 按难度随机抽题
     */
    public List<Question> findRandomByDifficulty(int subjectId, int difficulty, int limit) {
        String sql = "SELECT * FROM questions WHERE subject_id = ? AND difficulty = ? ORDER BY RANDOM() LIMIT ?";
        return queryList(sql, ROW_MAPPER, subjectId, difficulty, limit);
    }

    /**
     * 搜索题目（按内容关键词）
     */
    public List<Question> search(int subjectId, String keyword) {
        String sql = "SELECT * FROM questions WHERE subject_id = ? AND content LIKE ? ORDER BY question_type";
        return queryList(sql, ROW_MAPPER, subjectId, "%" + keyword + "%");
    }

    /**
     * 统计科目题目数量
     */
    public int countBySubject(int subjectId) {
        String sql = "SELECT COUNT(*) FROM questions WHERE subject_id = ?";
        return queryCount(sql, subjectId);
    }

    /**
     * 插入题目（通用，根据题型设置不同字段）
     */
    public int insert(Question question) {
        String sql = "INSERT INTO questions (subject_id, kp_id, question_type, difficulty, content, option_a, option_b, option_c, option_d, answer, explanation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String optA = null, optB = null, optC = null, optD = null;
        if (question instanceof model.ChoiceQuestion cq) {
            optA = cq.getOptionA();
            optB = cq.getOptionB();
            optC = cq.getOptionC();
            optD = cq.getOptionD();
        }

        return insertAndReturnKey(sql,
                question.getSubjectId(),
                question.getKpId(),
                question.getTypeCode(),
                question.getDifficulty(),
                question.getContent(),
                optA, optB, optC, optD,
                question.getAnswer(),
                question.getExplanation());
    }

    /**
     * 删除题目
     */
    public int delete(int questionId) {
        String sql = "DELETE FROM questions WHERE question_id = ?";
        return update(sql, questionId);
    }

    /**
     * ResultSet → Question 子类（使用工厂模式）
     */
    private Question mapRow(ResultSet rs) throws SQLException {
        return QuestionFactory.createFromResultSet(rs);
    }
}
