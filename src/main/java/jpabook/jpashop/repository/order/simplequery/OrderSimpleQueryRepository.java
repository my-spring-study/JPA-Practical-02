package jpabook.jpashop.repository.order.simplequery;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

	private final EntityManager em;

	/*
		OrderRepository 같은 Repository는 엔티티를 조회하는데만 사용하고,
		Query용 Repository는 별도의 클래스로 분리함으로서 유지보수성 높인다.
	 */

	/*
		쿼리 방식 선택 권장 순서
		1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
		2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
		3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
		4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
	 */

	// 필요한 컬럼만 조회하기 때문에 성능은 조금 더 낫다.(애플리케이션 네트웍 용량 최적화 But 생각보다 미비)
	// 리포지토리 재사용성 떨어짐. 재사용성이 떨어진다. (로직을 재활용할 수 없다.)
	// ⭐ API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점️. 논리적으로 레이어드 아키텍처가 무너져있음. View가 바뀌면 여기를 수정해야한다.
	// 엔티티를 퍼올리는게 아니기 때문에 값을 수정하거나 할 수는 없음
	public List<OrderSimpleQueryDto> findOrderDtos() {
		return em.createQuery("""
				select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o
				join o.member m
				join o.delivery d""", OrderSimpleQueryDto.class)
			.getResultList();
	}
}
