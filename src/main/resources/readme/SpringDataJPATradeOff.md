#### 스프링 데이터 JPA - 트레이드 오프

##### 예제
```
ItemService 는 ItemRepository 인터페이스에 의존한다.
JpaItemRepositoryV2가 구현체이다.
또, SpringDataJpaItemRepository 에 의존한다.
```
중간에서 JpaItemRepositoryV2 가 어댑터 역할을 해준 덕분에 MemberService 가 사용하는 MemberRepository 인터페이스를
그대로 유지할 수 있고 클라이언트인 MemberService 의 코드를 변경하지 않아도 되는 장점이 있다.

***고민***<br>
구조를 맞추기 위해서 중간에 어댑터가 들어가게 되면서 전체구조가 너무 복잡해지고 사용하는 클래스도 많아지게 됐다.<br>
유지보수 관점에서 ItemService 를 변경하지 않고, ItemRepository 의 구현체를 변경할 수 있는 장점이 있다.<br>
DI, OCP 원칙을 지킬 수 있다는 장점이 있다. 하지만 반대로 구조가 복잡해 어댑터코드와 실제코드까지 유지보수해야 된다.

***다른 선택***<br>
ItemService 코드를 일부 고쳐서 직접 스프링 데이터 JPA 를 사용하도록 하는 방법도 있다.<br>
DI, OCP 원칙을 포기하는 대신에, 어댑터를 제거하여 구조를 단순하게 하는 것이다.

***트레이드 오프***<br>
이것이 바로 트레이드 오프이다.<br>
DI, OCP 를 지키기 위해 어댑터를 도입하고, 더 많은 코드를 유지한다.<br>
어댑터를 제거하고 구조를 단순하게 가져가지만, DI, OCP 를 포기하고 ItemRepository 를 직접 변경한다.<br>
<br>
즉, 여기서 발생하는 트레이드 오프는 구조의 안정성 vs 단순한 구조와 개발의 편리성 사이의 선택이다.<br>
정답이 있는 것은 아니다. 상황에 따라 구조의 안정성이, 단순한 구조가 더 우선될 수 있다.<br>
<br>
유지보수 관점에서 추상화도 비용이 든다. 어설픈 추상화는 오히려 독이 된다.<br>
이 추상화 비용을 넘어설 만큼 효과가 있을 때 추상화를 도입하는 것이 실용적이다.<br>


<hr>

#### 다양한 데이터 접근 기술 조합
데이터 접근 기술의 선택에 있어서 하나의 정답이 있는 것은 아니다. 프로젝트의 비즈니스 상황, 구성원의 역량 등에 따라 결정하는게 맞을 것이다.<br>
JdbcTemplate 이나 MyBatis 같은 기술들은 SQL 을 직접 작성해야 하지만, 단순하기 때문에 SQL 에 익숙한 개발자라면 금방 적응할 수 있다.<br>
JPA, Spring Data JPA, Querydsl 같은 기술을은 개발 생산성을 높일수 있지만, 학습곡선이 높다. 또한 매우 복잡한 통계 쿼리등의 환경에서는 잘 맞지 않다.<br>
<br>
JPA, Spring Data JPA, Querydsl 을 기본으로 사용하고, 복잡한 쿼리가 요구될 때는 JdbcTemplate 이나 MyBatis 를 함께 사용하는 것이 좋은 방법이 될수도 있다.<br>
복잡한 쿼리가 많은 프로젝트라면 JdbcTemplate 나 MyBatis 비중이 높아질 수 있다.

###### 트랜젝션 매니저 선택 - JpaTransactionManager
JPA, Spring Data JPA, Querydsl 은 모두 JPA 기술을 사용하는 것이기 때문에 트랜젝션 매니저로 JpaTransactionManager 를 선택하면 된다.<br>
해당 기술을 사용하면 스프링 부트는 자동으로 JpaTransactionManager 를 스프링 빈에 등록한다.<br>
그런데, JdbcTemplate, MyBatis 같은 기술들은 내부에서 JDBC 를 직접 사용하기 때문에 DataSourceTransactionManager 를 사용한다.<br>
이 경우, 두 기술을 결합하여 사용할 때 트랜젝션 매니저가 달라지는데, 이는 JpaTransactionManager 를 사용하면 된다.<br>
JpaTransactionManager 는 DataSourceTransactionManager 가 제공하는 기능도 대부분 제공한다.<br>
JPA 라는 기술도 내부에서는 DataSource 와 JDBC Connection 을 사용하기 때문이다.<br>
<br>

***주의점***<br>
이렇게 JPA 와 JdbcTemplate 를 함께 사용하는 경우 JPA 의 flush 타이밍에 주의해야 한다. JPA 는 데이터를 변경하면 변경 사항을 즉시 DB에 반영하지 않는다. 기본적으로 트랜젝션이 커밋되는 시점에 DB에 반영한다.<br>
그래서 하나의 트랜젝션 안에서 JPA 를 통해 데이터를 변경한 다음에 JdbcTemplate 를 호출하는 경우 JdbcTemplate 에서는 JPA 가 변경한 데이터를 읽지 못하는 문제가 발생한다.<br>
이 문제를 해결하기위해 JPA 호출이 끝난 시점에 JPA 가 제공하는 flush 라는 기능을 사용해서 JPA 의 변경 내역을 DB에 반영해주어야 한다.<br>
그래야 그 다음에 호출되는 JdbcTemplate 에서 JPA 가 반영한 데이터를 사용할 수 있다.

<br>
<hr>

##### 정리
ItemServiceV2 는 스프링 데이터 JPA 를 제공하는 ItemRepositoryV2 도 참조하고, Querydsl 과 관련된 ItemQueryRepositoryV2 도 직접 참조한다.<br>
덕분에 ITemRepositoryV2 를 통해서 스프링 데이터 JPA 기능을 적절히 활용할 수 있고, ItemQueryRepositoryV2 를 통해서 복잡한 쿼리를 Querydsl 로 해결할 수 있다.<br>
이렇게 해서 구조의 복잡함 없이 개발할 수 있다.<br>
그러나, 프로젝트의 상황에 따라 단순하면서 라이브러리의 지원을 편하게 받는 구조가 더 나은 경우도 있다.<br>
다만, 이 경우는 유지보수에 어려움이 있다는 단점을 명심하자. 이런 트레이드 오프를 알고, 상황에 맞는 적절한 선택이 필요하다. 