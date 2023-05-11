package jpabook.jpashop.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jpabook.jpashop.domain.member.Member;
import jpabook.jpashop.domain.order.Order;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

	private final EntityManager em;

	public void save(Order order) {
		em.persist(order);
	}

	public Order findOne(Long id) {
		return em.find(Order.class, id);
	}

	public List<Order> findAll(OrderSearch orderSearch) {
		return em.createQuery("""
				select o
				from Order o
				join o.member m
				where o.status = :status and m.name like :name""", Order.class)
			.setParameter("status", orderSearch.getOrderStatus())
			.setParameter("name", orderSearch.getMemberName())
			// .setFirstResult(100)
			.setMaxResults(1000) // 최대 1000건
			.getResultList();
	}

	public List<Order> findAllByString(OrderSearch orderSearch) {

		//language=JPQL
		String jpql = "select o From Order o join o.member m";
		boolean isFirstCondition = true;

		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " o.status = :status";
		}

		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " m.name like :name";
		}

		TypedQuery<Order> query = em.createQuery(jpql, Order.class)
			.setMaxResults(1000); //최대 1000건

		if (orderSearch.getOrderStatus() != null) {
			query = query.setParameter("status", orderSearch.getOrderStatus());
		}

		if (StringUtils.hasText(orderSearch.getMemberName())) {
			query = query.setParameter("name", orderSearch.getMemberName());
		}

		return query.getResultList();
	}

	// "QueryDsl로 동적쿼리를 해결하는 것이 좋다.(컴파일 타임에 오류를 잡는 등 이점이 많다.)"
	public List<Order> findAllByCriteria(OrderSearch orderSearch) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> o = cq.from(Order.class);
		Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
		List<Predicate> criteria = new ArrayList<>();
		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			Predicate status = cb.equal(o.get("status"),
				orderSearch.getOrderStatus());
			criteria.add(status);
		}
		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			Predicate name =
				cb.like(m.<String>get("name"), "%" +
					orderSearch.getMemberName() + "%");
			criteria.add(name);

		}
		cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
		TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
		return query.getResultList();
	}

	// 프록시에 값을 다 채워서 가져온다.
	public List<Order> findAllWithMemberDelivery() {
		return em.createQuery("""
				select o from Order o
				join fetch o.member m
				join fetch o.delivery d""", Order.class)
			.getResultList();
	}

	public List<Order> findAllWithMemberDelivery(int offset, int limit) {
		return em.createQuery("""
				select o from Order o
				join fetch o.member m
				join fetch o.delivery d""", Order.class)
			.setFirstResult(offset)
			.setMaxResults(limit)
			.getResultList();
	}

	public List<Order> findAllWithItem() {
		/*
			JPQL에서 DISTINCT 키워드 사용하기

			distinct를 사용할 때 실제 DB에서는 완전히 같은 row여야만 적용되지만,
			JPA경우 같은 id에 대해 엔티티 하나만 유지
		 */

		/*
			컬렉션 페치조인에서는 페이징이 불가능하다. (현재 Order 기준 OrderItem에 대하여 1:N 페치조인)
			Order를 기준으로 페이징 하고 싶은데, 다(N)인 OrderItem을 조인하여 OrderItem이 기준이 되기 때문(데이터 뻥튀기)

			컬렉션 페치조인으로 페이징할 경우 아래와 같은 경고 문구를 띄운다.
			👉 WARN 42818 --- [nio-8080-exec-1] o.h.h.internal.ast.QueryTranslatorImpl   : HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!
			👉 이 경우 하이버네이트는 경고 로그를 남기고 모든 DB 데이터를 읽어서 메모리에서 페이징을 시도한다. 최악의 경우 장애로 이어질 수 있다.
		 */

		/*
			(참고) 컬렉션 페치 조인은 1개만 할 수 있다.
			👉 컬렉션 둘 이상에 대해 페치조인 하면 안 된다. 데이터가 부정확하게 조회될 수 있다.
		 */
		return em.createQuery("""
				select distinct o from Order o
				join fetch o.member m
				join fetch o.delivery d
				join fetch o.orderItems oi
				join fetch oi.item i""", Order.class)
			.setFirstResult(1)
			.setMaxResults(100)
			.getResultList();
	}
}
