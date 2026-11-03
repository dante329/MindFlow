package service;

import dao.KnowledgePointDao;
import dao.QuestionDao;
import dao.SubjectDao;
import exception.BusinessException;
import model.KnowledgePoint;
import model.Subject;
import util.ConsoleUtil;

import java.util.List;

/**
 * 科目管理业务服务
 */
public class SubjectService {

    private static final SubjectService INSTANCE = new SubjectService();
    private final SubjectDao subjectDao = SubjectDao.getInstance();
    private final QuestionDao questionDao = QuestionDao.getInstance();
    private final KnowledgePointDao kpDao = KnowledgePointDao.getInstance();

    public static SubjectService getInstance() {
        return INSTANCE;
    }

    /**
     * 获取当前用户的所有科目
     */
    public List<Subject> getMySubjects() {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        return subjectDao.findByUserId(userId);
    }

    /**
     * 根据 ID 获取科目
     */
    public Subject getSubject(int subjectId) {
        Subject subject = subjectDao.findById(subjectId);
        if (subject == null) {
            throw new BusinessException("科目不存在");
        }
        return subject;
    }

    /**
     * 添加自定义科目
     */
    public Subject addSubject(String name, String description, double totalScore) {
        int userId = UserService.getInstance().getCurrentUser().getUserId();
        Subject subject = new Subject(userId, name, description, totalScore);
        int id = subjectDao.insert(subject);
        subject.setSubjectId(id);
        ConsoleUtil.printSuccess("科目 '" + name + "' 添加成功");
        return subject;
    }

    /**
     * 修改科目信息
     */
    public void updateSubject(int subjectId, String name, String description, double totalScore) {
        Subject subject = getSubject(subjectId);
        if (subject.isPreset()) {
            // 预设科目只允许改描述和总分
            subject.setDescription(description);
            subject.setTotalScore(totalScore);
        } else {
            subject.setName(name);
            subject.setDescription(description);
            subject.setTotalScore(totalScore);
        }
        subjectDao.update(subject);
        ConsoleUtil.printSuccess("科目信息已更新");
    }

    /**
     * 删除自定义科目（预设科目不可删）
     */
    public void deleteSubject(int subjectId) {
        Subject subject = getSubject(subjectId);
        if (subject.isPreset()) {
            throw new BusinessException("预设科目不能删除");
        }
        int count = questionDao.countBySubject(subjectId);
        if (count > 0) {
            if (!ConsoleUtil.confirm("该科目下有 " + count + " 道题目，删除将同时删除所有题目，确认？")) {
                return;
            }
        }
        subjectDao.delete(subjectId);
        ConsoleUtil.printSuccess("科目已删除");
    }

    /**
     * 显示科目列表（含题目数量）
     */
    public void displaySubjects() {
        List<Subject> subjects = getMySubjects();
        if (subjects.isEmpty()) {
            ConsoleUtil.printWarning("还没有科目，请先添加");
            return;
        }

        ConsoleUtil.printHeader("我的科目");
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            int qCount = questionDao.countBySubject(s.getSubjectId());
            String presetTag = s.isPreset() ? " [预设]" : "";
            System.out.printf("  [%d] %s%s - %.0f分 - %d道题%n",
                    i + 1, s.getName(), presetTag, s.getTotalScore(), qCount);
        }
    }

    /**
     * 让用户选择一个科目，返回 Subject 对象
     */
    public Subject selectSubject() {
        List<Subject> subjects = getMySubjects();
        if (subjects.isEmpty()) {
            throw new BusinessException("还没有科目");
        }
        String[] options = subjects.stream()
                .map(s -> s.getName() + " (" + questionDao.countBySubject(s.getSubjectId()) + "题)")
                .toArray(String[]::new);
        int choice = ConsoleUtil.selectFromMenu(options);
        return subjects.get(choice - 1);
    }

    // ==================== 知识点管理 ====================

    public List<KnowledgePoint> getKnowledgePoints(int subjectId) {
        return kpDao.findBySubjectId(subjectId);
    }

    public void addKnowledgePoint(int subjectId, Integer parentId, String name) {
        KnowledgePoint kp = new KnowledgePoint(subjectId, parentId, name, 0);
        kpDao.insert(kp);
        ConsoleUtil.printSuccess("知识点 '" + name + "' 添加成功");
    }

    public void displayKnowledgePoints(int subjectId) {
        List<KnowledgePoint> kps = kpDao.findBySubjectId(subjectId);
        if (kps.isEmpty()) {
            ConsoleUtil.printInfo("暂无知识点");
            return;
        }
        for (KnowledgePoint kp : kps) {
            System.out.println("  " + kp);
        }
    }
}
