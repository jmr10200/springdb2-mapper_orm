package hello.itemservice.repository;

import hello.itemservice.domain.Item;

import java.util.List;
import java.util.Optional;

/**
 * interface
 * 인터페이스 이므로, 향후 다양한 데이터 접근 기술 구현체로 변경 가능
 */
public interface ItemRepository {

    Item save(Item item);

    void update(Long itemId, ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCondition cond);

}
