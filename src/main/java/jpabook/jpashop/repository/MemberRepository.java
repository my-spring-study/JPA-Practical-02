package jpabook.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import jpabook.jpashop.domain.member.Member;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

	private final EntityManager em;

	/**
	 * 영속성 컨텍스트에 의해 관리될 때 id 생성(key, value)
	 * em.persist()만 해도 id 생성
	 */
	public Long save(Member member) { // command 와 query를 분리하라 원칙 적용
		em.persist(member);
		return member.getId();
	}

	public Member findOne(Long id) {
		return em.find(Member.class, id);
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
			.getResultList();
	}

	public List<Member> findByName(String name) {
		return em.createQuery("""
				select m
				from Member m
				where m.name = :name""", Member.class)
			.setParameter("name", name)
			.getResultList();
	}
}
