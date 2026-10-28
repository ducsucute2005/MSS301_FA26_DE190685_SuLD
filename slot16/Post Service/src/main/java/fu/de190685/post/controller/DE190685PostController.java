package fu.de190685.post.controller;

import fu.de190685.post.dto.ApiResponse;
import fu.de190685.post.dto.PageDTO;
import fu.de190685.post.dto.PostDTO;
import fu.de190685.post.dto.PostDetailDTO;
import fu.de190685.post.dto.OnCreate;
import fu.de190685.post.entity.PostStatus;
import fu.de190685.post.service.PostService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/posts")
public class DE190685PostController {

	private final PostService postService;

	public DE190685PostController(PostService postService) {
		this.postService = postService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PostDTO>> create(
			@RequestBody @Validated(OnCreate.class) PostDTO postDTO) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Created successfully", postService.create(postDTO)));
	}

	@PutMapping("/{postId}")
	public ResponseEntity<ApiResponse<PostDTO>> update(
			@PathVariable Long postId, @Valid @RequestBody PostDTO postDTO) {
		return ResponseEntity.ok(ApiResponse.success("Updated successfully", postService.update(postId, postDTO)));
	}

	@GetMapping("/{postId}")
	public ResponseEntity<ApiResponse<PostDetailDTO>> getById(@PathVariable Long postId) {
		return ResponseEntity.ok(ApiResponse.success("Successful", postService.getById(postId)));
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long postId) {
		postService.cancel(postId);
		return ResponseEntity.ok(ApiResponse.success("Cancelled successfully", null));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageDTO>> getPosts(
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be at least 0") int page,
			@RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be at least 1")
			@Max(value = 100, message = "size must not exceed 100") int size,
			@RequestParam(required = false) String advertiserName,
			@RequestParam(required = false) PostStatus status) {
		return ResponseEntity.ok(ApiResponse.success(
				"Successful", postService.getPosts(page, size, advertiserName, status)));
	}
}
