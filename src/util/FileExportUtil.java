package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 文件导出工具类
 * 支持将学习数据导出为文本/CSV格式
 */
public class FileExportUtil {

    private static final String EXPORT_DIR = "exports";
    private static final DateTimeFormatter FILE_NAME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private FileExportUtil() {
        // 工具类不允许实例化
    }

    /**
     * 导出为 CSV 文件
     *
     * @param prefix  文件名前缀
     * @param headers 表头
     * @param rows    数据行
     * @return 导出文件的路径
     */
    public static String exportCsv(String prefix, String[] headers, List<String[]> rows) throws IOException {
        ensureExportDir();
        String fileName = prefix + "_" + LocalDateTime.now().format(FILE_NAME_FMT) + ".csv";
        Path filePath = Paths.get(EXPORT_DIR, fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            // BOM for Excel 兼容
            writer.write('\uFEFF');
            // 写表头
            writer.write(String.join(",", escapeCsvFields(headers)));
            writer.newLine();
            // 写数据行
            for (String[] row : rows) {
                writer.write(String.join(",", escapeCsvFields(row)));
                writer.newLine();
            }
        }

        return filePath.toString();
    }

    /**
     * 导出为纯文本报告
     *
     * @param prefix  文件名前缀
     * @param content 报告内容
     * @return 导出文件的路径
     */
    public static String exportText(String prefix, String content) throws IOException {
        ensureExportDir();
        String fileName = prefix + "_" + LocalDateTime.now().format(FILE_NAME_FMT) + ".txt";
        Path filePath = Paths.get(EXPORT_DIR, fileName);

        Files.writeString(filePath, content, StandardCharsets.UTF_8);
        return filePath.toString();
    }

    private static void ensureExportDir() throws IOException {
        Path dir = Paths.get(EXPORT_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private static String[] escapeCsvFields(String[] fields) {
        String[] escaped = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i] != null ? fields[i] : "";
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                field = "\"" + field.replace("\"", "\"\"") + "\"";
            }
            escaped[i] = field;
        }
        return escaped;
    }
}
