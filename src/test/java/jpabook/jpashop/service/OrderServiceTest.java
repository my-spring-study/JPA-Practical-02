package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.member.Member;
import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderStatus;
import jpabook.jpashop.domain.type.Address;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;

@SpringBootTest
@Transactional
class OrderServiceTest {

	@Autowired
	private EntityManager em;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@Test
	void 상품주문() {
		// given
		Member member = createMember("회원1");

		int bookPrice = 10000;
		int initialBookStockQuantity = 10;
		Book book = createBook("시골 JPA", bookPrice, initialBookStockQuantity);

		int orderCount = 2;

		// when
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
		em.flush();

		// then
		Order findOrder = orderRepository.findOne(orderId);

		assertThat(findOrder.getStatus())
			.as("상품 주문시 상태는 ORDER 이다.")
			.isEqualTo(OrderStatus.ORDER);

		assertThat(findOrder.getOrderItems().size())
			.as("주문한 상품 종류 수가 정확해야한다.")
			.isEqualTo(1);

		assertThat(findOrder.getTotalPrice())
			.as("주문 가격은 가격 * 수량이다.")
			.isEqualTo(bookPrice * orderCount);

		assertThat(book.getStockQuantity())
			.as("주문 수량만큼 재고가 줄어야 한다.")
			.isEqualTo(initialBookStockQuantity - orderCount);
	}

	/*
		"통합 테스트를 하는 것보다 item.removeStock()에 대한 단위테스트가 더 중요하다."
		"엔티티 자체의 비즈니스 로직을 테스트 하는 것이 중요하다."
		"통합 테스트는 여러 객체를 묶어서 테스트하는 것에 의의를 둔다."
	*/
	@Test
	@DisplayName("상품 주문시 재고 수량을 초과하면 예외가 발생한다.")
	void throws_exception_when_orderCount_exceeds_stockQuantity() {
		// given
		Member member = createMember("name");

		int bookStockQuantity = 10;
		Book book = createBook("book-name", 10000, bookStockQuantity);

		int orderCount = bookStockQuantity + 1;

		// when & then
		assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
			.isInstanceOf(NotEnoughStockException.class);
	}

	@Test
	@DisplayName("주문 취소")
	void cancel_order() {
		// given
		Member member = createMember("name");

		int stockQuantity = 10;
		Book item = createBook("시골 JPA", 10000, stockQuantity);

		int orderCount = 2;

		Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

		// when
		orderService.cancelOrder(orderId);

		// then
		Order findOrder = orderRepository.findOne(orderId);

		assertThat(findOrder.getStatus())
			.as("주문 취소 시 상태는 CANCEL 이다.")
			.isEqualTo(OrderStatus.CANCEL);

		assertThat(item.getStockQuantity())
			.as("주문이 취소된 상품은 그만큼 재고가 증가해야한다.")
			.isEqualTo(stockQuantity);
	}

	private Member createMember(String name) {
		Member member = new Member();
		member.setAddress(new Address("서울", "강가", "123-123"));
		member.setName(name);
		em.persist(member);
		return member;
	}

	private Book createBook(String name, int price, int stockQuantity) {
		Book book = new Book();
		book.setName(name);
		book.setPrice(price);
		book.setStockQuantity(stockQuantity);
		em.persist(book);
		return book;
	}
}