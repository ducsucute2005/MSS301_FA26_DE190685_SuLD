package fu.de190685.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class UserDTO {

	private Long userId;

	@Pattern(regexp = "(?s).*\\S.*", message = "username must not be blank")
	@Size(max = 30, message = "username must not exceed 30 characters")
	private String username;

	@Pattern(regexp = "(?s).*\\S.*", message = "email must not be blank")
	@Email(message = "email must be valid")
	@Size(max = 100, message = "email must not exceed 100 characters")
	private String email;

	@Pattern(regexp = "USER|CREATOR|ADMIN", message = "role must be USER, CREATOR, or ADMIN")
	private String role;

	@DecimalMin(value = "0", inclusive = false, message = "ratePerPost must be greater than 0")
	@Digits(integer = 16, fraction = 2, message = "ratePerPost must fit decimal(18,2)")
	private BigDecimal ratePerPost;

	@Min(value = 1, message = "maxDailyPosts must be at least 1")
	@Max(value = 10, message = "maxDailyPosts must not exceed 10")
	private Integer maxDailyPosts;

	@Min(value = 1, message = "level must be at least 1")
	private Integer level;

	@Pattern(regexp = "ACTIVE|SUSPENDED|DEACTIVATED", message = "status must be ACTIVE, SUSPENDED, or DEACTIVATED")
	private String status;

	public UserDTO() {
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public BigDecimal getRatePerPost() {
		return ratePerPost;
	}

	public void setRatePerPost(BigDecimal ratePerPost) {
		this.ratePerPost = ratePerPost;
	}

	public Integer getMaxDailyPosts() {
		return maxDailyPosts;
	}

	public void setMaxDailyPosts(Integer maxDailyPosts) {
		this.maxDailyPosts = maxDailyPosts;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
