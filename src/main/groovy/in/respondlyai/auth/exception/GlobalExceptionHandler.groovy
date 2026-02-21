package in.respondlyai.auth.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler)

    @Value('${spring.profiles.active:production}')
    private String activeProfile

    private boolean isDev() {
        return activeProfile == 'local' || activeProfile == 'dev' || activeProfile == 'development'
    }

    // Handle custom ApiException 

    @ExceptionHandler(ApiException)
    ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API Error: {} [{}] {} - {}", ex.errorType, ex.status.value(), request.requestURI, ex.message)

        ApiErrorResponse response = new ApiErrorResponse(ex.message, ex.errorType)

        if (isDev()) {
            response.stack = getStackTraceAsString(ex)
            response.metadata = ex.metadata
            response.path = request.requestURI
            response.method = request.method
        }

        return new ResponseEntity<>(response, ex.status)
    }

    // Handle validation errors (@Valid) 

    @ExceptionHandler(MethodArgumentNotValidException)
    ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Collect all field errors
        Map<String, String> fieldErrors = [:]
        ex.bindingResult.fieldErrors.each { error ->
            fieldErrors[error.field] = error.defaultMessage
        }

        String message = fieldErrors.values().join(', ')

        log.warn("Validation Error: {} - {}", request.requestURI, message)

        ApiErrorResponse response = new ApiErrorResponse(message, ErrorType.VALIDATION_ERROR)
        response.metadata = [fields: fieldErrors]

        if (isDev()) {
            response.path = request.requestURI
            response.method = request.method
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST)
    }

    // Handle DB constraint violations (e.g. unique email) 

    @ExceptionHandler(DataIntegrityViolationException)
    ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Database constraint violation: {} - {}", request.requestURI, ex.message)

        String message = 'A record with this data already exists'

        // Try to extract constraint details
        String rootMessage = ex.rootCause?.message ?: ex.message
        if (rootMessage?.contains('email')) {
            message = 'Email already in use'
        } else if (rootMessage?.contains('user_id')) {
            message = 'User ID already exists'
        }

        ApiErrorResponse response = new ApiErrorResponse(message, ErrorType.CONFLICT)

        if (isDev()) {
            response.stack = getStackTraceAsString(ex)
            response.metadata = [constraint: rootMessage]
            response.path = request.requestURI
            response.method = request.method
        }

        return new ResponseEntity<>(response, HttpStatus.CONFLICT)
    }

    // Catch-all for unhandled exceptions 

    @ExceptionHandler(Exception)
    ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}: {}", request.method, request.requestURI, ex.message, ex)

        ApiErrorResponse response = new ApiErrorResponse(
                'An unexpected error occurred',
                ErrorType.INTERNAL_SERVER_ERROR
        )

        if (isDev()) {
            response.stack = getStackTraceAsString(ex)
            response.metadata = [exceptionType: ex.class.simpleName]
            response.path = request.requestURI
            response.method = request.method
        }

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    // Helper 

    private static String getStackTraceAsString(Exception ex) {
        StringWriter sw = new StringWriter()
        ex.printStackTrace(new PrintWriter(sw))
        return sw.toString()
    }
}
