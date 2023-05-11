package jpabook.jpashop.domain.item;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.BatchSize;

import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

@BatchSize(size = 1000) // XXXToOne: OrderItem(Many) To Item(One) 관계에서는 @BatchSize를 클래스 레벨에 작성한다.
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Item {

	@Id
	@GeneratedValue
	@Column(name = "item_id")
	private Long id;

	private String name;

	private int price;

	private int stockQuantity;

	@ManyToMany(mappedBy = "items")
	private List<Category> categories = new ArrayList<>();

	public Item() {
	}

	/* "엔티티 안에 비즈니스 로직을 넣는 것이 좋다(DDD)." */
	//==비즈니스 로직==//

	/**
	 * stock 증가
	 */
	public void addStock(int quantity) {
		this.stockQuantity += quantity;
	}

	/**
	 * stock 감소
	 */
	public void removeStock(int quantity) {
		int restStock = this.stockQuantity - quantity;

		if (restStock < 0) {
			throw new NotEnoughStockException("need more stock");
		}

		this.stockQuantity = restStock;
	}
}
