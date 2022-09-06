package hello.itemservice.repository.v2;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemSearchCondition;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ItemQueryRepositoryV2 {

    private final JPAQueryFactory queryFactory;

    public ItemQueryRepositoryV2(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    // Querydsl 이용해서 복잡한 쿼리 문제 해결결

   public List<Item> findAll(ItemSearchCondition condition) {
        return queryFactory
                .select(QItem.item)
                .from(QItem.item)
                .where(
                        maxPrice(condition.getMaxPrice()),
                        likeItemName(condition.getItemName()))
                .fetch();
    }

    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return QItem.item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
            return QItem.item.price.loe(maxPrice);
        }
        return null;
    }
}
