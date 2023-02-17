package jpabook.jpashop.domain.item;

import javax.persistence.Entity;

import org.springframework.web.bind.annotation.RequestParam;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Album extends Item {

	private String artist;
	private String etc;
}
