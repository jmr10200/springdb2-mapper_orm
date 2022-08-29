package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

//@Import(MemoryConfig.class) // MemoryConfig 를 설정 파일로 사용
//@Import(JdbcTemplateV1Config.class) // JdbcTemplateV1Config 를 설정 파일로 사용
@Import(JdbcTemplateV2Config.class) // JdbcTemplateV1Config 를 설정 파일로 사용
// 컨트롤러만 컴포넌트 스캔 사용, 나머지는 수동 빈 등록
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	@Bean
	@Profile("local") // profile 지정
	public TestDataInit testDataInit(ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}

}
/* @Profile */
// 스프링은 로딩 시점에 application.properties 의 spring.profile.active 속성을 읽어 프로필로 사용한다.
// 로컬, 운영 환경, 테스트 실행 등 여러 황경에 따라 다른 설정을 할 때 사용
// 예를들어 개발 환경 DB, 운영 환경 DB, 테스트 환경 DB 는 모두 다르다. 이때 설정위해 사용한다.