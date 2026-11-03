package exception;

/**
 * 业务逻辑异常基类
 * 用于表示可预期的业务错误，如用户不存在、密码错误等
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BIZ_ERROR";
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BIZ_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
