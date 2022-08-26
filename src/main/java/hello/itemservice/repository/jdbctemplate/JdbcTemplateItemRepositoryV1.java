package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTemplate
 *
 * ItemRepository Interface 를 구현
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    /**
     * JdbcTemplate 는 관례상 다음과 같이 생성자를 이용하여 사용함
     */
    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        // JdbcTemplate 는 DataSource 가 필요
        this.template = new JdbcTemplate(dataSource);
        // DataSource 를 의존 관계 주입받아 생성자 내부에서 JdbcTemplate 생성함
        // JdbcTemplate 를 스프링 빈으로 직접 등록하고 주입받는 방식도 OK
    }

    /**
     * Item 저장
     */
    @Override
    public Item save(Item item) {
        String sql = "insert into item (item_name, price, quantity) values (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        // template.update() 의 리턴 값은 row 수
        template.update(connection -> {
            // 자동 증가 PK : identity (auto increment)
            // DB 에서 key 를 생성해주므로 설정하지 않는다.
            PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"id"});
            pstmt.setString(1, item.getItemName());
            pstmt.setInt(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            return pstmt;
        }, keyHolder);

        // DB 에서 key 를 생성해주므로 INSERT 가 완료된 후 생성된 ID 를 조회할 수 있다.
        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";

        // 쿼리문의 조건이 id = ? 이므로 영향받은 row 는 무조건 1개이다.
        template.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId);
    }

    /**
     * 결과가 없으면, EmptyResultDataAccessException
     * 결과가 둘 이상이면, IncorrectResultSizeDataAccessException
     */
    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = ?";
        try {
            // template.queryForObject() : 결과 row 가 1개 일 때 사용
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            // 결과가 없는 경우, Optional 을 반환해야 하므로 공백 리턴
            return Optional.empty();
        }
    }

    /**
     * DB 조회 결과를 객체로 변환할 때 사용
     * @return ResultSet 객체
     */
    private RowMapper<Item> itemRowMapper() {
        // JDBC 를 직접 사용할 때 ResultSet 를 사용했던 부분
        // 차이가 있다면 다음과 같이 JdbcTemplate 의 loop 를 돌려주고,
        // 개발자는 RowMapper 를 구현해서 그 내부 코드만 채운다
        // while(resultSet 이 끝날 때 까지) { rowMapper(rs, rowNum) }
        return (rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        };
    }

    @Override
    public List<Item> findAll(ItemSearchCondition searchCondition) {
        String itemName = searchCondition.getItemName();
        Integer maxPrice = searchCondition.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";
        // dynamic query
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', ?, '%')";
            param.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);
        }
        log.info("sql={}", sql);
        // template.query()
        // - 결과가 하나 이상일 때 사용
        // - RowMapper : DB 리턴 결과인 ResultSet 객체로 변환
        // - 결과가 없으면 빈 Collection 반환
        return template.query(sql, itemRowMapper(), param.toArray());
    }
}
/* findAll() : 동적쿼리 문제 */
// 4가지 상황에 따른 SQL 을 동적으로 생성해야 한다.
// where, and 의 설정 및 파라미터 등 실무상 설정하기 힘들다.

// 1. 검색 조건이 없음
// select if, item_name, price, quantity from item

// 2. 상품명 (item_name) 으로 검색
// select id, item_name, price, quantity from item
// where item_name like concat ('%', ?, '%')

// 3. 최대 가격 (maxPrice) 으로 검색
// select id, item_name, price, quantity from item
// where price <= ?

// 4. 상품명 (item_name), 최대 가격 (maxPrice) 둘다 검색
// select id, item_name, price, quantity from item
// where item_name like concat ('%', ?, '%')
//   and price <= ?


