#### JdbcTemplate 소개와 설정
SQL 을 직접 사용하는 경우, 스프링이 제공하는 JdbcTemplate 는
JDBC 를 매우 편리하게 사용할 수 있게 도와준다.

**장점**
* 설정의 편리함
  * JdbcTemplate 은 스프링으로 JDBC 를 사용할 때 기본으로 사용되는 spring-mvc 라이브러리에 포함되어 있다.
  즉, 별도의 설정이 요구되지 않는다.
* 반복 문제 해결
  * JdbcTemplate 은 템플릿 콜백 패턴을 사용해서, JDBC 를 직접 사용할 때 발생하는 대부분의 반복 작업을 대신 처리해준다.
    * Connection 획득
    * Statement 준비, 실행
    * Connection, Statement, ResultSet 종료
    * Transaction 위한 Connection 동기화
    * Exception 발생시 Spring Exception Translator 실행

**단점**
* 동적 SQL 해결의 어려움