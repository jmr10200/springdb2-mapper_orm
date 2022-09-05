package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.*;

@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager entityManager;
    // Querydsl 사용에는 JPAQueryFactory 가 필요하다.
    private final JPAQueryFactory queryFactory;

    public JpaItemRepositoryV3(EntityManager entityManager) {
        this.entityManager = entityManager;
        // JPAQueryFactory 사용에는 JPA 쿼리인 JPQL 을 만들기 때문에 EntityManager 필요하다.
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Item save(Item item) {
        entityManager.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setQuantity(updateParam.getQuantity());
        findItem.setPrice(updateParam.getPrice());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = entityManager.find(Item.class, id);
        return Optional.ofNullable(item);
    }

//    @Override
    public List<Item> findAllOld(ItemSearchCondition condition) {
        // Querydsl 을 사용해서 동적 쿼리 문제를 해결
        String itemName = condition.getItemName();
        Integer maxPrice = condition.getMaxPrice();

        QItem item = QItem.item;
        // BooleanBuilder 이용해서 where 조건을 설정
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(itemName)) {
            builder.and(item.itemName.like("%" + itemName + "%"));
        }
        if (maxPrice != null) {
            builder.and(item.price.loe(maxPrice));
        }

        List<Item> result = queryFactory
                .select(item)
                .from(item)
                .where(builder)
                .fetch();

        return result;
    }

    @Override
    public List<Item> findAll(ItemSearchCondition condition) {
        // findAllOld() 코드를 리팩토링
        String itemName = condition.getItemName();
        Integer maxPrice = condition.getMaxPrice();

        // Querydsl 장점 : 동적 쿼리, 컴파일 시점에 에러체크, 메소드추출로 코드 재사용
        List<Item> result = queryFactory
                .select(item) // QItem static import
                .from(item)
                .where(likeItemName(itemName), maxPrice(maxPrice)) // where 의 and 조건
                .fetch();

        return result;
    }

    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }
}
// 예외변환
// Querydsl 은 별도의 스프링 예외 추상화를 지원하지 않는다.
// @Repository 에서 스프링 예외 추상화를 처리해준다.
