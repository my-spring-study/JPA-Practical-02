package jpabook.jpashop.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.member.Member;

@SpringBootTest
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@Transactional
	@Rollback(value = false)
	void testMember() {
	    // given
		Member member = new Member();
		member.setName("memberA");

		// when
		Long savedId = memberRepository.save(member);
		Member findMember = memberRepository.findOne(savedId);

	 	// then
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getName()).isEqualTo(member.getName());
		assertThat(findMember).isEqualTo(member); // equals & hashcode를 재정의하지 않았기 때문에 == 비교와 같음
	}
}