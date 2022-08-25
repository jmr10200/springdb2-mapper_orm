package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.memory.MemoryItemRepository;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 설정
 * service, repository 구현체를 편리하게 변경하기 위해 수동 빈 등록
 * controller 는 component scan 을 사용함
 */
@Configuration
public class MemoryConfig {

    // ItemService 스프링 빈 등록, Constructor 통해 의존관계 주입
    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    // ItemRepository 스프링 빈 등록, Constructor 통해 의존관계 주입
    @Bean
    public ItemRepository itemRepository() {
        return new MemoryItemRepository();
    }

}
