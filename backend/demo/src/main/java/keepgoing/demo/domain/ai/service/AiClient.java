package keepgoing.demo.domain.ai.service;

import keepgoing.demo.domain.ai.dto.AiAnalyzeDto;
import keepgoing.demo.domain.ai.dto.AiRequestDto;
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

    // 1. 식단 분석
    public AiAnalyzeDto requestAnalysis(AiRequestDto requestDto) {
        return restClient.post()
                .uri("/api/diet/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .body(AiAnalyzeDto.class);
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

    // 3. 바디 스캔 (RestTemplate + Header 적용 버전)
    public Map<String, Object> requestBodyScan(AiRequestDto requestDto) {

        // 1. 데이터 매핑 (DTO -> Map)
        // 파이썬 서버가 분석에 필요한 모든 데이터를 Map에 담습니다.
        Map<String, Object> body = new HashMap<>();

        if (requestDto.profile() != null) {
            body.put("height", requestDto.profile().height());
            body.put("weight", requestDto.profile().weight());
            body.put("age", requestDto.profile().age());
            body.put("gender", requestDto.profile().gender());
            body.put("goal", requestDto.profile().goal());

            // [중요] 식단 생성 때처럼 'activity'를 'exercise'로 매핑해야 할 수도 있습니다.
            // 파이썬 서버가 'activity'를 받는지 'exercise'를 받는지 확인 필요하지만,
            // 보통 같은 변수명을 쓸 확률이 높으니 둘 다 보내거나 식단 때와 똑같이 맞춥니다.
            body.put("exercise", requestDto.profile().activity());
            body.put("activity", requestDto.profile().activity()); // 혹시 몰라 둘 다 넣음 (안전빵)
        }

        // 2. 헤더 설정 (JSON 명시 - 422 에러 방지 핵심)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. Body와 Header를 합침 (택배 포장)
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // 4. 요청 전송 (로그 추가)
            System.out.println("🚀 바디 스캔 요청 전송: " + body);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PYTHON_URL + "/api/body/scan",
                    entity,
                    Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            // 에러 발생 시 로그를 자세히 찍어서 원인 파악
            log.error("❌ 바디 스캔 요청 실패: ", e);
            throw new RuntimeException("바디 스캔 중 오류 발생: " + e.getMessage());
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