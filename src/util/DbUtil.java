package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * 数据库工具类（单例模式）
 * 管理 SQLite 数据库连接的创建和初始化
 */
public class DbUtil {

    private static final String DB_PATH = "mindflow.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;
    private static volatile DbUtil instance;

    private DbUtil() {
        // 私有构造，防止外部实例化
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC 驱动未找到，请确保 lib/sqlite-jdbc.jar 在 classpath 中", e);
        }
    }

    /**
     * 获取 DbUtil 单例实例（双重检查锁）
     */
    public static DbUtil getInstance() {
        if (instance == null) {
            synchronized (DbUtil.class) {
                if (instance == null) {
                    instance = new DbUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 获取数据库连接
     *
     * @return SQLite 数据库连接
     * @throws SQLException 连接失败时抛出
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_URL);
        // 启用外键约束
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /**
     * 初始化数据库：执行 schema.sql 建表脚本
     * 如果表已存在则跳过（IF NOT EXISTS）
     */
    public void initDatabase() {
        try {
            // 从 resources 目录读取 schema.sql
            InputStream is = getClass().getClassLoader().getResourceAsStream("resources/schema.sql");
            if (is == null) {
                // 尝试直接文件路径（开发环境）
                is = new java.io.FileInputStream("src/resources/schema.sql");
            }

            String sql;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                // 先过滤掉纯注释行，再拼接
                sql = reader.lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining("\n"));
            }

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                // SQLite 不支持一次执行多条语句，按分号拆分逐条执行
                String[] statements = sql.split(";");
                for (String s : statements) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }

            System.out.println("[DB] 数据库初始化完成 ✓");
        } catch (Exception e) {
            throw new RuntimeException("[DB] 数据库初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 安全关闭资源
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("[DB] 关闭资源失败: " + e.getMessage());
                }
            }
        }
    }
}
