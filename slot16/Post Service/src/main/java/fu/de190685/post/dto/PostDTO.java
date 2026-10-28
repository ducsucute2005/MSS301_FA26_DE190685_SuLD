package fu.de190685.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

public class PostDTO {

	private Long postId;

	@NotBlank(groups = OnCreate.class, message = "advertiserName is required")
	@Size(max = 100, message = "advertiserName must not exceed 100 characters")
	private String advertiserName;

	@NotBlank(groups = OnCreate.class, message = "advertiserEmail is required")
	@Email(message = "advertiserEmail must be valid")
	@Size(max = 100, message = "advertiserEmail must not exceed 100 characters")
	private String advertiserEmail;

	@NotBlank(groups = OnCreate.class, message = "advertiserPhone is required")
	@Size(max = 20, message = "advertiserPhone must not exceed 20 characters")
	private String advertiserPhone;

	@NotNull(groups = OnCreate.class, message = "startDate is required")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "UTC")
	private Date startDate;

	@NotNull(groups = OnCreate.class, message = "endDate is required")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "UTC")
	private Date endDate;

	@NotNull(groups = OnCreate.class, message = "numberOfPosts is required")
	@Min(value = 1, message = "numberOfPosts must be at least 1")
	private Integer numberOfPosts;

	private BigDecimal totalCost;

	@Pattern(regexp = "DRAFT|SCHEDULED|PUBLISHED|CANCELLED", message = "status is invalid")
	private String status;

	@NotNull(groups = OnCreate.class, message = "userId is required")
	@Min(value = 1, message = "userId must be positive")
	private Long userId;

	public PostDTO() {
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getAdvertiserName() {
		return advertiserName;
	}

	public void setAdvertiserName(String advertiserName) {
		this.advertiserName = advertiserName;
	}

	public String getAdvertiserEmail() {
		return advertiserEmail;
	}

	public void setAdvertiserEmail(String advertiserEmail) {
		this.advertiserEmail = advertiserEmail;
	}

	public String getAdvertiserPhone() {
		return advertiserPhone;
	}

	public void setAdvertiserPhone(String advertiserPhone) {
		this.advertiserPhone = advertiserPhone;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Integer getNumberOfPosts() {
		return numberOfPosts;
	}

	public void setNumberOfPosts(Integer numberOfPosts) {
		this.numberOfPosts = numberOfPosts;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
