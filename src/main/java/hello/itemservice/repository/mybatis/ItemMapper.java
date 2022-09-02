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
