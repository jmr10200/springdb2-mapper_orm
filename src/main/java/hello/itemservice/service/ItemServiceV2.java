package hello.itemservice.service;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.v2.ItemQueryRepositoryV2;
import hello.itemservice.repository.v2.ItemRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceV2 implements ItemService {

    // ItemRepositoryV2 는 스프링 데이터 JPA 의 기능을 제공하는 리포지토리
    private final ItemRepositoryV2 itemRepositoryV2;

    // ItemQueryRepositoryV2 는 Querydsl 사용해서 복잡한 쿼리 기능을 제공하는 리포지토리
    private final ItemQueryRepositoryV2 itemQueryRepositoryV2;

    // 이렇게 둘을 분리하여 기본 CRUD 와 단순 조회는 스프링 데이터 JPA , 복잡한 조회쿼리는 Querydsl

    @Override
    public Item save(Item item) {
        return itemRepositoryV2.save(item);
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
        return itemRepositoryV2.findById(id);
    }

    @Override
    public List<Item> findItems(ItemSearchCondition condition) {
        return itemQueryRepositoryV2.findAll(condition);
    }
}
