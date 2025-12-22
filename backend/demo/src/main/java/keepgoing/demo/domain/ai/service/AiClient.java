package keepgoing.demo.domain.ai.service;

import keepgoing.demo.domain.ai.dto.AiAnalyzeDto;
import keepgoing.demo.domain.ai.dto.AiRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiClient {

    // Python FastAPI 주소
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8000")
            .build();

    // 1. [기존] 식단 분석 ( /api/diet/analyze )
    public AiAnalyzeDto requestAnalysis(AiRequestDto requestDto) {
        return restClient.post()
                .uri("/api/diet/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .body(AiAnalyzeDto.class); // 결과를 DTO 클래스로 바로 매핑
    }

    // 2. [New] 식단 생성 ( /api/diet/generate )
    public List requestDietGeneration(AiRequestDto requestDto) {
        // Python이 원하는 필드(exercise, duration 등)는
        // AiRequestDto 내부의 UserProfile과 SurveyData에서 매핑해서 보내야 함
        // 여기서는 편의상 requestDto 전체를 보내고 Python이 알아서 파싱하거나,
        // DTO에서 Map으로 변환하여 전송하는 로직이 필요할 수 있습니다.

        // 간단하게 DTO 전체 전송 (Python Pydantic이 필터링함)
        return restClient.post()
                .uri("/api/diet/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapDtoToGenerateRequest(requestDto)) // 매핑 메서드 하단 참조
                .retrieve()
                .body(List.class);
    }

    // 3. [New] 바디 스캔 ( /api/body/scan )
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

    // 4. [New] 음식 사진 스캔 ( /api/diet/scan )
    public Map requestFoodScan(MultipartFile file) {
        MultiValueMap<String, Resource> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        return restClient.post()
                .uri("/api/diet/scan")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    // Helper: 식단 생성용 데이터 매핑
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