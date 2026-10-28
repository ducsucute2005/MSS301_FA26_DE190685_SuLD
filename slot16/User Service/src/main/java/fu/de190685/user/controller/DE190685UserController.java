package fu.de190685.user.controller;

import fu.de190685.user.dto.ApiResponse;
import fu.de190685.user.dto.PageDTO;
import fu.de190685.user.dto.UserDTO;
import fu.de190685.user.entity.Role;
import fu.de190685.user.entity.UserStatus;
import fu.de190685.user.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/users")
public class DE190685UserController {

	private final UserService userService;

	public DE190685UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse> create(@Valid @RequestBody UserDTO userDTO) {
		UserDTO createdUser = userService.create(userDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Created successfully", createdUser));
	}

	@PutMapping("/{userId}")
	public ResponseEntity<ApiResponse> update(@PathVariable Long userId, @Valid @RequestBody UserDTO userDTO) {
		return ResponseEntity.ok(ApiResponse.success("Updated successfully", userService.update(userId, userDTO)));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse> getById(@PathVariable Long userId) {
		return ResponseEntity.ok(ApiResponse.success("Successful", userService.getById(userId)));
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponse> deactivate(@PathVariable Long userId) {
		userService.deactivate(userId);
		return ResponseEntity.ok(ApiResponse.success("Deactivated successfully", null));
	}

	@GetMapping
	public ResponseEntity<ApiResponse> getUsers(
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be at least 0") int page,
			@RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be at least 1")
			@Max(value = 100, message = "size must not exceed 100") int size,
			@RequestParam(required = false) Role role,
			@RequestParam(required = false) UserStatus status) {
		PageDTO users = userService.getUsers(page, size, role, status);
		return ResponseEntity.ok(ApiResponse.success("Successful", users));
	}
}
