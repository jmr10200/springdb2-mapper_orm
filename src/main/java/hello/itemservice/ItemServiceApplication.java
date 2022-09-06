package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Slf4j
//@Import(MemoryConfig.class) // MemoryConfig 를 설정 파일로 사용
//@Import(JdbcTemplateV1Config.class) // JdbcTemplateV1Config 를 설정 파일로 사용
//@Import(JdbcTemplateV2Config.class) // JdbcTemplateV2Config 를 설정 파일로 사용
//@Import(JdbcTemplateV3Config.class) // JdbcTemplateV3Config 를 설정 파일로 사용
//@Import(MyBatisConfig.class) // MyBatis
//@Import(JpaConfig.class) // JPA
//@Import(SpringDataJpaConfig.class) // Spring Data JPA
//@Import(QuerydslConfig.class)
@Import(V2Config.class)
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

	/**
	 * 임베디드 모드 DB
	 */
/*
	@Bean
	@Profile("test")
	public DataSource dataSource() {
		log.info("메모리 데이터베이스 초기화");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
		// jdbc:h2:mem:db : dataSource 를 만들때 임베디드 모드(메모리 모드)로 동작하는 설정
		// DB_CLOSE_DELAY=-1 : 임베디드 모드에서는 데이터베이스 커넥션 연결이 모두 끊어지면 데이터베이스도 종료되는데, 그것을 방지하는 설정이다
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		return dataSource;
	}
*/

}
/* @Profile */
// 스프링은 로딩 시점에 application.properties 의 spring.profile.active 속성을 읽어 프로필로 사용한다.
// 로컬, 운영 환경, 테스트 실행 등 여러 황경에 따라 다른 설정을 할 때 사용
// 예를들어 개발 환경 DB, 운영 환경 DB, 테스트 환경 DB 는 모두 다르다. 이때 설정위해 사용한다.

/* 임베디드 모드 DB */
// H2 데이터베이스는 자바로 개발되어 있고, JVM 안에서 메모리 모드로 동작하는 특별한 기능을 제공한다.
// 그래서 어플리케이션을 실행할 때 H2 DB 도 해당 JVM 메모리에 포함해서 함께 실행할 수 있다.
// DB 를 어플리케이션에 내장해서 함께 실행한다고 해서 임베디드 모드(Embedded Mode) 라 한다.
// 어플리케이션이 종료되면 임베디드 모드로 동작하는 H2 DB 도 함께 종료되고, 데이터도 모두 사라진다.
// 자바 메모리를 함께 사용하는 라이브러리처럼 동작한다.

// 참고 - 에러
// nested exception is org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "ITEM" not found; SQL statement
// -> 메모리 DB 에 테이블을 생성하지 않아서 발생한다.
// 스프링부트는 SQL 스크립트를 실행해서 어플리케이션 로딩 시점에 DB 초기화 하는 기능을 제공한다.
// src/test/resource/schema.sql 추가 (파일명도 맞아야 함)