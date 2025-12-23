package keepgoing.demo.domain.ai.service;

import keepgoing.demo.domain.ai.dto.AiAnalyzeDto;
import keepgoing.demo.domain.ai.dto.AiRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiClient {

    // JSON 요청용 RestClient
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8000")
            .build();

    // Multipart 요청용 RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PYTHON_URL = "http://localhost:8000";

    // 1. 식단 분석
    public AiAnalyzeDto requestAnalysis(AiRequestDto requestDto) {
        return restClient.post()
                .uri("/api/diet/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .body(AiAnalyzeDto.class);
    }

    // 2. 식단 생성
    public List requestDietGeneration(AiRequestDto requestDto) {
        return restClient.post()
                .uri("/api/diet/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapDtoToGenerateRequest(requestDto))
                .retrieve()
                .body(List.class);
    }

    // 3. 바디 스캔
    public Map requestBodyScan(AiRequestDto requestDto) {
        Map<String, Object> body = new HashMap<>();
        if (requestDto.profile() != null) {
            body.put("height", requestDto.profile().height());
            body.put("weight", requestDto.profile().weight());
        }

        return restClient.post()
                .uri("/api/body/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    // 4. 음식 스캔 (RestTemplate 사용)
    public Map<String, Object> requestFoodScan(MultipartFile file) {
        System.out.println("========== 음식 스캔 시작 ==========");
        System.out.println("파일명: " + file.getOriginalFilename());
        System.out.println("파일 크기: " + file.getSize() + " bytes");

        try {
            // 1. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 2. Body 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 3. ByteArrayResource 생성
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ?
                            file.getOriginalFilename() : "image.jpg";
                }
            };

            body.add("file", resource);

            // 4. HttpEntity 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 5. POST 요청 (RestTemplate 사용)
            String url = PYTHON_URL + "/api/diet/scan";
            System.out.println("요청 URL: " + url);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    Map.class
            );

            System.out.println("========== 응답 성공 ==========");
            System.out.println(response.getBody());

            return response.getBody();

        } catch (IOException e) {
            System.err.println("❌ 파일 읽기 실패: " + e.getMessage());
            throw new RuntimeException("파일 처리 오류", e);
        } catch (Exception e) {
            System.err.println("❌ 음식 스캔 실패: " + e.getMessage());
            e.printStackTrace();

            // 더미 데이터 반환 (에러 발생 시 앱이 멈추지 않도록)
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("name", "분석 실패");
            errorResponse.put("emoji", "❌");
            errorResponse.put("calories", 0);
            Map<String, Integer> exercise = new HashMap<>();
            exercise.put("running", 0);
            exercise.put("walking", 0);
            exercise.put("swimming", 0);
            errorResponse.put("exercise", exercise);

            return errorResponse;
        }
    }

    // Helper
    private Map<String, Object> mapDtoToGenerateRequest(AiRequestDto dto) {
        Map<String, Object> map = new HashMap<>();
        if (dto.survey() != null) {
            map.put("duration", dto.survey().duration());
            map.put("sleep", dto.survey().sleep());
            map.put("water", dto.survey().water());
            map.put("meals", dto.survey().meals());
            map.put("favorite", dto.survey().favorite());
        }
        if (dto.profile() != null) {
            map.put("exercise", dto.profile().activity());
        }
        return map;
    }
}