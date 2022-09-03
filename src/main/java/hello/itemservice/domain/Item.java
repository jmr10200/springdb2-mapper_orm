package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * Item 객체
 */
@Data
@Entity // JPA 사용 객체
public class Item {

    @Id // 테이블의 PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 를 DB 에서 생성 (ex MySQL 의 auto increment)
    private Long id;

    @Column(name = "item_name", length = 10) // 테이블 컬럼명
    // 생략시 필드명 사용, 카멜 -> 스네이크 자동 변환 지원 OK (생략 OK, itemName -> item_name)
    private String itemName;
    private Integer price;
    private Integer quantity;

    // JPA 는 public 또는 protected 기본 생성자가 필수
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
