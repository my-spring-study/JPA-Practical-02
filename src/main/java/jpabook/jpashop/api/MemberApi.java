package jpabook.jpashop.api;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.member.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApi {

	private final MemberService memberService;

	// V1: 엔티티를 Request Body에 직접 매핑
	// - 프레젠테이션 레이어의 검증 로직이 엔티티까지 침범한다(@NotEmpty 등).
	// - 엔티티가 변경되면 API 스펙이 변경된다.(엔티티와 API 스펙이 매핑되어있다.)
	// 해결법 -> API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
	@PostMapping("/api/v1/members")
	public CreateMemberResponse saveMemberV1(
		@RequestBody @Valid Member member // "실무에서는 엔티티를 외부에 노출하지 마라!!"
	) {
		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}

	// V2: 엔티티 대신에 DTO를 RequestBody에 매핑
	// - 엔티티와 프레젠테이션 계층을 위한 로직을 분리할 수 있다.
	// - 엔티티와 API 스펙을 명확하게 분리할 수 있다.
	// - 엔티티가 변해도 API 스펙이 변하지 않는다.
	@PostMapping("/api/v2/members")
	public CreateMemberResponse saveMemberV2(
		@RequestBody @Valid CreateMemberRequest request
	) {
		Member member = new Member();
		member.setName(request.getName());

		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}

	// PUT은 리소스를 완전히 교체하므로 멱등성을 보장한다.
	// PATCH는 멱등하게, 혹은 멱등하지 않게 구현이 가능하다. 즉 멱등성을 보장하지 않는다.
	@PutMapping("/api/v2/member/{id}")
	public UpdateMemberResponse updateMemberV2(
		@PathVariable("id") Long id,
		@RequestBody @Valid UpdateMemberRequest request
	) {
		// 커맨드와 쿼리를 철저하게 분리한다. CQS(Command Query Separation)
		// memberService.update 하면서 member를 리턴하면 update 하면서 query하는 꼴이 된다.
		memberService.update(id, request.getName()); // Command

		Member findMember = memberService.findOne(id); // Query
		return new UpdateMemberResponse(findMember.getId(), findMember.getName());
	}

	@Data
	static class CreateMemberRequest {
		private String name;
	}

	@Data
	static class CreateMemberResponse {
		private Long id;

		public CreateMemberResponse(Long id) {
			this.id = id;
		}
	}

	@Data
	@NoArgsConstructor // Reflection을 위한 기본 생성자 추가
	@AllArgsConstructor
	private static class UpdateMemberRequest {
		private String name;
	}

	@Data
	@AllArgsConstructor
	private static class UpdateMemberResponse {
		private Long id;
		private String name;
	}
}
