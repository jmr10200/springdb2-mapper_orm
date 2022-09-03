package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional // 데이터 변경시에는 트랜젝션 안에서 (조회는 없이도 가능)
// (참고) 일반적으로는 비즈니스 로직을 시작하는 서비스 계층에서 트랜젝션을 걸어준다.
public class JpaItemRepositoryV1 implements ItemRepository {

    private final EntityManager entityManager;

    public JpaItemRepositoryV1(EntityManager entityManager) {
        // 스프링 통해 엔티티매니저 주입 받음
        // entityManager 는 내부에 DataSource 가지고있고, DB 접근 가능하다.
        this.entityManager = entityManager;
        // 참고
        // JPA 설정에는 EntityManagerFactory, JpaTransactionManager, DataSource 등 다양한 설정이 필요하다.
        // 스프링 부트가 이 과정들을 자동화 해준다.
    }

    @Override
    public Item save(Item item) {
        // em.persist() : JPA 에서 객체를 테이블에 저장하는 메소드
        entityManager.persist(item);
        // 실행 결과 로그 : insert into item (id, item_name, price, quantity) values (default, ?, ?, ?)
        // PK 생성 전략을 IDENTITY 로 사용했기 때문에 id 값이 빠져있다.
        // 쿼리 실행 후, Item 객체의 id 필드에 DB 가 생성한 PK 값이 들어가게 된다.
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = entityManager.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
        // em.update() 같은 메소드를 호출하지 않았는데 UPDATE SQL 이 실행된다.
        // JPA 는 트랜젝션이 커밋되는 시점에 변경된 엔티티 객체가 있는지 확인하고, 변경된 경우 SQL 을 실행한다.
        // JPA 가 변경된 엔티티 객체를 찾는 과정은 영속성 컨텍스트라는 JPA 내부 원리에 의한다.
        // 테스트의 경우 마지막에 트랜젝션이 rollback 되므로 @Commit 으로 확인해야 한다.
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = entityManager.find(Item.class, id);
        return Optional.ofNullable(item);
        // em.find() : JPA 에서 엔티티 객체를 PK 기준으로 조회할 때 조회타입, PK 지정으로 사용
        // JPA 가 SELECT SQL 을 생성해 실행해주고 결과를 조회타입 객체로 바로 변환해준다.
        // 참고)
        // item0_.id as id1_0_0 과 같이 JPA(Hibernate) 가 만들어 실행하는 SQL 은 별칭이 복잡하다.
        // 이는, JOIN 등 복잡한 조건에서도 문제없도록 기계적으로 만들다보니 이러한 결과가 나온 듯 하다.
    }

    @Override
    public List<Item> findAll(ItemSearchCondition cond) {
        String jpql = "select i from Item i";

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%', :itemName, '%')";
            param.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
            param.add(maxPrice);
        }

        log.info("jpql={}", jpql);

        TypedQuery<Item> query = entityManager.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
        // JPQL (Java Persistence Query Language) 객체지향 쿼리 언어
        // 주로 여러 데이터를 복잡한 조건으로 조회할 때 사용
        // SQL 이 테이블 대상이라면, JPQL 은 엔티티 객체를 대상으로 SQL 을 실행한다 생각하자.
        // 엔티티 객체를 대상으로 하기 때문에 from 다음에 Item 엔티티 객체 이름이 들어간다.
        // 엔티티 객체와 속성의 대소문자는 구분해야 한다.
        // SQL 과 문법이 거의 비슷하다.

        // 로그로 확인한 실행된 JPQL
        // select i from Item i where i.itemName like concat('%', :itemName, '%') and i.price <= :maxPrice
        // JPQL 통해 생성된 SQL
        // select item0_.id as id1_0_, item0_.item_name as item_nam2_0_, item0_.price as price3_0_, item0_.quantity as quantity4_0_ from item item0_ where (item0_.item_name like ('%'||?||'%')) and item0_.price<=?

    }
}
