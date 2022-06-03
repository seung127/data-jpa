package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.Entity;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age); //By면 동등 Greater이면 큰 조건

    List<Member> findTop3HelloBy();

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username,@Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    //dto조회
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names ")
    List<Member> findByNames(@Param("names") Collection<String> names);


    //반환타입이 유연
    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username);//단건
    Optional<Member> findOptionalMemberByUsername(String username); //단건 Optional


    Page<Member> findByAge(int age, Pageable pageable);

    @Query(value = "select m from Member m",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberAllCountBy(Pageable pageable);

    //벌크성 쿼리
    @Modifying(clearAutomatically = true) //  .executeUpdate(); 이걸 해주는 역할 &clear과정 자동화
    @Query("update Member m set m.age=m.age+1 where m.age >= :age")
    int bulkAgePlus (@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")  //한번에 다 끌고 온다
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"}) //team까지 한번에
    List<Member> findAll();

    @EntityGraph(attributePaths = ("Team"))
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value=@QueryHint( name="org.hibernate.readOnly",value="true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);

}
