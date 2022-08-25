package hello.itemservice.repository;

import lombok.Data;

/**
 * Item 검색 조건 Model
 */
@Data
public class ItemSearchCondition {

    private String itemName;
    private Integer maxPrice;

    public ItemSearchCondition() {
    }

    public ItemSearchCondition(String itemName, Integer maxPrice) {
        this.itemName = itemName;
        this.maxPrice = maxPrice;
    }
}
