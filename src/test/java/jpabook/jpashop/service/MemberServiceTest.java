package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.member.Member;
import jpabook.jpashop.repository.MemberRepository;

@SpringBootTest
@Transactional // 이 어노테이션이 테스트 케이스에서 사용될 때만 롤백
class MemberServiceTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private EntityManager em;

	@Test
	void 회원가입() {
		// given
		Member member = new Member();
		member.setName("kim");

		// when
		Long savedId = memberService.join(member);

		// then
	 	/*
	 		flush()를 하지 않으면 insert 쿼리가 나가지 않는다.
	 		이유: JPA 입장에서 롤백을 해버리면 굳이 insert 해야할 필요도 없기 때문
		 */
		em.flush();
		assertThat(memberRepository.findOne(savedId)).isEqualTo(member);
	}

	@Test
	void 중복_회원_예외() {
		// given
		Member member1 = new Member();
		member1.setName("kim1");

		Member member2 = new Member();
		member2.setName("kim1");

		memberService.join(member1);

		// when & then
		/**
		 * memberRepository.findByName(member2)를 할 때 쓰기 지연 SQL 저장소에 있던 insert 쿼리 실행
		 * 이유: JPQL을 실행하면 먼저 flush가 일어나기 때문
		 */
		assertThatThrownBy(() -> memberService.join(member2))
			.isInstanceOf(IllegalStateException.class);
	}
}