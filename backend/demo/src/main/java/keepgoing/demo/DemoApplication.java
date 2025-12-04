package keepgoing.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// DB 연결 안한 상태에서 개발할 때의 설정
// DB 연결하면 (exclude = {DataSourceAutoConfiguration.class}) 제거
@SpringBootApplication
@MapperScan(basePackages = "keepgoing.demo")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
