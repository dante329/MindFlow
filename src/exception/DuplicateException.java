package exception;

/**
 * 数据重复异常
 * 当尝试创建已存在的数据时抛出，如用户名重复
 */
public class DuplicateException extends BusinessException {

    public DuplicateException(String entityName, String field, Object value) {
        super("DUPLICATE", entityName + " 的 " + field + " '" + value + "' 已存在");
    }

    public DuplicateException(String message) {
        super("DUPLICATE", message);
    }
}
