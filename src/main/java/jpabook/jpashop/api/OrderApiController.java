package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderItem;
import jpabook.jpashop.domain.order.OrderStatus;
import jpabook.jpashop.domain.type.Address;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderRepository orderRepository;

	@GetMapping("/api/v1/orders")
	public List<Order> ordersV1() {
		List<Order> all = orderRepository.findAllByString(new OrderSearch());

		// 강제 초기화
		for (Order order : all) {
			order.getMember().getName();
			order.getDelivery().getAddress();
			List<OrderItem> orderItems = order.getOrderItems();
			orderItems.stream().forEach(o -> o.getItem().getName());
		}

		return all;
	}

	@GetMapping("/api/v2/orders")
	public List<OrderDto> ordersV2() {
		// Order(1) -> Member(N) -> Delivery(N) -> OrderItem(1) -> Item(N)
		return orderRepository.findAllByString(new OrderSearch()).stream()
			.map(OrderDto::new)
			.toList();
	}

	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3() {
		/*
			Order, Member, Delivery, OrderItem, Item 전부 한 번에 조회하지만,
			결국 DB상에서 결과 Row는 Order가 아닌 OrderItem 기준으로 뻥튀기 되어서 애플리케이션에 전달된다. (DB는 결과 Row를 앱으로 다 전송한다.)
				👉 Order 데이터가 중복되어 Row에 나타난다.
			⭐️정리하자면, 쿼리는 한 번이지만 데이터 전송량 자체가 많아진다.(용량이 많은 이슈 발생)

			반면에 Batch Size를 이용한 IN 쿼리의 경우 딱 최적화 된 Row 만 검색하기 때문에 용량 이슈 X
			- "Select Order with Member, Delivery" 페치 조인 결과로 Row가 2건만 조회된다.(Order 자체가 2건이므로)
		 */
		List<Order> orders = orderRepository.findAllWithItem();

		// order 가 뻥튀기 되어서 원래 order는 2개만 있는데, 4개로 조회된다. (order당 orderitem이 2개이므로, join문에 의해 row가 4개가 됨)
		// order_id | order_item_id | ...
		List<OrderDto> result = orders.stream()
			.map(OrderDto::new)
			.collect(Collectors.toList());

		return result;
	}

	/*
		➡️ (FETCH JOIN) Select Order with member, delvery
		select
    	    order0_.order_id as order_id1_6_0_,
    	    member1_.member_id as member_i1_4_1_,
    	    delivery2_.delivery_id as delivery1_2_2_,
    	    order0_.delivery_id as delivery4_6_0_,
    	    order0_.member_id as member_i5_6_0_,
    	    order0_.order_date as order_da2_6_0_,
    	    order0_.status as status3_6_0_,
    	    member1_.city as city2_4_1_,
    	    member1_.street as street3_4_1_,
    	    member1_.zipcode as zipcode4_4_1_,
    	    member1_.name as name5_4_1_,
    	    delivery2_.city as city2_2_2_,
    	    delivery2_.street as street3_2_2_,
    	    delivery2_.zipcode as zipcode4_2_2_,
    	    delivery2_.status as status5_2_2_
	    from
	        orders order0_
	    inner join
	        member member1_
	            on order0_.member_id=member1_.member_id
	    inner join
	        delivery delivery2_
	            on order0_.delivery_id=delivery2_.delivery_id limit ?

		👉 Batch Size = 100 설정
		- (이전) Lazy Loading 으로 인한 N + 1 문제 발생: Order(1) -> OrderItem(N) -> Item(N)
		- (이후) IN 쿼리를 사용하여 1 + 1 + 1 : Order(1) -> OrderItem In Query(1) -> Item In Query(1)
			👉 PK로 IN 쿼리를 타기 때문에 최적화가 잘된다. (인덱스)

		➡️ OrderItem IN QUERY
	    select
	        orderitems0_.order_id as order_id5_5_1_,
	        orderitems0_.order_item_id as order_it1_5_1_,
	        orderitems0_.order_item_id as order_it1_5_0_,
	        orderitems0_.count as count2_5_0_,
	        orderitems0_.item_id as item_id4_5_0_,
	        orderitems0_.order_id as order_id5_5_0_,
	        orderitems0_.order_price as order_pr3_5_0_
	    from
	        order_item orderitems0_
	    where
	        orderitems0_.order_id in (
	            4, 11
	        )

	    ➡️ Item IN QUERY
		select
			item0_.item_id as item_id2_3_0_,
			item0_.name as name3_3_0_,
			item0_.price as price4_3_0_,
			item0_.stock_quantity as stock_qu5_3_0_,
			item0_.artist as artist6_3_0_,
			item0_.etc as etc7_3_0_,
			item0_.author as author8_3_0_,
			item0_.isbn as isbn9_3_0_,
			item0_.actor as actor10_3_0_,
			item0_.director as directo11_3_0_,
			item0_.dtype as dtype1_3_0_
		from
			item item0_
		where
			item0_.item_id in (
				2, 3, 9, 10
			)
	 */
	@GetMapping("/api/v3.1/orders")
	public List<OrderDto> ordersV3_page(
		@RequestParam(value = "offset", defaultValue = "0") int offset,
		@RequestParam(value = "limit", defaultValue = "100") int limit
	) {
		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // Order & Member & Delivery 페치조인

		List<OrderDto> result = orders.stream()
			.map(OrderDto::new)
			.toList();

		return result;
	}

	@Data
	private static class OrderDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		private List<OrderItemDto> orderItems;

		public OrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName();
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress();
			orderItems = order.getOrderItems().stream()
				.map(OrderItemDto::new)
				.toList();
		}
	}

	@Getter
	static class OrderItemDto {

		private final String itemName;
		private final int orderPrice;
		private final int count;

		public OrderItemDto(OrderItem orderItem) {
			itemName = orderItem.getItem().getName();
			orderPrice = orderItem.getOrderPrice();
			count = orderItem.getCount();
		}
	}
}
