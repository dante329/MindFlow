package exception;

/**
 * 数据未找到异常
 * 当查询的数据不存在时抛出
 */
public class DataNotFoundException extends BusinessException {

    public DataNotFoundException(String entityName, Object id) {
        super("DATA_NOT_FOUND", entityName + " 不存在 (ID: " + id + ")");
    }

    public DataNotFoundException(String message) {
        super("DATA_NOT_FOUND", message);
    }
}
