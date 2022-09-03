package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ItemMapper {

    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCondition itemSearch);

}
// @Mapper : MyBatis Mapping XML 을 호출해주는 Mapper Interface
// xml 파일의 해당 SQL 을 실행하고 결과를 반환한다.
// xml 파일은 같은 위치에 만들어주면 된다.
// src/main/resource/hello/itemservice/repository/mybatis/ItemMapper.xml

// XML 파일의 경로를 수정하고 싶은 경우
// application.properties 에 다음과 같이 설정하면 된다. (resource/mapper 포함한 하위 폴더의 xml)
// mybatis.mapper-locations=classpath:mapper/**/*.xml

/* ItemMapper 인터페이스의 구현체 없이 동작하는 원리 */
// 1. 어플리케이션 로딩 시점에 MyBatis 스프링 연동 모듈은 @Mapper 가 붙어있는 인터페이스를 조사한다.
// 2. 해당 인터페이스가 발견되면 동적 프록시 기술을 사용해서 ItemMapper 인터페이스의 구현체를 만든다.
// 3. 생성된 구현체를 스프링 빈으로 등록한다.

// 매퍼 구현체
// MyBatis 스프링 연동 모듈이 만들어주는 ItemMapper 구현체 덕분에 인터페이스 만으로 XML 의 데이터를 찾아 호출 가능하다.
// 매퍼 구현체는 MyBatis 에서 발생한 예외를 스프링 예외 추상화인 DataAccessException 에 맞게 변환해서 반환해준다.
// JdbcTemplate 이 제공하는 예외 변환 기능을 제공한다고 생각하면 된다.

// 정리
// 매퍼 구현체 덕분에 MyBatis 를 스프링에 편리하게 통합해서 사용할 수 있다.
// 매퍼 구현체를 사용하면 스프링 예외 추상화도 함께 적용된다.
// 마이바티스 스프링 연동 모듈이 많은 부분을 자동으로 설정해주는데, DB 커넥션, 트랜젝션 관련 기능도 함께 연동되고 동기화 해준다.
