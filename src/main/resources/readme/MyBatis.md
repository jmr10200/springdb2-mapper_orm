### MyBatis 기능 정리1 - 동적 쿼리

#### 동적SQL
MyBatis 를 사용하는 이유는 동적 SQL 기능 때문이라 할 수 있다.
* if
* choose (when, otherwise)
* trim (where, set)
* foreach
<br>
<br>
#### IF
```
<select id="findActiveBlogWithTitleLike" resultType="Blog">
  SELECT * FROM BLOG
  WHERE state = ‘ACTIVE’
  <if test="title != null">
    AND title like #{title}
  </if>
</select>
```
* 해당 조건에 따라 값을 추가할지 판단한다.
* 내부의 문법은 OGNL 을 사용한다.
<br>
<br>
#### choose, when, otherwise
```
<select id="findActiveBlogLike" resultType="Blog">
  SELECT * FROM BLOG WHERE state = ‘ACTIVE’
  <choose>
    <when test="title != null">
      AND title like #{title}
    </when>
    <when test="author != null and author.name != null">
      AND author_name like #{author.name}
    </when>
    <otherwise>
      AND featured = 1
    </otherwise>
  </choose>
</select>
```
* java 의 switch 구문과 유사한 구문
<br>
<br>
#### trim, where, set
```
<select id="findActiveBlogLike" resultType="Blog">
  SELECT * FROM BLOG
  WHERE
  <if test="state != null">
    state = #{state}
  </if>
  <if test="title != null">
    AND title like #{title}
  </if>
  <if test="author != null and author.name != null">
    AND author_name like #{author.name}
  </if>
</select>
```
이 예제의 문제점은 모두 만족하지 않을때 발생한다.
```
SELECT * FROM BLOG
WHERE
```
WHERE 문의 문제가 발샐한다.
#### \<where> 사용
```
<select id="findActiveBlogLike" resultType="Blog">
  SELECT * FROM BLOG
  <where>
    <if test="state != null">
      state = #{state}
    </if>
    <if test="title != null">
      AND title like #{title}
    </if>
    <if test="author != null and author.name != null">
      AND author_name like #{author.name}
    </if>
  </where>
</select>
```
\<where> 는 문장이 없으면 WHERE 를 추가하지 않는다. 만약 AND 가 먼저 시작하면 AND 를 지워준다.
<br>
참고) trim 기능을 이용해도 된다.
```
<trim prefix="WHERE" prefixOverrides="AND |OR ">
 ...
</trim>
```
<br>
<br>

#### foreach
```
<select id="selectPostIn" resultType="domain.blog.Post">
  SELECT *
  FROM POST P
  <where>
    <foreach item="item" index="index" collection="list"
     open="ID in (" separator="," close=")" nullable="true">
       #{item}
    </foreach>
  </where>
</select>
```
* 컬렉션 반복 처리시 사용
* 파라미터로 List 전달

<br>
<br>

### MyBatis 기능 정리2 - 기타 기능

#### 어노테이션으로 SQL 작성
XML 대신 어노테이션으로 SQL 을 작성할 수 있다.
```
@Select("select id, item_name, price, quantity from item where id=#{id}")
Optional<Item> findById(Long id);
```
* @Insert, @Update, @Delete, @Select 기능이 제공된다.
* 이 경우 XML 에서 <select if="findById">...</select> 제거해야 한다.
* 동적 SQL 을 사용하지 않는 간단한 경우에만 사용한다.

<br>
<br>

#### 문자열 대체 (String Substitution)
#{...} 문법은 ?를 넣고 파라미터를 바인딩하는 PreparedStatement 를 사용한다.<br>
파라미터 바인딩이 아니라 문자 그대로를 처리하고 싶은 경우는 ${...} 를 사용하면 된다.
```
@Select("select * from user where ${column} = #{value}")
User findByColumn(@Param("column") String column, @Param("value") String value);
```
* 주의
* ${...} 를 사용하면 SQL 인젝션 공격을 당할 수 있다. 따라서 가급적 사용하지 말아야하며 매우 주의깊게 사용해야 한다.

<br>
<br>

#### 재사용 가능한 SQL 조각
\<sql> 를 사용하면 SQL 코드를 재사용 할 수 있다.
```
<sql id="userColumns"> ${alias}.id,${alias}.username,${alias}.password </sql>
```
```
<select id="selectUsers" resultType="map">
  select
    <include refid="userColumns"><property name="alias" value="t1"/></include>,
    <include refid="userColumns"><property name="alias" value="t2"/></include>
  from some_table t1
    cross join some_table t2
</select>
```
* \<include> 를 통해서 <sql> 조각 사용할 수 있다.
```
<sql id="sometable">
  ${prefix}Table
</sql>
<sql id="someinclude">
  from
  <include refid="${include_target}"/>
</sql>
<select id="select" resultType="map">
  select
  field1, field2, field3
  <include refid="someinclude">
    <property name="prefix" value="Some"/>
    <property name="include_target" value="sometable"/>
  </include>
</select>
```
* 프로퍼티 값을 전달할 수 있고, 해당 값은 내부에서 사용할 수 있다.

<br>
<br>

#### ResultMaps
결과를 매핑할 때 테이블은 user_id 이지만 객체는 id 인 경우<br>
컬럼명과 객체의 프로퍼티 명이 다르다. 이때는 별칭 (as) 를 사용하면 된다.
```
<select id="selectUsers" resultType="User">
  select
    user_id as "id",
    user_name as "userName",
    hashed_password as "hashedPassword"
  from some_table
  where id = #{id}
</select>
```
별칭을 사용하지 않는 경우, resultMap 을 선언하면 된다.
```
<resultMap id="userResultMap" type="User">
  <id property="id" column="user_id" />
  <result property="username" column="username"/>
  <result property="password" column="password"/>
</resultMap>

<select id="selectUsers" resultMap="userResultMap">
  select user_id, user_name, hashed_password
  from some_table
  where id = #{id}
</select>
```

<br>
<br>

#### 복잡한 결과매핑
MyBatis 도 매우 복잡한 결과에 객체 연관관계를 고려하여 데이터를 조회하는 것이 가능하다.<br>
이때는 <association>, <collection> 등을 사용한다.<br>
이 부분은 성능과 실효성 측면에서 많은 고민이 필요하다.<br>
JPA 는 객체와 관계형 DB를 ORM 개념으로 매핑하기 때문에 자연스럽지만,<br>
MyBatis 에서는 들어가는 공수도 많고, 성능을 최적화하기 어렵다.<br>
따라서 해당 기능을 사용할 때 신중하게 사용해야 한다.