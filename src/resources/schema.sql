-- MindFlow 考研备考复习系统 - 数据库建表脚本
-- 使用 SQLite 数据库

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT    NOT NULL UNIQUE,
    password    TEXT    NOT NULL,
    nickname    TEXT    NOT NULL,
    target_school TEXT,
    target_major  TEXT,
    exam_date   TEXT,
    created_at  TEXT    DEFAULT (datetime('now', 'localtime')),
    updated_at  TEXT    DEFAULT (datetime('now', 'localtime'))
);

-- 科目表
CREATE TABLE IF NOT EXISTS subjects (
    subject_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    name        TEXT    NOT NULL,
    description TEXT,
    total_score REAL    DEFAULT 150.0,
    is_preset   INTEGER DEFAULT 0,
    created_at  TEXT    DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 知识点/章节表
CREATE TABLE IF NOT EXISTS knowledge_points (
    kp_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id  INTEGER NOT NULL,
    parent_id   INTEGER DEFAULT NULL,
    name        TEXT    NOT NULL,
    sort_order  INTEGER DEFAULT 0,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES knowledge_points(kp_id) ON DELETE SET NULL
);

-- 题目表（单表继承，用 question_type 区分题型）
CREATE TABLE IF NOT EXISTS questions (
    question_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id    INTEGER NOT NULL,
    kp_id         INTEGER,
    question_type TEXT    NOT NULL CHECK(question_type IN ('CHOICE', 'TRUE_FALSE', 'FILL_BLANK')),
    difficulty    INTEGER DEFAULT 3 CHECK(difficulty BETWEEN 1 AND 5),
    content       TEXT    NOT NULL,
    -- 选择题专用
    option_a      TEXT,
    option_b      TEXT,
    option_c      TEXT,
    option_d      TEXT,
    -- 通用答案字段
    answer        TEXT    NOT NULL,
    -- 解析
    explanation   TEXT,
    -- 填空题可能有多个空，用 JSON 格式存答案 {"1":"xxx","2":"yyy"}
    created_at    TEXT    DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    FOREIGN KEY (kp_id) REFERENCES knowledge_points(kp_id) ON DELETE SET NULL
);

-- 答题记录表
CREATE TABLE IF NOT EXISTS answer_records (
    record_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL,
    question_id   INTEGER NOT NULL,
    user_answer   TEXT    NOT NULL,
    is_correct    INTEGER NOT NULL,
    time_spent_ms INTEGER DEFAULT 0,
    answered_at   TEXT    DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- 错题本表
CREATE TABLE IF NOT EXISTS wrong_questions (
    wq_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL,
    question_id   INTEGER NOT NULL,
    wrong_count   INTEGER DEFAULT 1,
    mastery_level INTEGER DEFAULT 0 CHECK(mastery_level BETWEEN 0 AND 3),
    last_wrong_at TEXT    DEFAULT (datetime('now', 'localtime')),
    resolved_at   TEXT,
    UNIQUE(user_id, question_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- 每日学习计划表
CREATE TABLE IF NOT EXISTS daily_plans (
    plan_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL,
    plan_date     TEXT    NOT NULL,
    subject_id    INTEGER,
    target_count  INTEGER DEFAULT 20,
    actual_count  INTEGER DEFAULT 0,
    is_completed  INTEGER DEFAULT 0,
    study_minutes INTEGER DEFAULT 0,
    note          TEXT,
    created_at    TEXT    DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE SET NULL
);

-- 学习会话记录（用于统计学习时长）
CREATE TABLE IF NOT EXISTS study_sessions (
    session_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL,
    subject_id    INTEGER,
    start_time    TEXT    NOT NULL,
    end_time      TEXT,
    duration_ms   INTEGER DEFAULT 0,
    questions_done INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE SET NULL
);

-- 复习提醒表（艾宾浩斯遗忘曲线）
CREATE TABLE IF NOT EXISTS review_reminders (
    reminder_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL,
    question_id   INTEGER NOT NULL,
    review_date   TEXT    NOT NULL,
    review_stage  INTEGER DEFAULT 1,
    is_reviewed   INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_questions_subject ON questions(subject_id);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(question_type);
CREATE INDEX IF NOT EXISTS idx_questions_kp ON questions(kp_id);
CREATE INDEX IF NOT EXISTS idx_answer_records_user ON answer_records(user_id);
CREATE INDEX IF NOT EXISTS idx_answer_records_question ON answer_records(question_id);
CREATE INDEX IF NOT EXISTS idx_wrong_questions_user ON wrong_questions(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_plans_user_date ON daily_plans(user_id, plan_date);
CREATE INDEX IF NOT EXISTS idx_study_sessions_user ON study_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_review_reminders_user_date ON review_reminders(user_id, review_date);
