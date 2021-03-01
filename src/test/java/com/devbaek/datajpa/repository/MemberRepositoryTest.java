package com.devbaek.datajpa.repository;

import com.devbaek.datajpa.dto.MemberDto;
import com.devbaek.datajpa.entity.Member;
import com.devbaek.datajpa.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @Test
    void basicTest() throws Exception{

        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);

    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isZero();
    }

    @Test
    void queryMethodTest() {
        Member member1 = new Member("member1", 20);
        Member member2 = new Member("member2", 30);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("member1", 19);

        assertThat(members.get(0).getAge()).isEqualTo(20);
    }

    @Test
    void namedQueryTest() {
        Member member1 = new Member("member1", 20);
        Member member2 = new Member("member2", 30);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsername("member1");

        assertThat(members.get(0).getAge()).isEqualTo(20);
    }

    @Test
    void testQuery() {
        Member member1 = new Member("member1", 20);
        Member member2 = new Member("member2", 30);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findUser("member1", 20);

        assertThat(members.get(0).getAge()).isEqualTo(20);
    }

    @Test
    void testQueryFindByDto() {
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member member1 = new Member("member1", 20, teamA);
        Member member2 = new Member("member2", 20, teamA);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        assertThat(memberDto.get(0).getUsername()).isEqualTo("member1");
        assertThat(memberDto.size()).isEqualTo(2);

        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    void collectionParameterBindingTest() {
        Member member1 = new Member("member1", 20);
        Member member2 = new Member("member2", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> names = new ArrayList<>();
        names.add("member1");
        names.add("member2");

        List<Member> byNames = memberRepository.findByNames(names);

        assertThat(byNames.size()).isEqualTo(2);
        assertThat(byNames.get(0).getUsername()).isEqualTo("member1");
    }

    @Test
    void pagingTest() {

        int age = 10;

        memberRepository.save(new Member("member1", age));
        memberRepository.save(new Member("member2", age));
        memberRepository.save(new Member("member3", age));
        memberRepository.save(new Member("member4", age));
        memberRepository.save(new Member("member5", age));

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isZero();
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void pagingSliceTest() {

        int age = 10;

        memberRepository.save(new Member("member1", age));
        memberRepository.save(new Member("member2", age));
        memberRepository.save(new Member("member3", age));
        memberRepository.save(new Member("member4", age));
        memberRepository.save(new Member("member5", age));

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);
//        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isZero();
//        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 16));
        memberRepository.save(new Member("member3", 17));
        memberRepository.save(new Member("member4", 18));
        memberRepository.save(new Member("member5", 19));

        int resultCount = memberRepository.bulkAgePlus(16);
        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5.getAge());

        // Modifying option에 clear를 넣으면 clear 해 줄 필요 없
//        em.clear();

        List<Member> resultAfterClear = memberRepository.findByUsername("member5");
        Member member5AfterClear = resultAfterClear.get(0);
        System.out.println("member5AfterClear = " + member5AfterClear.getAge());

        assertThat(resultCount).isEqualTo(4);
    }
}