package keepgoing.demo.domain.ai.service;

import keepgoing.demo.domain.ai.dto.AiRequestDto;
import keepgoing.demo.domain.ai.dto.AiResponseDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiClient {

    // Spring Boot 3.2+ 부터는 RestClient가 가장 편합니다.
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://127.0.0.1:8000") // 파이썬 서버 주소
            .build();

    public AiResponseDto requestAnalysis(AiRequestDto requestDto) {
        return restClient.post()
                .uri("/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .body(AiResponseDto.class);
    }
}