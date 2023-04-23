package jpabook.jpashop.domain.order;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import jpabook.jpashop.domain.delivery.Delivery;
import jpabook.jpashop.domain.delivery.DeliveryStatus;
import jpabook.jpashop.domain.member.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Order {

	@Id
	@GeneratedValue
	@Column(name = "order_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	/**
	 * OrderItem과 Delivery는 Order 가 private owner 이므로 cascade 옵션을 사용했다.
	 * 즉, Order만 OrderItem과 Delivery를 참조 & persist 라이프 사이클이 같기 때문에 cascade를 사용한 것이다.
	 */
	@OneToMany(mappedBy = "order", cascade = ALL)
	private List<OrderItem> orderItems = new ArrayList<>();

	// 연관관계 주인이 Order인 이유:
	// Order -> Delivery 를 찾는 일이 더 많고, Delivery -> Order 찾는 일은 드물다.
	@OneToOne(fetch = LAZY, cascade = ALL)
	@JoinColumn(name = "delivery_id")
	private Delivery delivery;

	private LocalDateTime orderDate;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	//== 생성 메서드 ==//
	/* 이렇게 작성하는게 중요한 이유: 생성 시 변경 점이 생기면 이 메서드만 변경하면 된다. */
	public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
		Order order = new Order();

		order.setMember(member);
		order.setDelivery(delivery);
		for (OrderItem orderItem : orderItems) {
			order.addOrderItem(orderItem);
		}
		order.setStatus(OrderStatus.ORDER);
		order.setOrderDate(LocalDateTime.now());

		return order;
	}

	public void addOrderItem(OrderItem orderItem) {
		this.orderItems.add(orderItem);
		orderItem.setOrder(this);
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
		delivery.setOrder(this);
	}

	//== 연관관계 메서드 ==//
	public void setMember(Member member) {
		this.member = member;
		member.getOrders().add(this);
	}

	//== 비즈니스 로직 ==//

	/**
	 * 주문 취소
	 */
	public void cancel() {
		if (this.delivery.getStatus() == DeliveryStatus.COMP) {
			throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
		}

		this.setStatus(OrderStatus.CANCEL);
		for (OrderItem orderItem : this.orderItems) {
			orderItem.cancel();
		}
	}

	//== 조회 로직 ==//

	/**
	 * 전체 주문 가격 조회
	 */
	public int getTotalPrice() {
		return this.orderItems.stream()
			.mapToInt(OrderItem::getTotalPrice)
			.sum();
	}
}