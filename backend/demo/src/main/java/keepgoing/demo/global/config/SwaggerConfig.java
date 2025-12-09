package keepgoing.demo.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KeepGoing AI 식단 분석 API")
                        .description("사용자의 식단을 분석하고 추천해주는 서비스 API 문서입니다.")
                        .version("1.0.0"));
    }
}
