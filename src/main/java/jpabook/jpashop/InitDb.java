package jpabook.jpashop;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.delivery.Delivery;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.member.Member;
import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderItem;
import jpabook.jpashop.domain.type.Address;
import lombok.RequiredArgsConstructor;

/*
 	// 회원 2명, 회원당 주문 2건
 	- userA
		- JPA1 Book
 		- JPA2 Book
	- userB
		- Spring1 Book
		- Spring2 Book
 */

// 서버 띄우면 Spring 의 ComponentScan 에 의해 Scan
// -> Spring Bean 에 엮이고 Spring Bean이 다 완료 되면 @PostConstruct 호
@Component
@RequiredArgsConstructor
public class InitDb {

	private final InitService initService;

	@PostConstruct // 애플리케이션 로딩 시점에 호출
	public void init() {
		// 이곳에 더미 데이터를 생성하는 것은 코드가 잘 실행되지 않는다.
		// 이유: 스프링 라이프 사이클 때문에 트랜잭션이 잘 먹히질 않는다.
		initService.dbInit1();
		initService.dbInit2();
	}

	@Component
	@Transactional
	@RequiredArgsConstructor
	static class InitService {

		private final EntityManager em;

		public void dbInit1() {
			Member member = createMember("userA", "서울", "1", "1111	");
			em.persist(member);

			Book book1 = createBook("JPA1 Book", 10000, 100);
			em.persist(book1);
			Book book2 = createBook("JPA2 Book", 20000, 100);
			em.persist(book2);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		public void dbInit2() {
			Member member = createMember("userB", "경기", "2", "2222");
			em.persist(member);

			Book book1 = createBook("Spring1 Book", 10000, 100);
			em.persist(book1);
			Book book2 = createBook("Spring2 Book", 20000, 100);
			em.persist(book2);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		private Member createMember(String name, String city, String street, String zipcode) {
			Member member = new Member();
			member.setName(name);
			member.setAddress(new Address(city, street, zipcode));
			return member;
		}

		private Book createBook(String name, int price, int stockQuantity) {
			Book book1 = new Book();
			book1.setName(name);
			book1.setPrice(price);
			book1.setStockQuantity(stockQuantity);
			return book1;
		}

		private Delivery createDelivery(Member member) {
			Delivery delivery = new Delivery();
			delivery.setAddress(member.getAddress());
			return delivery;
		}
	}
}
