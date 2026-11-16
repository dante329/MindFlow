# MindFlow 考研备考复习系统

> G01 课程设计项目 — 基于 Java + SQLite 的命令行考研复习管理系统

## 功能概览

| 模块 | 说明 |
|------|------|
| 用户管理 | 注册、登录、个人资料（目标院校/专业/考试日期） |
| 科目管理 | 预设考研四科（政治/英语/数学/专业课），支持自定义 |
| 题库管理 | 选择题 / 判断题 / 填空题，按科目和关键词检索 |
| 答题练习 | 随机刷题、按题型刷、错题重做，实时计时 |
| 错题本 | 自动收集错题，支持回顾和移除 |
| 每日计划 | 设定每日目标题数，跟踪完成进度 |
| 学习统计 | 各科正确率、答题趋势，支持 CSV 导出 |
| 复习提醒 | 艾宾浩斯遗忘曲线多线程后台提醒 |

## 环境要求

- **JDK 21+**（使用了 switch 表达式、var 等语法）
- 无需额外构建工具，依赖 jar 已包含在 `lib/` 目录

## 快速开始

```bash
# 编译 + 运行测试
./build.sh

# 仅编译
./build.sh compile

# 运行程序
./build.sh run

# 清理编译产物
./build.sh clean
```

或在 IntelliJ IDEA 中直接运行 `src/Main.java`。

## 项目结构

```
MindFlow/
├── src/                    # 源码
│   ├── Main.java           # 主入口 & 控制台菜单
│   ├── model/              # 实体类（Question 继承体系）
│   ├── dao/                # 数据访问层
│   ├── service/            # 业务逻辑层
│   ├── strategy/           # 策略模式 - 判分逻辑
│   ├── factory/            # 工厂模式 - 题目创建
│   ├── thread/             # 复习提醒线程
│   ├── exception/          # 自定义异常
│   ├── util/               # 工具类（DB/密码/导出/控制台）
│   └── resources/          # schema.sql
├── test/                   # 单元测试（JUnit 5）
├── lib/                    # 依赖 jar（SQLite JDBC, SLF4J, JUnit）
├── out/                    # 编译输出
├── exports/                # CSV 导出目录
├── build.sh                # 构建 & 测试脚本
└── generate_doc.py         # 设计文档生成器
```

## 设计模式

- **单例模式** — DbUtil、各 Service、各 Dao
- **工厂模式** — QuestionFactory 根据题型创建题目对象
- **策略模式** — GradingStrategy 接口 + 三种判分实现
- **模板方法** — Question.formatDisplay() 定义显示骨架，子类重写细节

## 数据库

SQLite 本地存储，9 张表：users / subjects / knowledge_points / questions / answer_records / wrong_questions / daily_plans / study_sessions / review_reminders

首次运行时自动建表并导入示例数据（121 道题）。

## 运行测试

```bash
./build.sh test
```

当前覆盖：策略模式判分（选择/判断/填空）、工厂模式、密码工具、实体类显示格式化，共 41 个测试用例。

## 技术栈

- Java 21
- SQLite (sqlite-jdbc 3.46.1)
- JUnit 5 (junit-platform-console-standalone 1.10.3)
- SLF4J 2.0.16
