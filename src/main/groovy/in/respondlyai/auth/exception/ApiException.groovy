package in.respondlyai.auth.exception

import org.springframework.http.HttpStatus

/**
 * Custom exception class with factory methods for common error types.
 */
class ApiException extends RuntimeException {

    final HttpStatus status
    final ErrorType errorType
    final Map<String, Object> metadata

    ApiException(String message, HttpStatus status, ErrorType errorType, Map<String, Object> metadata = null) {
        super(message)
        this.status = status
        this.errorType = errorType
        this.metadata = metadata
    }

    static ApiException validationError(String message, Map<String, Object> metadata = null) {
        return new ApiException(message, HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR, metadata)
    }

    static ApiException authError(String message) {
        return new ApiException(message, HttpStatus.UNAUTHORIZED, ErrorType.AUTH_ERROR)
    }

    static ApiException forbidden(String message) {
        return new ApiException(message, HttpStatus.FORBIDDEN, ErrorType.FORBIDDEN)
    }

    static ApiException notFound(String message) {
        return new ApiException(message, HttpStatus.NOT_FOUND, ErrorType.NOT_FOUND)
    }

    static ApiException conflict(String message) {
        return new ApiException(message, HttpStatus.CONFLICT, ErrorType.CONFLICT)
    }

    static ApiException badRequest(String message) {
        return new ApiException(message, HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST)
    }

    static ApiException internalError(String message, Map<String, Object> metadata = null) {
        return new ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL_SERVER_ERROR, metadata)
    }

    static ApiException databaseError(String message, Map<String, Object> metadata = null) {
        return new ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.DATABASE_ERROR, metadata)
    }

    static ApiException serviceUnavailable(String message, Map<String, Object> metadata = null) {
        return new ApiException(message, HttpStatus.SERVICE_UNAVAILABLE, ErrorType.SERVICE_UNAVAILABLE, metadata)
    }
}
