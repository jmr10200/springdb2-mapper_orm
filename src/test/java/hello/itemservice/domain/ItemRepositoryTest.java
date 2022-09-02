package hello.itemservice.domain;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.memory.MemoryItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest // @SpringBootApplication 를 찾아서 설정으로 사용함
class ItemRepositoryTest {

    // 해당 구현체가 아니라 interface 를 주입했다.
    // 즉, 구현체가 바뀌어도 테스트 코드가 변경되지 않을 것이다.
    @Autowired
    ItemRepository itemRepository;

    // 트랜젝션
/*
    @Autowired
    PlatformTransactionManager transactionManager;
    TransactionStatus status;

    @BeforeEach
    void beforeEach() {
        // 트랜젝션 시작
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // 스프링부트가 자동으로 적절한 트랜젝션 매니저를 스프링 빈으로 등록해준다.
    }
*/

    // 테스트는 서로 영향을 주면 안된다. 따라서 각각의 테스트가 끝나면 데이터를 제거해준다.
    @AfterEach
    void afterEach() {
        // MemoryItemRepository 의 경우 제한적으로 사용
        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }

        // 트랜젝션 rollback
//        transactionManager.rollback(status);
    }

    @Test
    void save() {
        // given
        Item item = new Item("itemA", 10000, 10);

        // when
        Item savedItem = itemRepository.save(item);

        // then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void updateItem() {
        // given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        // when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        // then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findItems() {
        // given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        // 둘 다 없음 검증
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        // itemName 검증
        test("itemA", null, item1, item2);
        test("itemA", null, item1, item2);
        test("itemB", null, item3);

        // maxPrice 검증
        test(null, 10000, item1);

        // 둘 다 있음 검증
        test("itemA", 10000, item1);
    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCondition(itemName, maxPrice));
        assertThat(result).containsExactly(items);
    }
}
// 테스트의 데이터베이스 분리
// 로컬에서 사용하는 어플리케이션 서버와 테스트에서 같은 DB 를 사용하면 테스트에서 문제가 발생한다.
// 이러한 문제 때문에 테스트를 분리할 필요가 있다.
// 가장 간단한 방법은 테스트 전용 DB 를 별도로 운영하는 것이다.
// jdbc:h2:tcp://localhost/~/test local 에서 접근하는 서버 전용 DB
// jdbc:h2:tcp://localhost/~/testcase test 케이스에서 접근하는 전용 DB
// 또한, 반복해서 실행할 수 있도록 트랜젝션 rollback 을 이용하자

// 테스트 코드에서의 @Transactional
// 테스트에 있으면 스프링은 테스트를 트랜젝션 안에서 실행하고, 테스트가 끝나면 자동으로 롤백 시킨다.
// 테스트가 끝나고 직접 데이터를 삭제하지 않아도 되는 편리함을 제공한다.
// 커밋하지 않기때문에 테스트 중 강제 종료되어도 데이터는 자동으로 롤백된다.
// 트랜젝션 범위 안에서 테스트를 진행하기 때문에 동시에 다른 테스트가 진행되어도 서로 영향을 주지 않는다.

// @Transactional 덕분에 다음 원칙을 지킬 수 있게 되었다.
// ・테스트는 다른 테스트와 격리해야 한다.
// ・테스트는 반복해서 실행할 수 있어야 한다.

// 강제로 커밋하고 싶은 경우 : @Commit
// @Transactional 설정하에서 데이터가 잘 저장되는지 확인하기 위해서는 @Commit 을 사용하면 된다.
// @Rollback(value = false) 도 동일하게 동작한다.