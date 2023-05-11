package jpabook.jpashop.domain.member;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.type.Address;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {

	@Id
	@GeneratedValue
	@Column(name = "member_id")
	private Long id;

	private String name; // 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 유니크 제약 조건을 추가하는 것이 안전

	@Embedded
	private Address address;

	@JsonIgnore
	@OneToMany(mappedBy = "member") // TODO: 멤버를 조회할 때는 Orders 도 Batch Size에 의해 IN Query 가 나갈 것인가?
	private List<Order> orders = new ArrayList<>();

	public Member() {
	}

	public Member(String name) {
		this.name = name;
	}
}
