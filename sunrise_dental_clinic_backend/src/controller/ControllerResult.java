package controller;

/**
 * Shared success/message/data envelope returned by every controller method
 * that can fail, so each Swing view only has one shape to branch on
 * (isSuccess() / getMessage() / getData()) instead of each controller
 * inventing its own result type.
 */
public final class ControllerResult<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private ControllerResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ControllerResult<T> success(T data) {
        return new ControllerResult<>(true, "OK", data);
    }

    public static <T> ControllerResult<T> failure(String message) {
        return new ControllerResult<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
