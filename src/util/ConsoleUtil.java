package util;

import exception.BusinessException;

import java.util.List;
import java.util.Scanner;

/**
 * 控制台交互工具类
 * 提供统一的输入输出、菜单显示、格式化等功能
 */
public class ConsoleUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    // ANSI 颜色代码（部分终端支持）
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String CYAN = "\033[36m";
    public static final String BOLD = "\033[1m";

    private ConsoleUtil() {
        // 工具类不允许实例化
    }

    /**
     * 获取全局 Scanner 实例
     */
    public static Scanner getScanner() {
        return SCANNER;
    }

    // ==================== 输出方法 ====================

    public static void printLine(String text) {
        System.out.println(text);
    }

    public static void printSuccess(String text) {
        System.out.println(GREEN + "✓ " + text + RESET);
    }

    public static void printError(String text) {
        System.out.println(RED + "✗ " + text + RESET);
    }

    public static void printWarning(String text) {
        System.out.println(YELLOW + "⚠ " + text + RESET);
    }

    public static void printInfo(String text) {
        System.out.println(CYAN + "ℹ " + text + RESET);
    }

    public static void printHeader(String title) {
        int width = 50;
        String padding = "═".repeat((width - title.length()) / 2);
        System.out.println();
        System.out.println(BOLD + CYAN + "╔" + "═".repeat(width) + "╗" + RESET);
        System.out.println(BOLD + CYAN + "║" + centerText(title, width) + "║" + RESET);
        System.out.println(BOLD + CYAN + "╚" + "═".repeat(width) + "╝" + RESET);
        System.out.println();
    }

    public static void printSeparator() {
        System.out.println("─".repeat(50));
    }

    public static void printMenu(String[] options) {
        System.out.println();
        for (int i = 0; i < options.length; i++) {
            System.out.println(BOLD + "  [" + (i + 1) + "]" + RESET + " " + options[i]);
        }
        System.out.println();
    }

    // ==================== 输入方法 ====================

    /**
     * 读取一行字符串输入
     */
    public static String readLine(String prompt) {
        System.out.print(prompt + ": ");
        try {
            return SCANNER.nextLine().trim();
        } catch (java.util.NoSuchElementException e) {
            // 输入流已关闭（如管道输入耗尽），返回空字符串
            return "";
        }
    }

    /**
     * 读取整数输入，带重试
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input;
            try {
                input = SCANNER.nextLine().trim();
            } catch (java.util.NoSuchElementException e) {
                throw new BusinessException("输入流已关闭");
            }
            if (input.isEmpty()) {
                throw new BusinessException("输入为空");
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printError("请输入有效的数字");
            }
        }
    }

    /**
     * 读取整数输入，限制范围
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt + " (" + min + "-" + max + ")");
            if (value >= min && value <= max) {
                return value;
            }
            printError("请输入 " + min + " 到 " + max + " 之间的数字");
        }
    }

    /**
     * 读取密码（不回显在普通控制台中无法实现，这里正常读取）
     */
    public static String readPassword(String prompt) {
        return readLine(prompt);
    }

    /**
     * 确认操作 (y/n)
     */
    public static boolean confirm(String prompt) {
        String answer = readLine(prompt + " (y/n)");
        return "y".equalsIgnoreCase(answer) || "yes".equalsIgnoreCase(answer);
    }

    /**
     * 从菜单中选择一项，返回 1-based 索引
     */
    public static int selectFromMenu(String[] options) {
        printMenu(options);
        return readInt("请选择", 1, options.length);
    }

    // ==================== 表格输出 ====================

    /**
     * 简易表格打印
     *
     * @param headers 表头
     * @param rows    数据行
     */
    public static void printTable(String[] headers, List<String[]> rows) {
        // 计算每列最大宽度
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, widths.length); i++) {
                widths[i] = Math.max(widths[i], row[i] != null ? row[i].length() : 0);
            }
        }

        // 打印表头
        StringBuilder headerLine = new StringBuilder("│ ");
        StringBuilder separator = new StringBuilder("├");
        for (int i = 0; i < headers.length; i++) {
            headerLine.append(padRight(headers[i], widths[i])).append(" │ ");
            separator.append("─".repeat(widths[i] + 2)).append("┼");
        }
        separator.deleteCharAt(separator.length() - 1);
        separator.append("┤");

        System.out.println(separator);
        System.out.println(BOLD + headerLine + RESET);
        System.out.println(separator);

        // 打印数据行
        for (String[] row : rows) {
            StringBuilder rowLine = new StringBuilder("│ ");
            for (int i = 0; i < headers.length; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                rowLine.append(padRight(cell, widths[i])).append(" │ ");
            }
            System.out.println(rowLine);
        }
        System.out.println(separator);
    }

    // ==================== 辅助方法 ====================

    private static String padRight(String text, int length) {
        if (text.length() >= length) {
            return text;
        }
        return text + " ".repeat(length - text.length());
    }

    private static String centerText(String text, int width) {
        int totalPadding = width - text.length();
        int leftPad = totalPadding / 2;
        int rightPad = totalPadding - leftPad;
        return " ".repeat(Math.max(0, leftPad)) + text + " ".repeat(Math.max(0, rightPad));
    }

    /**
     * 清屏（简单方式：输出多个空行）
     */
    public static void clearScreen() {
        System.out.println("\n".repeat(3));
    }

    /**
     * 暂停等待用户按键
     */
    public static void pause() {
        System.out.print("\n按回车键继续...");
        try {
            SCANNER.nextLine();
        } catch (java.util.NoSuchElementException e) {
            // 输入流耗尽，直接返回
        }
    }
}
