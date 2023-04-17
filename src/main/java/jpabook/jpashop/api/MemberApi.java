package jpabook.jpashop.api;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.member.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
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

	@Data
	static class CreateMemberRequest {
		private String name;
	}

	@Data
	static class CreateMemberResponse{

		private Long id;

		public CreateMemberResponse(Long id) {
			this.id = id;
		}
	}
}
