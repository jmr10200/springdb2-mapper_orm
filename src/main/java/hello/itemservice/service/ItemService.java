package hello.itemservice.service;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;

import java.util.List;
import java.util.Optional;

/**
 * ItemService interface
 * 인터페이스 이므로 구현체를 쉽게 변경할 수 있다.
 * 서비스는 구현체를 변경할 일이 드물이 굳이 인터페이스를 만들지는 않지만, 예제상 생성
 */
public interface ItemService {

    Item save(Item item);

    void update(Long itemId, ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findItems(ItemSearchCondition itemSearch);
}
