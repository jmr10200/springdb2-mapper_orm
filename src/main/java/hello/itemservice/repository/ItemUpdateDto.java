package hello.itemservice.repository;

import lombok.Data;

/**
 * 상품 수정 객체
 * DTO : 단순히 데이터를 전달하는 용도의 객체
 */
@Data
public class ItemUpdateDto {
    private String itemName;
    private Integer price;
    private Integer quantity;

    public ItemUpdateDto() {
    }

    public ItemUpdateDto(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
/* 참고 DTO */
// DTO : Data Transfer Object
// 데이터 전송 객체
// DTO 는 기능은 없고 데이터를 전달만 하는 용도로 사용되는 객체
// 참고로 기능을 추가해도 된다. 객체의 주 목적이 데이터를 전송하는 것이라면 DTO 라 할 수 있다.
// 객체이름에 DTO 를 명시하는 것은, 알아보기 쉽게 하기 위함이다.
// ItemSearchCondition 도 DTO 이다. 하지만 Dto 를 명시하면 객체 이름이 너무 길어진다.