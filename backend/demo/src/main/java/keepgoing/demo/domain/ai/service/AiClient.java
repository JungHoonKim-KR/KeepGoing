package keepgoing.demo.domain.ai.service;

import keepgoing.demo.domain.ai.dto.AiAnalyzeDto;
import keepgoing.demo.domain.ai.dto.AiRequestDto;
import keepgoing.demo.domain.ai.dto.BodyScanResponse;
import keepgoing.demo.domain.diet.dto.FoodAnalysisResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AiClient {

    // JSON 요청용 RestClient (분석, 바디스캔 등에서 사용)
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8000")
            .build();

    // Multipart 및 헤더 수동 제어용 RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PYTHON_URL = "http://localhost:8000";

    // =================================================================
// 1. 식단 분석 (수정됨: RestTemplate + Header 적용으로 422 에러 방지)
// =================================================================
    public AiAnalyzeDto requestAnalysis(AiRequestDto requestDto) {
        // 1. 헤더 설정 (JSON 타입 명시)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Body와 Header를 합침
        // (DTO 객체를 넣으면 RestTemplate이 알아서 JSON으로 변환해 줍니다)
        HttpEntity<AiRequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            // 3. 요청 전송
            ResponseEntity<AiAnalyzeDto> response = restTemplate.postForEntity(
                    PYTHON_URL + "/api/diet/analyze",
                    entity,
                    AiAnalyzeDto.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("식단 분석 요청 실패: ", e);
            // 필요에 따라 구체적인 예외 처리를 하거나 커스텀 예외를 던지세요
            throw new RuntimeException("식단 분석 중 오류 발생");
        }
    }

    // =================================================================
    // 2. 식단 생성 (수정됨: 422 에러 해결을 위해 RestTemplate + Header 사용)
    // =================================================================
    public List requestDietGeneration(AiRequestDto requestDto) {
        // 1. DTO -> Map 변환
        Map<String, Object> requestBody = mapDtoToGenerateRequest(requestDto);

        // 2. 헤더 설정 (JSON 명시 - 이 부분이 없어서 422 에러가 났던 것)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. Body와 Header를 합침
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. 요청 전송
            ResponseEntity<List> response = restTemplate.postForEntity(
                    PYTHON_URL + "/api/diet/generate",
                    entity,
                    List.class
            );
            return response.getBody();

        } catch (Exception e) {
            log.error("식단 생성 요청 실패: ", e);
            throw new RuntimeException("식단 생성 중 오류 발생");
        }
    }
    public BodyScanResponse requestBodyScan(AiRequestDto requestDto) {

        // 1. 데이터 매핑 (DTO -> Map)
        Map<String, Object> body = new HashMap<>();

        // (1) 프로필 정보 매핑
        if (requestDto.profile() != null) {
            body.put("height", requestDto.profile().height());
            body.put("weight", requestDto.profile().weight());
            body.put("age", requestDto.profile().age());
            body.put("gender", requestDto.profile().gender());
            body.put("goal", requestDto.profile().goal());
            body.put("activity", requestDto.profile().activity());
        }

        // (2) [추가됨] 설문 정보 매핑 (수면, 물, 식습관 등)
        // -> 이게 있어야 "미래 예측"과 "취약 부위" 분석이 가능합니다.
        if (requestDto.survey() != null) {
            body.put("sleep", requestDto.survey().sleep());
            body.put("water", requestDto.survey().water());
            body.put("meals", requestDto.survey().meals());
            body.put("favorite", requestDto.survey().favorite());
        }

        // 2. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. 포장 (Entity 생성)
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            System.out.println("🚀 [Java] 바디 스캔 요청 데이터: " + body);

            // 4. 요청 전송 (반환 타입을 DTO로 지정)
            ResponseEntity<BodyScanResponse> response = restTemplate.postForEntity(
                    PYTHON_URL + "/api/body/scan",
                    entity,
                    BodyScanResponse.class // Map.class 대신 DTO 사용 권장
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("❌ 바디 스캔 요청 실패: ", e);
            // 실패 시 빈 객체라도 반환하거나 예외 처리
            throw new RuntimeException("AI 서버 연결 실패: " + e.getMessage());
        }
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

            // 더미 데이터 반환
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

    public FoodAnalysisResponseDto requestFoodAnalysis(String foodName) {

        Map<String, String> body = new HashMap<>();
        body.put("foodName", foodName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        try{
            ResponseEntity<FoodAnalysisResponseDto> response = restTemplate.postForEntity(
                    PYTHON_URL + "/api/food/create",
                    entity,
                    FoodAnalysisResponseDto.class
            );
            return response.getBody();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("DTO 매핑 오류 발생: " + e.getMessage());
        }
    }

    // Helper
    private Map<String, Object> mapDtoToGenerateRequest(AiRequestDto dto) {
        log.info(String.valueOf(dto.survey().duration()));
        log.info(String.valueOf(dto.survey().sleep()));
        log.info(String.valueOf(dto.survey().water()));
        log.info(String.valueOf(dto.survey().meals()));
        log.info(String.valueOf(dto.survey().favorite()));

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

        System.out.println("생성된 맵 데이터: " + map.toString());
        return map;
    }


}