package jpabook.jpashop.controller.item;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.UpdateItemDto;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping("/items/new")
	public String createForm(Model model) {
		model.addAttribute("form", new BookForm());
		return "items/createItemForm";
	}

	@PostMapping("/items/new")
	public String create(BookForm form) {
		Book book = new Book();
		book.setName(form.getName());
		book.setPrice(form.getPrice());
		book.setStockQuantity(form.getStockQuantity());
		book.setAuthor(form.getAuthor());
		book.setIsbn(form.getIsbn());

		itemService.saveItem(book);
		return "redirect:/";
	}

	@GetMapping("/items")
	public String list(Model model) {
		List<Item> items = itemService.findItems();
		model.addAttribute("items", items);
		return "items/itemList";
	}

	@GetMapping("/items/{itemId}/edit")
	public String updateItemForm(
		@PathVariable("itemId") Long itemId,
		Model model
	) {
		Book item = (Book)itemService.findOne(itemId);

		BookForm form = new BookForm();
		form.setId(item.getId());
		form.setName(item.getName());
		form.setPrice(item.getPrice());
		form.setStockQuantity(item.getStockQuantity());
		form.setAuthor(item.getAuthor());
		form.setIsbn(item.getIsbn());

		model.addAttribute("form", form);
		return "items/updateItemForm";
	}

	@PostMapping("/items/{itemId}/edit")
	public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable Long itemId) {

		// 임의로 만들어낸 엔티티 -> 기존 식별자를 갖고 있으면 준영속 엔티티이다.
		// 준영속 엔티티는 변경감지가 바로 작동하지 않기 때문에, DB에서 pk로 찾아서 영속상태를 만들고 변경감지를 하거나,
		// 아니면 병합(merge)를 사용하여 컬럼을 업데이트한다.
		// Book book = new Book();
		// book.setId(form.getId());
		// book.setName(form.getName());
		// book.setPrice(form.getPrice());
		// book.setStockQuantity(form.getStockQuantity());
		// book.setAuthor(form.getAuthor());
		// book.setIsbn(form.getIsbn());

		// itemService.saveItem(book);

		UpdateItemDto itemDto = new UpdateItemDto(form.getName(), form.getPrice(), form.getStockQuantity());
		itemService.updateItem(itemId, itemDto);
		return "redirect:/items";
	}
}