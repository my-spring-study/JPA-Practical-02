package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
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
		List<Order> orders = orderRepository.findAllWithItem();

		// order 가 뻥튀기 되어서 원래 order는 2개만 있는데, 4개로 조회된다. (order당 orderitem이 2개이므로, join문에 의해 row가 4개가 됨)
		// order_id | order_item_id | ...
		List<OrderDto> result = orders.stream()
			.map(OrderDto::new)
			.collect(Collectors.toList());

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