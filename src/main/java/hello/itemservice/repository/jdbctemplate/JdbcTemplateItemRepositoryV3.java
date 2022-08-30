package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SimpleJdbcInsert
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        // DataSource 를 주입받아 내부에서 생성한다. (스프링의 관례상 생성 방법)
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item") // 테이블 명 지정
                .usingGeneratedKeyColumns("id"); // key 생성하는 PK 컬럼명 지정
//                .usingColumns("item_name", "price", "quantity"); // INSERT 에 사용할 컬럼 지정, 생략가능
        // SimpleJdbcInsert 는 생성 시점에 DB 테이블의 메타 데이터를 조회한다.
        // 따라서 어떤 컬럼이 있는지 확인할 수 있으므로 usingColumns 를 생략할 수 있다.
        // 만약 특정 컬럼만 지정해서 저장하고 싶다면 usingColumns 를 사용하면 된다.
    }

    @Override
    public Item save(Item item) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        // INSERT SQL 생성하고 생성된 키 값도 편리하게 조회할 수 있다.
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name = :itemName, price = :price, quantity = :quantity where id = :id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);
        template.update(sql, param);
    }

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

    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
    }

    @Override
    public List<Item> findAll(ItemSearchCondition condition) {
        Integer maxPrice = condition.getMaxPrice();
        String itemName = condition.getItemName();

        SqlParameterSource param = new BeanPropertySqlParameterSource(condition);

        String sql = "select id, item_name, price quantity from item";

        // 동적 쿼리
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
}
