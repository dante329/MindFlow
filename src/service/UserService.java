package service;

import dao.SubjectDao;
import dao.UserDao;
import exception.BusinessException;
import exception.DuplicateException;
import model.Subject;
import model.User;
import util.ConsoleUtil;
import util.PasswordUtil;

/**
 * 用户业务服务
 * 处理注册、登录、个人信息管理
 */
public class UserService {

    private static final UserService INSTANCE = new UserService();
    private final UserDao userDao = UserDao.getInstance();
    private final SubjectDao subjectDao = SubjectDao.getInstance();

    /** 当前登录用户（会话级别） */
    private User currentUser;

    public static UserService getInstance() {
        return INSTANCE;
    }

    /**
     * 用户注册
     */
    public User register(String username, String password, String nickname) {
        // 校验
        if (username == null || username.length() < 2) {
            throw new BusinessException("用户名至少2个字符");
        }
        if (password == null || password.length() < 4) {
            throw new BusinessException("密码至少4个字符");
        }
        if (userDao.existsByUsername(username)) {
            throw new DuplicateException("用户", "用户名", username);
        }

        // 创建用户
        User user = new User(username, PasswordUtil.hashPassword(password), nickname);
        int userId = userDao.insert(user);
        user.setUserId(userId);

        // 初始化预设科目
        initPresetSubjects(userId);

        ConsoleUtil.printSuccess("注册成功！欢迎 " + nickname);
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        this.currentUser = user;
        ConsoleUtil.printSuccess("登录成功！欢迎回来，" + user.getNickname());
        return user;
    }

    /**
     * 登出
     */
    public void logout() {
        if (currentUser != null) {
            ConsoleUtil.printInfo(currentUser.getNickname() + " 已退出登录");
        }
        this.currentUser = null;
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 4) {
            throw new BusinessException("新密码至少4个字符");
        }
        userDao.updatePassword(user.getUserId(), PasswordUtil.hashPassword(newPassword));
        ConsoleUtil.printSuccess("密码修改成功");
    }

    /**
     * 更新个人信息
     */
    public void updateProfile(String nickname, String targetSchool, String targetMajor, String examDate) {
        User user = getCurrentUser();
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        user.setTargetSchool(targetSchool);
        user.setTargetMajor(targetMajor);
        user.setExamDate(examDate);
        userDao.update(user);
        ConsoleUtil.printSuccess("个人信息已更新");
    }

    /**
     * 初始化考研四大预设科目
     */
    private void initPresetSubjects(int userId) {
        if (subjectDao.hasPresetSubjects(userId)) {
            return;
        }
        String[][] presets = {
                {"政治", "马克思主义基本原理、毛泽东思想等", "100"},
                {"英语", "英语一/英语二", "100"},
                {"数学", "数学一/数学二/数学三", "150"},
                {"专业课", "根据报考专业确定", "150"}
        };
        for (String[] preset : presets) {
            Subject subject = new Subject(userId, preset[0], preset[1], Double.parseDouble(preset[2]));
            subject.setPreset(true);
            subjectDao.insert(subject);
        }
    }
}
