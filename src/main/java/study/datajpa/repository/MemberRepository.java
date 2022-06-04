package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

/**사용자 정의 메서드를 사용하기 위해서는
 * 1.사용자 정의 인터페이스
 * 2.사용자 정의 인터페이스 구현 클래스
 * 3.사용자 정의 인터페이스를 상속
 *
 * <규칙>
 *     사용자 정의 구현 클래스 이름 = 리포지토리 인터페이스 이름 + impl => 스프링 데이터 jpa가 인식해서 스프링 빈으로 등록
 *     리포지토리 인터페이스 이름 = 사용자 정의 인터페이스 이름 + impl도 가능
 */
public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age); //By면 동등 Greater이면 큰 조건

    List<Member> findTop3HelloBy();

    //--------------------------------------------------------------------------------------------------------

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username,@Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    //------------------------------------------------------------------------------------------------------------
    // dto조회
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //---------------------------------------------------------------------------------------------------------------

    //이름 기반 파라미터 바인딩
    @Query("select m from Member m where m.username = :name")
    Member findMembers(@Param("name") String username);

    //컬렉션 파라미터 바인딩
    @Query("select m from Member m where m.username in :names ")
    List<Member> findByNames(@Param("names") Collection<String> names);

    //-----------------------------------------------------------------------------------------------------------------
    //반환타입이 유연
    List<Member> findListByUsername(String username); //컬렉션(결과 없음 - 빈 컬렉션 반환)
    Member findMemberByUsername(String username);//단건(결과 없음 - null반환)&(결과가 2건 이상 - 예외발생:스프링 데이터는 이를 무시하고 null값을 반환)
    Optional<Member> findOptionalMemberByUsername(String username); //단건 Optional

    //-------------------------------------------------------------------------------------------------------------------

    //count쿼리 사용
    Page<Member> findByAge(int age, Pageable pageable);

    //count쿼리 사용 안 함 (다음 페이지 여부 확인 하므로 limit+1)
    Slice<Member> findByUsername(String name, Pageable pageable);

    @Query(value = "select m from Member m",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberAllCountBy(Pageable pageable);


    //-----------------------------------------------------------------------------------------------------------------------
    /**벌크 연산은 영속성 컨텍스트를 무시하고 실행하기 때문에, 영속성 컨텍스트에 있는 엔티티의 상태와
     DB에 엔티티 상태가 달라질 수 있다.
     > 권장하는 방안
     > 1. 영속성 컨텍스트에 엔티티가 없는 상태에서 벌크 연산을 먼저 실행한다.
     > 2.부득이하게 영속성 컨텍스트에 엔티티가 있으면 벌크 연산 직후 영속성 컨텍스트를 초기화 한다.**/

    //벌크성 쿼리
    //@Modifying : 벌크성 수정,삭제 쿼리에서 사용
    @Modifying(clearAutomatically = true) //=>true면 영속성 컨텍스트 초기화(이 조건이 없다면 findById로 다시 조회하면 영속성 컨텍스트에 과거 값이 남아서 문제가 될수 있다
   // executeUpdate(); 이걸 해주는 역할 & clear과정 자동화
    @Query("update Member m set m.age=m.age+1 where m.age >= :age")
    int bulkAgePlus (@Param("age") int age);

    //--------------------------------------------------------------------------------------------------------------------------

    @Query("select m from Member m left join fetch m.team")  //한번에 다 끌고 온다(jpql없이 페치 조인을 할 수 있다)
    List<Member> findMemberFetchJoin();

    //공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"}) //team까지 한번에
    List<Member> findAll();

    //jpql+엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    //메서드 이름으로 쿼리에서 편리
    @EntityGraph(attributePaths = ("Team"))
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value=@QueryHint( name="org.hibernate.readOnly",value="true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);

}
