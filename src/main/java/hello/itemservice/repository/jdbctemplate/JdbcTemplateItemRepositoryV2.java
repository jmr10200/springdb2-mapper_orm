package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NamedParameterJdbcTemplate
 * SqlParameterSource
 * - BeanPropertySqlParameterSource
 * - MapSqlParameterSource
 * Map
 *
 * BeanPropertyRowMapper
 *
 * ItemRepository interface 구현
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        // DataSource 주입받아 생성자로 생성
        // 관례상 많이 사용하는 방법
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Item 저장
     *
     * 이름지정 파라미터 : BeanPropertySqlParameterSource 사용
     */
    @Override
    public Item save(Item item) {
        // 파라미터를 :parameterName 으로 설정
        String sql = "insert into item (item_name, price, quantity) values (:itemName, :price, :quantity)";

        // BeanPropertySqlParameterSource 사용
        // 자바빈 프로퍼티 규약을 통해서 자동으로 파라미터 객체를 생성한다.
        // Ex) getXxx() -> xxx , getItemName() -> itemName
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        // BeanPropertySqlParameterSource 가 많은 것을 자동화 해주기 때문에 가장 편해보이지만 항상 사용할 수는 없다.
        // 예를들어 update() 에서는 SQL 에 :id 를 바인딩 해야하는데,
        // update() 에서 사용하는 ItemUpdateDto 에는 itemId 가 없다.
        // 따라서 대신에 MapSqlParameterSource 를 사용한다.

        Long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    /**
     * item 업데이트
     *
     * 이름지정 파라미터 : MapSqlParameterSource
     */
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name = :itemName, price = :price, quantity = :quantity where id = :id";

        // MapSqlParameterSource
        // Map 과 유사한데, SQL 타입을 지정할 수 있는 등 SQL 에 좀더 특화된 기능을 제공한다.
        // SqlParameterSource 의 구현체이다.
        // 메소드 체인을 통해 편리한 사용법도 제공한다.
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId); //이 부분이 별도로 필요하다.
        template.update(sql, param);
    }

    /**
     * 이름지정 파라미터 : Map 사용
     *
     * 결과가 없으면, EmptyResultDataAccessException
     * 결과가 둘 이상이면, IncorrectResultSizeDataAccessException
     */
    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = :id";
        try {
            Map<String, Object> param = Map.of("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCondition searchCondition) {
        String itemName = searchCondition.getItemName();
        Integer maxPrice = searchCondition.getMaxPrice();

        SqlParameterSource param = new BeanPropertySqlParameterSource(searchCondition);

        String sql = "select id, item_name, price, quantity from item";
        // dynamic query
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', :itemName, '%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
        }
        log.info("sql={}", sql);
        return template.query(sql, param, itemRowMapper());
    }

    /**
     * BeanPropertyRowMapper
     */
    private RowMapper<Item> itemRowMapper() {
        // Camel case 변환 지원
        return BeanPropertyRowMapper.newInstance(Item.class);

        // BeanPropertyRowMapper
        // ResultSet 의 결과를 받아서 자바빈 규약에 맞추어 데이터를 변환한다.
        // 예를들어 DB 에서 조회한 결과가 select id, price 라고 한다면 다음과 같은 코드를 작성해준다.
        // (실제로는 리플렉션 같은 기능을 사용)
        // Item item = new Item();
        // item.setId(rs.getLong("id"));
        // item.setPrice(rs.getInt("price"));
        // DB 에서 조회한 결과 이름을 기반으로 setId(), setPrice() 처럼 자바빈 프로퍼티 규약에 맞춘 메소드를 호출한다.

        // 그런데 select item_name 의 경우 setItem_name() 이라는 메소드가 없을때는 어떻게 해야할까?
        // 그럴때는 SQL 의 별칭을 사용한다.
        // select item_name as itemName
        // 보통 sql 은 snake_case 를 사용하고, 자바는 camelCase 를 사용하므로 별칭을 사용하면 쉽게 문제를 해결해준다.

    }

}