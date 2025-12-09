package keepgoing.demo.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // 모든 컨트롤러의 에러를 여기서 잡음
public class GlobalExceptionHandler {

    // 1. IllegalArgumentException 처리 (우리가 Service에서 throw 한 것들)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage()); // "회원이 존재하지 않습니다" 등이 담김

        return ResponseEntity.badRequest().body(response); // 400 Bad Request로 반환
    }

    // 2. 그 외 모든 알 수 없는 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        e.printStackTrace(); // 서버 로그에는 남김

        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

        return ResponseEntity.internalServerError().body(response); // 500 Error
    }
}