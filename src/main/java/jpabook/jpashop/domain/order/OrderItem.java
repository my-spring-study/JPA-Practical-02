package jpabook.jpashop.domain.order;

import static javax.persistence.FetchType.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) // protected OrderItem() {} 생성
@Getter
@Setter
public class OrderItem {

	@Id
	@GeneratedValue
	@Column(name = "order_item_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "item_id")
	private Item item;

	@JsonIgnore
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	private int orderPrice;

	private int count;

	//== 생성 메서드==//
	public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
		OrderItem orderItem = new OrderItem();
		orderItem.setItem(item);
		orderItem.setOrderPrice(orderPrice);
		orderItem.setCount(count);

		item.removeStock(count);

		return orderItem;
	}

	//== 비즈니스 로직 ==//
	public void cancel() {
		this.item.addStock(count);
	}

	//== 조회 로직 ==//

	/**
	 * 주문상품 전체 가격 조회
	 */
	public int getTotalPrice() {
		return this.orderPrice * this.count;
	}
}
