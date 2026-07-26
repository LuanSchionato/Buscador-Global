package geopolitico.exception;

public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public ApiException(String message, int statusCode) {
        super(message + " (HTTP " + statusCode + ")");
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
