package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderStatus;
import jpabook.jpashop.domain.type.Address;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * XToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApi {

	private final OrderRepository orderRepository;
	private final OrderSimpleQueryRepository orderSimpleQueryRepository;

	// Order -> Member -> Order -> Member ... 무한루프
	// 양방향 중 한 곳은 @JsonIgnore
	@GetMapping("/api/v1/simple-orders")
	public List<Order> ordersV1() {
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		for (Order order : all) {
			order.getMember().getName(); // Lazy 강제 초기화
			order.getDelivery().getAddress(); // Lazy 강제 초기화
		}
		return all;
	}

	@GetMapping("/api/v2/simple-orders")
	public List<SimpleOrderDto> ordersV2() {
		// ORDER -> findAllByString에서 JPQL, 즉 SQL 1번 -> 결과 주문수 2 👉 쿼리 1번
		// N+1(1+N)문제 -> 1 + 회원 N + 배송 N. 여기서 쿼리 '1'에서 가져오는 결과가 2개 이므로 N은 2
		List<Order> findOrders = orderRepository.findAllByString(new OrderSearch());

		// 주문 1의 Member, Delivery 조회 👉 쿼리 2번
		// 주문 2의 Member, Delivery 조회 👉 쿼리 2번
		// 따라서 총 쿼리 5번
		List<SimpleOrderDto> dtos = findOrders
			.stream()
			.map(SimpleOrderDto::new)
			.toList();

		return dtos;
	}

	@GetMapping("/api/v3/simple-orders")
	public List<SimpleOrderDto> ordersV3() {
		List<Order> findOrders = orderRepository.findAllWithMemberDelivery();

		List<SimpleOrderDto> result = findOrders
			.stream()
			.map(SimpleOrderDto::new)
			.toList();

		return result;
	}

	@GetMapping("/api/v4/simple-orders")
	public List<OrderSimpleQueryDto> ordersV4() {
		// 필요한 컬럼만 퍼올린다.
		return orderSimpleQueryRepository.findOrderDtos();
	}

	@Data
	static class SimpleOrderDto {
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;

		public SimpleOrderDto(Order order) { // 별로 중요하지 않은 DTO 에서 중요한 엔티티 참조하는 것 괜찮다.
			orderId = order.getId();
			name = order.getMember().getName(); // LAZY 초기화
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress(); // LAZY 초기화
		}
	}
}
