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