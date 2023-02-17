package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

	@Transactional
	public void saveItem(Item item) {
		itemRepository.save(item);
	}

	@Transactional
	public void updateItem(Long itemId, UpdateItemDto itemDto) {
		Item findItem = itemRepository.findOne(itemId);

		/*
			Setter없이 엔티티 안에서 변경사항을 바로 추적할 수 있는 메서드를 만들어라.
			예) findItem.change(name, price, stockQuantity)
		 */
		findItem.setName(itemDto.getName());
		findItem.setPrice(itemDto.getPrice());
		findItem.setStockQuantity(itemDto.getStockQuantity());
	}

	public List<Item> findItems() {
		return itemRepository.findAll();
	}

	public Item findOne(Long id) {
		return itemRepository.findOne(id);
	}
}
