package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// Spring Data JPA 가 제공 하는 JpaRepository 상속
public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

    // 이름 조건 검색 쿼리
    // 다음과같은 JPQL 실행 : select i from Item i where i.name like ?
    List<Item> findByItemNameLike(String itemName);

    // 가격 조건 검색 쿼리
    // 다음과같은 JPQL 실행 : select i from Item i where i.price <= ?
    List<Item> findByPriceLessThanEqual(Integer price);

    // 쿼리 메소드 (아래 메소드와 같은 기능 수행)
    // 다음과 같은 JPQL 실행 : select i from Item i where i.itemName like ? and i.price <= ?
    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

    // 쿼리 직접 실행
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);
    // 메소드명으로 쿼리를 실행하는 기능의 단점
    // 1. 조건이 많으면 메소드명이 길어진다.
    // 2. 조인 같은 복잡한 조건 사용에 무리가 따른다.
    // 이경우 직접 JPQL 쿼리를 작성하도록 @Query 가 제공된다.
    // 메소드 명으로 쿼리를 실행할 때는 파라미터를 순서대로 입력해야 하지만
    // 직접 실행하는 방법은 @Param 으로 명시적으로 파라미터를 바인딩 해야한다.

}
// 스프링 데이터 JPA 가 제공하는 JpaRepository 인터페이스를 상속 받으면 기본적인 CRUD 기능 사용 가능
// 이름이나 가격으로 검색하는 기능은 공통 제공이 아니므로 쿼리 메소드 기능을 사용하거나 @Query 를 사용한다.

// 데이터를 조건에 따라 4가지로 분류해서 검색
// 1. 모든 데이터 조회 : findAll()
// JpaRepository 공통 인터페이스가 제공한다. 상속하였으므로 사용가능하다.
// 다음과 같은 JPQL 이 실행된다 : select i from Item i
// 2. 이름 조회 : findByItemNameLike()
// 3. 가격 조회 : findByPriceLessThanEqual()
// 4. 이름 + 가격 조회 : findByItemNameLikeAndPriceLessThanEqual()

// 참고
// Spring Data JPA 는 동적 쿼리에 약하다.
// Example 이라는 기능으로 약간의 동적 쿼리를 지원하지만, 실무사용에 기능이 빈약하다.
