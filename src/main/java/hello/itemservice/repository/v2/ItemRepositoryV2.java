package hello.itemservice.repository.v2;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
}
// JpaRepository 를 인터페이스 상속받아 스프링 데이터 JPA 기능을 제공하는 리포지토리
// 기본 CRUD 제공되므로 사용하면 된다.
// 추가로 단순한 조회 쿼리등을 설정할 수 있다.